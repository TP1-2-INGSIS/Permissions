package com.ingsis.CNC.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  Optional<User> findByUserName(String userName);

  List<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
      String userName, String email);
}
