package vn.team9.auction_system.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import vn.team9.auction_system.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Only eager load role, not permissions - permissions will be loaded lazily if
    // needed
    @EntityGraph(attributePaths = { "role" })
    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, Long userId);

    boolean existsByEmailAndUserIdNot(String email, Long userId);

    List<User> findAllByIsDeletedFalse();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findByUserId(Long userId);
}