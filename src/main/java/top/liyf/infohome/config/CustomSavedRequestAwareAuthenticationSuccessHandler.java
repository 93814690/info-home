package top.liyf.infohome.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import top.liyf.flyauthapi.bo.AuthUserBO;
import top.liyf.infohome.feign.AuthUserClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyf
 * Created in 2021-07-06
 */
@Component
public class CustomSavedRequestAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    final OAuth2AuthorizedClientService clientService;
    final AuthUserClient authUserClient;

    public CustomSavedRequestAwareAuthenticationSuccessHandler(OAuth2AuthorizedClientService clientService, AuthUserClient authUserClient) {
        this.clientService = clientService;
        this.authUserClient = authUserClient;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName());

        String accessToken = client.getAccessToken().getTokenValue();
        HttpSession session = request.getSession();
        session.setAttribute("accessToken", accessToken);

        AuthUserBO userBO = authUserClient.getUserBO("Bearer " + accessToken);
        System.out.println("userBO = " + userBO);

        if (userBO.getPermissions().isEmpty()) {
            System.out.println("is empty");
            // todo init permission
        }
        session.setAttribute("user", userBO);

        // update permission
        List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
        for (String permission : userBO.getPermissions()) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(permission);
            updatedAuthorities.add(authority);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                        SecurityContextHolder.getContext().getAuthentication().getCredentials(),
                        updatedAuthorities)
        );

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
