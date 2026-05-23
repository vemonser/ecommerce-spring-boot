package com.codencanvas.ecommerce.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.user.model.AuthProvider;
import com.codencanvas.ecommerce.user.model.ProviderType;
import com.codencanvas.ecommerce.user.model.User;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {

    boolean existsByUserAndProvider(User user, ProviderType provider);
    
}
