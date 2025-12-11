package quantum.futback.services;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import quantum.futback.config.security.JwtToken.UserPrincipal;
import quantum.futback.core.multitenancy.TenantContext;
import quantum.futback.entity.DTO.ChangePasswordRequest;
import quantum.futback.entity.DTO.UserCreateRequest;
import quantum.futback.entity.DTO.UserUpdateRequest;
import quantum.futback.entity.Role;
import quantum.futback.entity.User;
import quantum.futback.repository.RoleRepository;
import quantum.futback.repository.UserRepository;
import quantum.futback.services.interfaces.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Authentication getAuthenticationFromUser(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        return new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant no definido");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
        }

        if (userRepository.findByDni(request.getDni()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "DNI ya registrado");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        if (!tenantId.equals(role.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol pertenece a otro tenant");
        }

        User user = new User();
        user.setTenantId(tenantId);
        user.setRole(role);
        user.setDni(request.getDni());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setActive(request.getIsActive() == null || request.getIsActive());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(UUID id, UserUpdateRequest request) {
        User user = getByIdOrThrow(id);

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserStatus(UUID id, boolean isActive) {
        User user = getByIdOrThrow(id);
        user.setActive(isActive);
        return userRepository.save(user);
    }

    @Override
    public List<User> listUsers(UUID roleId, String dni) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(u -> roleId == null || (u.getRole() != null && roleId.equals(u.getRole().getId())))
                .filter(u -> dni == null || dni.equalsIgnoreCase(u.getDni()))
                .toList();
    }

    @Override
    public User getByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Override
    @Transactional
    public User changeOwnPassword(UUID userId, ChangePasswordRequest request) {
        User user = getByIdOrThrow(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        return userRepository.save(user);
    }
}
