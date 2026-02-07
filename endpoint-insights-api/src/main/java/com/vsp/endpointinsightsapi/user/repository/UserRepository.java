package com.vsp.endpointinsightsapi.user.repository;


import com.vsp.endpointinsightsapi.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    @Query("SELECT u FROM User u WHERE UPPER(u.name) LIKE UPPER(CONCAT(:query, '%'))")
    List<User> findAllMatching(@Param("query") String query);
}
