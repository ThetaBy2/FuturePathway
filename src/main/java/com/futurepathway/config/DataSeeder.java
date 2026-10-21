package com.futurepathway.config;

import com.futurepathway.entity.AppUser;
import com.futurepathway.enums.Role;
import com.futurepathway.repository.SkillRepository;
import com.futurepathway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the bootstrap portal ADMIN on first startup (only when no users exist). */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name:Portal Admin}")
    private String adminName;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            AppUser admin = AppUser.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Seeded portal ADMIN user: {}", adminEmail);
        } else {
            log.info("Users already exist ({}), skipping admin seed", userRepository.count());
        }
        log.info("Skill catalog size: {}", skillRepository.count());
    }
}
