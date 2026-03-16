package dev.svenehrke.demo.core.config;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QuarkusProfile {

	/**
	 * Returns true if the current Quarkus profile is "dev".
	 */
	public boolean isDev() {
		return "dev".equals(System.getProperty("quarkus.profile", "prod"));
	}
}
