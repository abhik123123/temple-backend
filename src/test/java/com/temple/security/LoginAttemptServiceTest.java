package com.temple.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.temple.config.AppProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static AppProperties props() {
        return new AppProperties(
                new AppProperties.Security("secret", Duration.ofHours(8), "test", 3, Duration.ofMinutes(15)),
                new AppProperties.Cors(List.of()),
                new AppProperties.BootstrapAdmin(null, null, null, null),
                new AppProperties.Photos(5_242_880),
                new AppProperties.Newsletter("a@b.c", false, "0 0 2 * * SUN", "UTC", "http://localhost"));
    }

    @Test
    void locksOutAfterMaxFailuresAndUnlocksAfterWindow() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(props(), clock);

        for (int i = 0; i < 3; i++) {
            service.checkAllowed("Admin", "1.2.3.4");
            service.recordFailure("Admin", "1.2.3.4");
        }
        assertThatThrownBy(() -> service.checkAllowed("admin", "5.6.7.8"))
                .isInstanceOf(LoginBlockedException.class);

        clock.now = clock.now.plus(Duration.ofMinutes(16));
        assertThatCode(() -> service.checkAllowed("admin", "1.2.3.4")).doesNotThrowAnyException();
    }

    @Test
    void successResetsTheUserCounter() {
        LoginAttemptService service = new LoginAttemptService(props(), new MutableClock());
        service.recordFailure("admin", "1.2.3.4");
        service.recordFailure("admin", "1.2.3.4");
        service.recordSuccess("admin", "1.2.3.4");
        service.recordFailure("admin", "1.2.3.4");
        assertThatCode(() -> service.checkAllowed("admin", "1.2.3.4")).doesNotThrowAnyException();
    }
}
