package com.administrativetool.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void userBuilder_shouldCreateUserWithAllFields() {
        var user = User.builder()
                .id(1L)
                .username("admin")
                .password("encodedPassword")
                .role("ADMIN")
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void userNoArgsConstructor_shouldCreateEmptyUser() {
        var user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isNull();
    }

    @Test
    void userAllArgsConstructor_shouldCreateUserWithAllFields() {
        var user = User.builder()
                .id(1L)
                .username("admin")
                .password("encodedPassword")
                .role("ADMIN")
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void userSetters_shouldUpdateFields() {
        var user = new User();
        user.setId(2L);
        user.setUsername("user");
        user.setPassword("password");
        user.setRole("USER");

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getRole()).isEqualTo("USER");
    }
}
