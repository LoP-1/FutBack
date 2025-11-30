package quantum.futback.services.interfaces;
import org.springframework.security.core.Authentication;
import quantum.futback.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Optional<User> findById(UUID id);
    Authentication getAuthenticationFromUser(User user);
}