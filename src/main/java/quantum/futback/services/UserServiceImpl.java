package quantum.futback.services;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import quantum.futback.config.security.JwtToken.UserPrincipal;
import quantum.futback.entity.User;
import quantum.futback.repository.UserRepository;
import quantum.futback.services.interfaces.UserService;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Authentication getAuthenticationFromUser(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        return new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
    }
}
