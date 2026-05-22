package com.codencanvas.ecommerce.user;


import java.util.ArrayList;
import java.util.List;

import com.codencanvas.ecommerce.auth.AuthProvider;
import com.codencanvas.ecommerce.auth.dto.ProviderType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String fullName;

    @Column(name = "avatar_url", nullable = true)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled = true;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuthProvider> providers = new ArrayList<>();

    // helper method
    public boolean hasProvider(ProviderType type) {
        return providers.stream()
                .anyMatch(p -> p.getProvider() == type);
    }

}
