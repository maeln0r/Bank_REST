package com.example.bankcards.unit;

import com.example.bankcards.AbstractIntegrationTest;
import com.example.bankcards.util.PanHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class PanHasherTest extends AbstractIntegrationTest {

    @Autowired
    PanHasher hasher;

    @Test
    @DisplayName("PAN fingerprint is deterministic, URL-safe and without padding")
    void panFingerprint() {
        String fp1 = hasher.fingerprint("1234567890123456");
        String fp2 = hasher.fingerprint("1234567890123456");
        assertThat(fp1).isEqualTo(fp2);
        assertThat(fp1).doesNotContain("=");
        assertThat(fp1).isEqualTo("O0g9HSOKFMm1D8zD3it-6lnObIqTahy4aKRVFxnMXbk");
    }
}