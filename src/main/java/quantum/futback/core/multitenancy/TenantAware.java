package quantum.futback.core.multitenancy;

import java.util.UUID;

public interface TenantAware {
    UUID getTenantId();
    void setTenantId(UUID tenantId);
}
