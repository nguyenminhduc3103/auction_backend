package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.admin.BanUserRequest;
import vn.team9.auction_system.common.dto.transaction.TransactionResponse;
import vn.team9.auction_system.common.dto.user.UserRequest;
import vn.team9.auction_system.common.dto.user.UserResponse;
import java.util.List;

public interface IUserService {
    UserResponse register(UserRequest request);
    UserResponse updateUser(Long id, UserRequest request);
    UserResponse getUserById(Long id);
    UserResponse banUser(Long id, BanUserRequest request);
    UserResponse unbanUser(Long id);
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
    List<TransactionResponse> getAllTransactions(Long userId);
}
