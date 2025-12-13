package quantum.futback.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantum.futback.config.security.JwtToken.UserPrincipal;
import quantum.futback.entity.User;
import quantum.futback.repository.UserRepository;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        User user;
        
        // Try to parse as UUID first (for JWT authentication)
        try {
            UUID userId = UUID.fromString(usernameOrId);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + usernameOrId));
        } catch (IllegalArgumentException e) {
            // If not a UUID, treat as DNI (for login)
            user = userRepository.findByDni(usernameOrId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with dni: " + usernameOrId));
        }

        return UserPrincipal.create(user);
    }
}
