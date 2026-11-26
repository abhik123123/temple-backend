package com.temple.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PublicNameTest {

    @Test
    void showsFirstNameAndSurnameInitialOnly() {
        assertThat(EventRegistrationService.publicName(" Ravi ", "kumar")).isEqualTo("Ravi K.");
        assertThat(EventRegistrationService.publicName("Lakshmi", null)).isEqualTo("Lakshmi");
        assertThat(EventRegistrationService.publicName("Lakshmi", "  ")).isEqualTo("Lakshmi");
        assertThat(EventRegistrationService.publicName("రవి", "కుమార్")).isEqualTo("రవి క.");
    }
}
