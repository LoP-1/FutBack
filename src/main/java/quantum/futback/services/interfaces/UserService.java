package quantum.futback.services.interfaces;
import org.springframework.security.core.Authentication;
import quantum.futback.entity.User;
import quantum.futback.entity.DTO.ChangePasswordRequest;
import quantum.futback.entity.DTO.UserCreateRequest;
import quantum.futback.entity.DTO.UserUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Authentication getAuthenticationFromUser(User user);
    User createUser(UserCreateRequest request);
    User updateUser(UUID id, UserUpdateRequest request);
    User updateUserStatus(UUID id, boolean isActive);
    List<User> listUsers(UUID roleId, String dni);
    User getByIdOrThrow(UUID id);
    User changeOwnPassword(UUID userId, ChangePasswordRequest request);
}
