package quantum.futback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import quantum.futback.core.multitenancy.TenantAware;

import java.util.UUID;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Entity
@Table(name = "teams")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Team implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "is_own_team")
    private Boolean isOwnTeam = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // --- Getters y Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getIsOwnTeam() {
        return isOwnTeam;
    }

    public void setIsOwnTeam(Boolean isOwnTeam) {
        this.isOwnTeam = isOwnTeam;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}