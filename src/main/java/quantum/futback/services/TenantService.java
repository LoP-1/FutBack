package quantum.futback.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantum.futback.core.multitenancy.TenantContext;
import quantum.futback.entity.DTO.TenantSetupRequest;
import quantum.futback.entity.Role;
import quantum.futback.entity.Tenant;
import quantum.futback.entity.User;
import quantum.futback.repository.RoleRepository;
import quantum.futback.repository.TenantRepository;
import quantum.futback.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(TenantRepository tenantRepository,
                         UserRepository userRepository,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Tenant createTenantSetup(TenantSetupRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.academyName());
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setActive(true);

        Tenant savedTenant = tenantRepository.save(tenant);
        UUID newTenantId = savedTenant.getId();
        TenantContext.setTenantId(newTenantId);

        try {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setTenantId(newTenantId);
            roleRepository.save(adminRole);

            User adminUser = new User();
            adminUser.setFullName(request.adminFullName());
            adminUser.setEmail(request.adminEmail());
            adminUser.setDni(request.adminDni());
            adminUser.setPhone(request.adminPhone());
            adminUser.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
            adminUser.setRole(adminRole);
            adminUser.setActive(true);
            adminUser.setTenantId(newTenantId); // CAMBIO: Usar UUID real

            userRepository.save(adminUser);

        } finally {
            TenantContext.clear();
        }

        return savedTenant;
    }

    public Tenant getCurrentTenant() {
        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId != null) {
            return tenantRepository.findById(currentTenantId)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
        }
        return tenantRepository.findAll().stream().findFirst().orElseThrow();
    }

    @Transactional
    public Tenant updateTenantStatus(UUID tenantId, boolean isActive) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setActive(isActive);
        return tenantRepository.save(tenant);
    }
}