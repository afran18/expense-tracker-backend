package com.afran.expense.commons;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CommonsModuleHealthConfiguration {

	@Bean
	HealthIndicator commonsModuleHealthIndicator() {
		return () -> Health.up()
				.withDetail("module", "commons-module")
				.withDetail("verification", "Loaded from the shared commons-module JAR")
				.build();
	}
}