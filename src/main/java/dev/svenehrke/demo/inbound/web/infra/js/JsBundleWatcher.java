package dev.svenehrke.demo.inbound.web.infra.js;

import dev.svenehrke.demo.core.config.DevConfig;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

@IfBuildProfile("dev")
@ApplicationScoped
@Startup
public class JsBundleWatcher {

	private static final Logger log = LoggerFactory.getLogger(JsBundleWatcher.class);

	@Inject
	DevConfig devConfig;

	@Inject
	JsBundleChangeHandler jsBundleChangeHandler;

	private long lastModified = -1;

	@Scheduled(every = "1s")
	void checkFile() throws Exception {
		Path file = Path.of(devConfig.ssr().filename());

		if (!Files.exists(file)) {
			return;
		}

		long current = Files.getLastModifiedTime(file).toMillis();

		if (current != lastModified) {
			lastModified = current;
			log.info("SSR bundle changed → notifying handler");
			jsBundleChangeHandler.run();
		}
	}
}
