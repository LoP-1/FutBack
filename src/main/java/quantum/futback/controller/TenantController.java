package quantum.futback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import quantum.futback.entity.Tenant;
import quantum.futback.services.TenantService;

import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/current")
    public ResponseEntity<Tenant> getCurrentTenant() {
        return ResponseEntity.ok(tenantService.getCurrentTenant());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Tenant> updateStatus(@PathVariable UUID id, @RequestParam boolean isActive) {
        return ResponseEntity.ok(tenantService.updateTenantStatus(id, isActive));
    }
}
