package dev.svenehrke.demo.core.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app")
public interface AppConfig {

	Ssr ssr();

	interface Ssr {
		String resource();
	}
}
