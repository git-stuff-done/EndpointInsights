package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.enums.GitAuthType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GitRepositoryServiceTest {

    @TempDir
    Path tempDir;

    private GitRepositoryService gitRepositoryService;

    @BeforeEach
    void setUp() {
        gitRepositoryService = new GitRepositoryService(tempDir.toString());
    }

    @Test
    void constructor_shouldCreateCheckoutDirectory() {
        assertTrue(Files.exists(tempDir));
        assertTrue(Files.isDirectory(tempDir));
    }

    @Test
    void checkoutJobRepository_withNullGitUrl_shouldThrowException() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gitRepositoryService.checkoutJobRepository(job)
        );

        assertTrue(exception.getMessage().contains("Git URL is required"));
    }

    @Test
    void checkoutJobRepository_withEmptyGitUrl_shouldThrowException() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl("  ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gitRepositoryService.checkoutJobRepository(job)
        );

        assertTrue(exception.getMessage().contains("Git URL is required"));
    }

    @Test
    void checkoutJobRepository_withInvalidUrl_shouldThrowException() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl("invalid-url");
        job.setGitAuthType(GitAuthType.NONE);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gitRepositoryService.checkoutJobRepository(job)
        );

        assertTrue(exception.getMessage().contains("Unable to checkout repository"));
    }

    @Test
    void checkoutJobRepository_withBasicAuthMissingUsername_shouldThrowException() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl("https://github.com/test/repo.git");
        job.setGitAuthType(GitAuthType.BASIC);
        job.setGitPassword("password");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gitRepositoryService.checkoutJobRepository(job)
        );

        assertTrue(exception.getMessage().contains("Git basic auth requires username and password"));
    }

    @Test
    void checkoutJobRepository_withBasicAuthMissingPassword_shouldThrowException() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl("https://github.com/test/repo.git");
        job.setGitAuthType(GitAuthType.BASIC);
        job.setGitUsername("user");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gitRepositoryService.checkoutJobRepository(job)
        );

        assertTrue(exception.getMessage().contains("Git basic auth requires username and password"));
    }

    @Test
    void checkoutJobRepository_withSshAuthMissingPrivateKey_shouldThrowException() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl("git@github.com:test/repo.git");
        job.setGitAuthType(GitAuthType.SSH_KEY);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gitRepositoryService.checkoutJobRepository(job)
        );

        assertTrue(exception.getMessage().contains("SSH private key is required"));
    }

    @Test
    void buildCredentialsProvider_withNoneAuthType_shouldReturnNull() throws Exception {
        Job job = new Job();
        job.setGitAuthType(GitAuthType.NONE);

        Object provider = ReflectionTestUtils.invokeMethod(
                gitRepositoryService,
                "buildCredentialsProvider",
                job
        );

        assertNull(provider);
    }

    @Test
    void buildCredentialsProvider_withSshAuthType_shouldReturnNull() throws Exception {
        Job job = new Job();
        job.setGitAuthType(GitAuthType.SSH_KEY);

        Object provider = ReflectionTestUtils.invokeMethod(
                gitRepositoryService,
                "buildCredentialsProvider",
                job
        );

        assertNull(provider);
    }

    @Test
    void buildCredentialsProvider_withBasicAuthValidCredentials_shouldReturnProvider() throws Exception {
        Job job = new Job();
        job.setGitAuthType(GitAuthType.BASIC);
        job.setGitUsername("testuser");
        job.setGitPassword("testpass");

        Object provider = ReflectionTestUtils.invokeMethod(
                gitRepositoryService,
                "buildCredentialsProvider",
                job
        );

        assertNotNull(provider);
    }

    @Test
    void checkoutJobRepository_clonesAndPullsLocalRepo() throws Exception {
        Path originDir = Files.createTempDirectory(tempDir, "origin-repo");
        createLocalRepo(originDir, "initial");

        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl(originDir.toUri().toString());
        job.setGitAuthType(GitAuthType.NONE);

        Path checkoutPath = gitRepositoryService.checkoutJobRepository(job);

        assertTrue(Files.exists(checkoutPath.resolve(".git")));

        try (Git git = Git.open(originDir.toFile())) {
            Files.writeString(originDir.resolve("README.md"), "update", StandardOpenOption.TRUNCATE_EXISTING);
            git.add().addFilepattern("README.md").call();
            git.commit().setMessage("update").call();
        }

        Path secondCheckout = gitRepositoryService.checkoutJobRepository(job);

        assertEquals(checkoutPath, secondCheckout);
    }

    @Test
    void checkoutJobRepository_withSshAuth_clonesLocalRepo() throws Exception {
        Path originDir = Files.createTempDirectory(tempDir, "origin-ssh-repo");
        createLocalRepo(originDir, "initial");

        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setGitUrl(originDir.toUri().toString());
        job.setGitAuthType(GitAuthType.SSH_KEY);
        job.setGitSshPrivateKey("dummy-key");

        Path checkoutPath = gitRepositoryService.checkoutJobRepository(job);

        assertTrue(Files.exists(checkoutPath.resolve(".git")));
    }

    private void createLocalRepo(Path repoDir, String content) throws Exception {
        try (Git git = Git.init().setDirectory(repoDir.toFile()).call()) {
            Path readme = repoDir.resolve("README.md");
            Files.writeString(readme, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            git.add().addFilepattern("README.md").call();
            git.commit().setMessage("init").call();
        }
    }
}
