package quantum.futback.core.multitenancy;

public interface TenantAware {
    Long getTenantId();
    void setTenantId(Long tenantId);
}
