package quantum.futback.core.multitenancy;

import jakarta.servlet.*;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TenantFilter implements Filter {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String TENANT_FILTER_NAME = "tenantFilter";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // CAMBIO: Usar UUID
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);

            session.enableFilter(TENANT_FILTER_NAME)
                    .setParameter("tenantId", tenantId)
                    .validate();
        }

        try {
            chain.doFilter(request, response);
        } finally {
            if (tenantId != null) {
                Session session = entityManager.unwrap(Session.class);
                session.disableFilter(TENANT_FILTER_NAME);
            }
        }
    }
}