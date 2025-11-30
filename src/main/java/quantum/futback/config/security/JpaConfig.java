package quantum.futback.config.security;

import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import quantum.futback.core.multitenancy.TenantInterceptor;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class JpaConfig {

    private final TenantInterceptor tenantInterceptor;
    private final JpaVendorAdapter jpaVendorAdapter;
    private final JpaProperties jpaProperties;

    public JpaConfig(TenantInterceptor tenantInterceptor,
                     JpaVendorAdapter jpaVendorAdapter,
                     JpaProperties jpaProperties) {
        this.tenantInterceptor = tenantInterceptor;
        this.jpaVendorAdapter = jpaVendorAdapter;
        this.jpaProperties = jpaProperties;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setPackagesToScan("quantum.futback.entity");

        Map<String, Object> properties = (Map) jpaProperties.getProperties();

        properties.put("hibernate.session_factory.interceptor", tenantInterceptor);

        em.setJpaPropertyMap(properties);

        return em;
    }
}