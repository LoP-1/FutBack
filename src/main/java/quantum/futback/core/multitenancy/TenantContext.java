package quantum.futback.core.multitenancy;

import java.util.Optional;

public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        if (tenantId != null) {
            currentTenant.set(tenantId);
        }
    }

    public static Long getTenantId() {
        return currentTenant.get();
    }

    public static Optional<Long> getOptionalTenantId() {
        return Optional.ofNullable(currentTenant.get());
    }

    public static void clear() {
        currentTenant.remove();
    }
}