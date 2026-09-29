package ar.edu.unq.tusViajes.util;

import ar.edu.unq.tusViajes.model.User;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public class TestSecurityUtils {

    public static RequestPostProcessor withCustomUserDetails(User user) {
        return user(new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                createAuthorityList("ROLE_" + user.getRole().name()),
                user.isActive()
        ));
    }
}
