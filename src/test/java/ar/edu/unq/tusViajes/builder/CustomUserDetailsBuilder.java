package ar.edu.unq.tusViajes.builder;

import java.util.Collection;

import ar.edu.unq.tusViajes.security.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

public class CustomUserDetailsBuilder {

    private Long id = 1L;
    private String email = "user@example.com";
    private String password = "password";
    private Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
    private boolean enabled = true;

    public static CustomUserDetailsBuilder aUserDetails() {
        return new CustomUserDetailsBuilder();
    }

    public CustomUserDetailsBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CustomUserDetailsBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CustomUserDetailsBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public CustomUserDetailsBuilder withAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
        return this;
    }

    public CustomUserDetailsBuilder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public CustomUserDetails build() {
        return new CustomUserDetails(id, email, password, authorities, enabled);
    }
}