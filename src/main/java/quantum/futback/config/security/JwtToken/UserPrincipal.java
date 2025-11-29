package quantum.futback.config.security.JwtToken;

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
    private String email;
    private String password;
    private UUID tenantId;
    private String roleName;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String email, String password, UUID tenantId, String roleName, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.tenantId = tenantId;
        this.roleName = roleName;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getName())
        );

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getTenant().getId(),
                user.getRole().getName(),
                authorities
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getRoleName() {
        return roleName;
    }

    // Métodos de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
