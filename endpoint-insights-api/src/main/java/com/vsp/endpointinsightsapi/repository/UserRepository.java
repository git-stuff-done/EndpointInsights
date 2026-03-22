package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their OIDC identity (issuer and subject).
     *
     * @param issuer the OIDC issuer claim
     * @param subject the OIDC subject claim
     * @return the user if found
     */
    Optional<User> findByIssuerAndSubject(String issuer, String subject);
}
