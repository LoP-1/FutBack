package quantum.futback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quantum.futback.entity.DTO.TenantSetupRequest;
import quantum.futback.entity.Tenant;
import quantum.futback.services.TenantService;

@RestController
@RequestMapping("/api/internal/tenants")
public class InternalTenantController {

    private final TenantService tenantService;

    public InternalTenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/admin-setup")
    public ResponseEntity<Tenant> setupTenant(@RequestBody TenantSetupRequest request) {
        Tenant newTenant = tenantService.createTenantSetup(request);
        return ResponseEntity.ok(newTenant);
    }
}