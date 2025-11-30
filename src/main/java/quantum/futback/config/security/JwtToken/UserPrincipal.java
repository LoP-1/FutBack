package quantum.futback.config.security.JwtToken;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import quantum.futback.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    private UUID id;
    private Long tenantId;
    private String email;
    private String roleName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, Long tenantId, String email, String roleName, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.roleName = roleName;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getName())
        );

        return new UserPrincipal(
                user.getId(),
                user.getTenantId(), // <-- AQUÍ ESTABA EL ERROR: Usamos getTenantId() directo
                user.getEmail(),
                user.getRole().getName(),
                user.getPasswordHash(),
                authorities
        );
    }

    public UUID getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}