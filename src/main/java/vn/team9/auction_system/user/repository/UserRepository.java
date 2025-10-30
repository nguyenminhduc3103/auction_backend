package vn.team9.auction_system.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.team9.auction_system.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
