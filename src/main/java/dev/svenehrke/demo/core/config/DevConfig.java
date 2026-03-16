package dev.svenehrke.demo.core.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dev")
public interface DevConfig {

	Ssr ssr();

	interface Ssr {
		String filename();
	}
}
