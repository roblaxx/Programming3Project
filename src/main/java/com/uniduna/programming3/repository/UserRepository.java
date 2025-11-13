package com.uniduna.programming3.repository;

import com.uniduna.programming3.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    // Getting a User via his mail
    @Query("SELECT u FROM Users u WHERE u.mail = :mail")
    Optional<Users> findUserByMail(@Param("mail") String mail);
}
