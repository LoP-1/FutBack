package quantum.futback.core.multitenancy;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TenantInterceptor implements Interceptor {

    private static final String TENANT_ID_PROPERTY = "tenantId";

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        if (!(entity instanceof TenantAware)) {
            return false;
        }

        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Cannot save TenantAware entity. Tenant ID is missing in TenantContext.");
        }

        return injectTenantId(state, propertyNames, tenantId);
    }

    private boolean injectTenantId(Object[] state, String[] propertyNames, UUID tenantId) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (TENANT_ID_PROPERTY.equals(propertyNames[i])) {
                state[i] = tenantId;
                return true;
            }
        }
        return false;
    }
}