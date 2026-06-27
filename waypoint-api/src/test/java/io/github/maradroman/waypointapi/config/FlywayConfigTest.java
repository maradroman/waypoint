package io.github.maradroman.waypointapi.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlywayConfigTest {

    @Mock
    private DataSource dataSource;

    @Test
    @DisplayName("flyway bean creates configured Flyway instance")
    void flyway_createsConfiguredInstance() {
        var config = new FlywayConfig();
        var flyway = config.flyway(dataSource);

        assertThat(flyway).isNotNull();
        assertThat(flyway.getConfiguration().getDataSource()).isNotNull();
    }
}
