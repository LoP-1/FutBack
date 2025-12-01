package quantum.futback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import quantum.futback.core.multitenancy.TenantAware;

import java.time.LocalDate;
import java.util.UUID;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Entity
@Table(name = "players")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Player implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = true)
    private User parentUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_primary_id", nullable = true)
    private Position positionPrimary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_secondary_id", nullable = true)
    private Position positionSecondary;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "dni", length = 20)
    private String dni;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "dominant_foot", length = 10)
    private String dominantFoot;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public User getParentUser() {
        return parentUser;
    }

    public void setParentUser(User parentUser) {
        this.parentUser = parentUser;
    }

    public Position getPositionPrimary() {
        return positionPrimary;
    }

    public void setPositionPrimary(Position positionPrimary) {
        this.positionPrimary = positionPrimary;
    }

    public Position getPositionSecondary() {
        return positionSecondary;
    }

    public void setPositionSecondary(Position positionSecondary) {
        this.positionSecondary = positionSecondary;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDominantFoot() {
        return dominantFoot;
    }

    public void setDominantFoot(String dominantFoot) {
        this.dominantFoot = dominantFoot;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}