package com.codencanvas.ecommerce.oauth2;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.auth.AuthProvider;
import com.codencanvas.ecommerce.auth.dto.ProviderType;
import com.codencanvas.ecommerce.oauth2.userinfo.OAuth2UserInfo;
import com.codencanvas.ecommerce.user.Role;
import com.codencanvas.ecommerce.user.User;
import com.codencanvas.ecommerce.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {

        OAuth2User oAuth2User = super.loadUser(request);

        String registrationId = request.getClientRegistration()
                .getRegistrationId(); 

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory
                .getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();

            boolean alreadyLinked = authProviderRepository
                    .existsByUserAndProvider(user, ProviderType.valueOf(registrationId.toUpperCase()));

            if (!alreadyLinked) {
                linkProvider(user, userInfo, registrationId);
            }
        } else {
            user = createNewUser(userInfo);
            linkProvider(user, userInfo, registrationId);
        }

        return new UserPrincipal(user);
    }

    private User createNewUser(OAuth2UserInfo userInfo) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .fullName(userInfo.getName())
                .avatarUrl(userInfo.getAvatarUrl())
                .role(Role.CUSTOMER)
                .build();
        return userRepository.save(user);
    }

    private void linkProvider(User user, OAuth2UserInfo info, String registrationId) {
        AuthProvider provider = AuthProvider.builder()
                .user(user)
                .provider(ProviderType.valueOf(registrationId.toUpperCase()))
                .providerId(info.getId())
                .avatarUrl(info.getAvatarUrl())
                .build();
        authProviderRepository.save(provider);
    }
}
