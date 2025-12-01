package quantum.futback.core.multitenancy;

import java.util.Optional;
import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public static void setTenantId(UUID tenantId) { // Cambio de Long a UUID
        if (tenantId != null) {
            currentTenant.set(tenantId);
        }
    }

    public static UUID getTenantId() {
        return currentTenant.get();
    }

    public static Optional<UUID> getOptionalTenantId() {
        return Optional.ofNullable(currentTenant.get());
    }

    public static void clear() {
        currentTenant.remove();
    }
}