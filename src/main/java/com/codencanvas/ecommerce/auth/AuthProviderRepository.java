package com.codencanvas.ecommerce.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.auth.dto.ProviderType;
import com.codencanvas.ecommerce.user.User;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {

    boolean existsByUserAndProvider(User user, ProviderType provider);
    
}
