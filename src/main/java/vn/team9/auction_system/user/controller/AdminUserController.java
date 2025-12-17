package vn.team9.auction_system.user.controller;

import vn.team9.auction_system.common.dto.admin.BanUserRequest;
import vn.team9.auction_system.common.dto.transaction.TransactionResponse;
import vn.team9.auction_system.common.dto.user.UserRequest;
import vn.team9.auction_system.common.dto.user.UserResponse;
import vn.team9.auction_system.common.service.IUserService;
import vn.team9.auction_system.user.service.AdminServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;


@RestController
@RequestMapping("api/superadmin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminServiceImpl adminServiceImpl;

    @Autowired
    private IUserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @PutMapping("/{id}/ban")
    public ResponseEntity<UserResponse> banUser(
            @PathVariable Long id,
            @RequestBody BanUserRequest request) {
        return ResponseEntity.ok(userService.banUser(id, request));
    }

    @PutMapping("/{id}/unban")
    public ResponseEntity<UserResponse> unbanUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unbanUser(id));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    } 
    
    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getUserTransactions(@PathVariable Long id) {
        return adminServiceImpl.getAllTransactions(id);
    }
    
}
