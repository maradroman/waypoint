package io.github.maradroman.waypointapi.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorEnvelopeTest {

    @Test
    @DisplayName("of creates envelope with error details")
    void of_createsEnvelopeWithErrorDetails() {
        var details = List.of("field1", "field2");
        var envelope = ErrorEnvelope.of("VALIDATION_ERROR", "Invalid input", details);

        assertThat(envelope.error()).isNotNull();
        assertThat(envelope.error().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(envelope.error().message()).isEqualTo("Invalid input");
        assertThat(envelope.error().details()).isEqualTo(details);
    }

    @Test
    @DisplayName("of creates envelope with null details")
    void of_createsEnvelopeWithNullDetails() {
        var envelope = ErrorEnvelope.of("NOT_FOUND", "Not found", null);

        assertThat(envelope.error()).isNotNull();
        assertThat(envelope.error().code()).isEqualTo("NOT_FOUND");
        assertThat(envelope.error().message()).isEqualTo("Not found");
        assertThat(envelope.error().details()).isNull();
    }
}
