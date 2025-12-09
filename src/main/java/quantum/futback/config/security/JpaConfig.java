package quantum.futback.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import quantum.futback.core.multitenancy.TenantInterceptor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JpaConfig {

    private final TenantInterceptor tenantInterceptor;
    private final JpaVendorAdapter jpaVendorAdapter;
    private final JpaProperties jpaProperties;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.database-platform:}")
    private String databasePlatform;

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

        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());

        properties.put("hibernate.session_factory.interceptor", tenantInterceptor);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.show_sql", showSql);

        if (databasePlatform != null && !databasePlatform.isEmpty()) {
            properties.put("hibernate.dialect", databasePlatform);
        }

        em.setJpaPropertyMap(properties);

        return em;
    }
}