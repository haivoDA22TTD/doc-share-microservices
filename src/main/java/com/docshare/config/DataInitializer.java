package com.docshare.config;

import com.docshare.entity.Role;
import com.docshare.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder()
                    .name(Role.RoleName.ROLE_USER)
                    .description("Default user role")
                    .build());
            
            roleRepository.save(Role.builder()
                    .name(Role.RoleName.ROLE_ADMIN)
                    .description("Administrator role")
                    .build());
            
            roleRepository.save(Role.builder()
                    .name(Role.RoleName.ROLE_MODERATOR)
                    .description("Moderator role")
                    .build());
        }
    }
}
