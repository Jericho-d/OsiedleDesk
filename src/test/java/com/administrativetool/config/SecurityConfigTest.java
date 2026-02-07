package com.administrativetool.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoderBean_shouldBeAvailable() {
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    void passwordEncoder_shouldBeBCrypt() {
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder.getClass().getSimpleName()).isEqualTo("BCryptPasswordEncoder");
    }

    @Test
    void passwordEncoder_shouldEncodePassword() {
        var rawPassword = "testPassword";
        var encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
    }

    @Test
    void passwordEncoder_shouldMatchPasswords() {
        var rawPassword = "admin";
        var encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder).matches(rawPassword, encodedPassword);
        assertThat(passwordEncoder).doesNotMatch("wrong", encodedPassword);
    }
}
