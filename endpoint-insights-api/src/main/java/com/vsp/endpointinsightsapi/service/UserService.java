package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.User;
import com.vsp.endpointinsightsapi.model.UserContext;
import com.vsp.endpointinsightsapi.model.enums.UserRole;
import com.vsp.endpointinsightsapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing user persistence based on OIDC authentication.
 *
 * <p>Automatically creates or updates user records when they authenticate,
 * using their OIDC issuer and subject claims as the unique identity.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates or updates a user based on their OIDC identity and current JWT claims.
     *
     * <p>This method should be called during authentication to ensure the user
     * record is up-to-date with their latest information from the identity provider.
     *
     * @param iss OIDC issuer claim (identity provider URL)
     * @param sub OIDC subject claim (unique user ID within issuer)
     * @param name Display name (from preferred_username or name claim)
     * @param email Email address (from email claim)
     * @param roles List of roles extracted from groups claim
     * @return the created or updated user entity
     */
    @Transactional
    public User createOrUpdateUser(String iss, String sub, String name, String email, List<UserRole> roles) {
        User user = userRepository.findByIssuerAndSubject(iss, sub)
                .orElse(new User());

        user.setIssuer(iss);
        user.setSubject(sub);
        user.setName(name);
        user.setEmail(email);

        UserRole highestRole = UserRole.NONE;
        if (roles.contains(UserRole.WRITE)) {
            highestRole = UserRole.WRITE;
        } else if (roles.contains(UserRole.READ)) {
            highestRole = UserRole.READ;
        }
        user.setRole(highestRole);

        User savedUser = userRepository.save(user);
        LOG.debug("User created/updated: {} ({}/{})", name, iss, sub);

        return savedUser;
    }

    /**
     * Creates or updates a user from a UserContext.
     *
     * @param userContext the user context from JWT validation
     * @return the created or updated user entity
     */
    @Transactional
    public User createOrUpdateUser(UserContext userContext) {
        return createOrUpdateUser(
                userContext.getIssuer(),
                userContext.getSubject(),
                userContext.getUsername(),
                userContext.getEmail(),
                userContext.getRoles()
        );
    }
}
