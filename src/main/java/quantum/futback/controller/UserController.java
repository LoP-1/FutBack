package quantum.futback.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import quantum.futback.config.security.JwtToken.UserPrincipal;
import quantum.futback.entity.DTO.ChangePasswordRequest;
import quantum.futback.entity.DTO.UserCreateRequest;
import quantum.futback.entity.DTO.UserUpdateRequest;
import quantum.futback.entity.DTO.StatusUpdateRequest;
import quantum.futback.entity.User;
import quantum.futback.services.interfaces.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> listUsers(@RequestParam(required = false) UUID roleId,
                                                @RequestParam(required = false) String dni) {
        return ResponseEntity.ok(userService.listUsers(roleId, dni));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        User created = userService.createUser(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> updateStatus(@PathVariable UUID id, @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(id, request.getIsActive() != null && request.getIsActive()));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getByIdOrThrow(principal.getId()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<User> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changeOwnPassword(principal.getId(), request));
    }
}
