package com.vsp.endpointinsightsapi.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.enums.GitAuthType;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.function.Supplier;

@Service
public class GitRepositoryService {

    private static final Logger LOG = LoggerFactory.getLogger(GitRepositoryService.class);
    private static final String DEFAULT_CHECKOUT_DIR = "./.git-checkouts";

    private final Path checkoutRoot;

    public GitRepositoryService(@Value("${app.git.checkout-dir:" + DEFAULT_CHECKOUT_DIR + "}") String checkoutDir) {
        this.checkoutRoot = Paths.get(checkoutDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.checkoutRoot);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize checkout directory", e);
        }
    }

    public Path checkoutJobRepository(Job job) {
        if (job.getGitUrl() == null || job.getGitUrl().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Git URL is required to checkout a repository");
        }

        Path checkoutPath = checkoutRoot.resolve(job.getJobId().toString());

        try {
            if (Files.exists(checkoutPath.resolve(".git"))) {
                pullRepository(job, checkoutPath);
            } else {
                cloneRepository(job, checkoutPath);
            }
            return checkoutPath;
        } catch (GitAPIException | IOException e) {
            LOG.error("Failed to checkout repository for job {}", job.getJobId(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to checkout repository", e);
        }
    }

    private void cloneRepository(Job job, Path checkoutPath) throws GitAPIException, IOException {
        CredentialsProvider credentialsProvider = buildCredentialsProvider(job);
        Runnable cloneAction = () -> {
            CloneCommand command = Git.cloneRepository()
                    .setURI(job.getGitUrl())
                    .setDirectory(checkoutPath.toFile());
            if (credentialsProvider != null) {
                command.setCredentialsProvider(credentialsProvider);
            }
            try (Git ignored = command.call()) {
                LOG.info("Repository cloned for job {}", job.getJobId());
            } catch (GitAPIException e) {
                throw new GitOperationException(e);
            }
        };

        runWithSshIfNeeded(job, cloneAction);
    }

    private void pullRepository(Job job, Path checkoutPath) throws GitAPIException, IOException {
        CredentialsProvider credentialsProvider = buildCredentialsProvider(job);
        Runnable pullAction = () -> {
            try (Git git = Git.open(checkoutPath.toFile())) {
                PullCommand command = git.pull();
                if (credentialsProvider != null) {
                    command.setCredentialsProvider(credentialsProvider);
                }
                command.call();
                LOG.info("Repository updated for job {}", job.getJobId());
            } catch (IOException | GitAPIException e) {
                throw new GitOperationException(e);
            }
        };

        runWithSshIfNeeded(job, pullAction);
    }

    private void runWithSshIfNeeded(Job job, Runnable action) throws GitAPIException, IOException {
        if (job.getGitAuthType() == GitAuthType.SSH_KEY) {
            String privateKey = job.getGitSshPrivateKey();
            if (privateKey == null || privateKey.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SSH private key is required for git SSH auth");
            }

            try {
                withSshKey(privateKey, job.getGitSshPassphrase(), () -> {
                    action.run();
                    return null;
                });
                return;
            } catch (GitOperationException e) {
                if (e.getCause() instanceof GitAPIException gitException) {
                    throw gitException;
                }
                if (e.getCause() instanceof IOException ioException) {
                    throw ioException;
                }
                throw e;
            }
        }

        try {
            action.run();
        } catch (GitOperationException e) {
            if (e.getCause() instanceof GitAPIException gitException) {
                throw gitException;
            }
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    private CredentialsProvider buildCredentialsProvider(Job job) {
        if (job.getGitAuthType() != GitAuthType.BASIC) {
            return null;
        }

        String username = job.getGitUsername();
        String password = job.getGitPassword();
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Git basic auth requires username and password");
        }

        return new UsernamePasswordCredentialsProvider(username, password);
    }

    private <T> T withSshKey(String privateKey, String passphrase, Supplier<T> action) {
        Path tempDir = null;
        SshSessionFactory previousFactory = SshSessionFactory.getInstance();
        try {
            tempDir = Files.createTempDirectory("ei-git-ssh");
            Path sshDir = Files.createDirectories(tempDir.resolve(".ssh"));
            Path keyPath = sshDir.resolve("id_rsa");

            Files.writeString(keyPath, privateKey, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            setOwnerOnlyPermissions(keyPath);

            JschConfigSessionFactory factory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host host, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch jsch = super.createDefaultJSch(fs);
                    jsch.removeAllIdentity();
                    if (passphrase != null && !passphrase.trim().isEmpty()) {
                        jsch.addIdentity(keyPath.toString(), passphrase);
                    } else {
                        jsch.addIdentity(keyPath.toString());
                    }
                    return jsch;
                }
            };

            SshSessionFactory.setInstance(factory);
            return action.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            SshSessionFactory.setInstance(previousFactory);
            if (tempDir != null) {
                deleteDirectoryQuietly(tempDir);
            }
        }
    }

    private void setOwnerOnlyPermissions(Path keyPath) {
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(keyPath, permissions);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Non-POSIX file system or permissions not supported.
        }
    }

    private void deleteDirectoryQuietly(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }

    private static class GitOperationException extends RuntimeException {
        private GitOperationException(Exception cause) {
            super(cause);
        }
    }
}
