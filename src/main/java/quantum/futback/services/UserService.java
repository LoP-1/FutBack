package quantum.futback.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantum.futback.core.multitenancy.TenantContext;
import quantum.futback.entity.Role;
import quantum.futback.entity.User;
import quantum.futback.repository.RoleRepository; // Asumo RoleRepository para asignar ROLE_PARENT
import quantum.futback.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User findOrCreateParentUser(String dni, String fullName) {

        Optional<User> existingUser = userRepository.findByDni(dni);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = new User();

        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context must be set before creating a parent user.");
        }
        newUser.setTenantId(tenantId);

        Role parentRole = roleRepository.findByName("PARENT")
                .orElseGet(() -> {
                    // Si el rol PARENT no existe, se crea
                    Role role = new Role();
                    role.setName("PARENT");
                    role.setTenantId(tenantId);
                    return roleRepository.save(role);
                });

        newUser.setRole(parentRole);
        newUser.setDni(dni);
        newUser.setFullName(fullName);
        newUser.setEmail(dni + "@futback.com"); // Email por defecto
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString())); // Contraseña temporal
        newUser.setActive(true);

        return userRepository.save(newUser);
    }

}