package com.codencanvas.ecommerce.oauth2.service;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.common.exception.OAuth2ProcessingException;
import com.codencanvas.ecommerce.oauth2.userinfo.OAuth2UserInfo;
import com.codencanvas.ecommerce.oauth2.userinfo.OAuth2UserInfoFactory;
import com.codencanvas.ecommerce.user.model.AuthProvider;
import com.codencanvas.ecommerce.user.model.ProviderType;
import com.codencanvas.ecommerce.user.model.Role;
import com.codencanvas.ecommerce.user.model.User;
import com.codencanvas.ecommerce.user.repository.AuthProviderRepository;
import com.codencanvas.ecommerce.user.repository.UserRepository;
import com.codencanvas.ecommerce.user.security.UserPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory
                .getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // 1. التحقق من الـ email
        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            log.error("OAuth2 provider {} did not return email for user {}",
                    registrationId, userInfo.getId());
            throw new OAuth2ProcessingException(
                    "Email not available from " + registrationId
                            + ". Please make your email public or use email/password login.");
        }

        // 2. دور على الـ user بالـ email
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Existing user {} logging in via {}", user.getEmail(), registrationId);

            // لو الـ user كان مسجّل بالإيميل/باسورد ومش متفعّل، فعّله (لأنه OAuth2 =
            // verified)
            if (!user.isEnabled()) {
                user.setEnabled(true);
                userRepository.save(user);
            }

            boolean alreadyLinked = authProviderRepository
                    .existsByUserAndProvider(user, ProviderType.valueOf(registrationId.toUpperCase()));

            if (!alreadyLinked) {
                linkProvider(user, userInfo, registrationId);
            }
        } else {
            // 3. إنشاء user جديد
            user = createNewOAuthUser(userInfo, registrationId);
            linkProvider(user, userInfo, registrationId);
            log.info("New user {} created via OAuth2 {}", user.getEmail(), registrationId);
        }

        return new UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User createNewOAuthUser(OAuth2UserInfo userInfo, String registrationId) {
        // generate username من الـ email أو name عشان الـ column مش بيقبل null
        String generatedUsername = generateUsername(userInfo);

        User user = User.builder()
                .email(userInfo.getEmail())
                .username(generatedUsername)
                .fullName(userInfo.getName())
                .avatarUrl(userInfo.getAvatarUrl())
                .role(Role.CUSTOMER)
                .enabled(true) // <-- OAuth2 = email verified by Google/GitHub
                .build();

        return userRepository.save(user);
    }

    private String generateUsername(OAuth2UserInfo userInfo) {
        // جرّب تستخدم الجزء قبل @ في الإيميل
        String base = userInfo.getEmail().split("@")[0]
                .replaceAll("[^a-zA-Z0-9_]", "") // <-- نضف أي حاجة غير alphanumeric
                .toLowerCase();

        String username = base;
        int suffix = 1;

        // لو الـ username موجود، ضيف رقم وراه
        while (userRepository.existsByUsername(username)) {
            username = base + suffix;
            suffix++;
        }

        return username;
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
