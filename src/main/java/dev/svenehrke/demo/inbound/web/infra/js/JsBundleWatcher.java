package dev.svenehrke.demo.inbound.web.infra.js;

import dev.svenehrke.demo.core.config.DevConfig;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.nio.file.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.arc.profile.IfBuildProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@IfBuildProfile("dev")
@Startup
public class JsBundleWatcher {

	@Inject
	DevConfig devConfig;

	@Inject
	JsHolder jsHolder;

	private final Logger log = LoggerFactory.getLogger(JsBundleWatcher.class);

	private WatchService watchService;
	private Thread watchThread;

	@PostConstruct
	public void start() throws IOException {
		log.info("Starting JsBundleWatcher");

		Path file = Path.of(devConfig.ssr().filename());
		Path dir = file.getParent();
		log.info("dir: {}", dir.toString());

		watchService = FileSystems.getDefault().newWatchService();

		dir.register(
			watchService,
			StandardWatchEventKinds.ENTRY_MODIFY,
			StandardWatchEventKinds.ENTRY_CREATE
		);

		watchThread = new Thread(this::watchLoop, "js-bundle-watcher");
		watchThread.setDaemon(true);
		watchThread.start();
	}

	private void watchLoop() {
		Path fileName = Path.of(devConfig.ssr().filename());
		while (!Thread.currentThread().isInterrupted()) {
			WatchKey key;
			try {
				key = watchService.take();
			}
			catch (InterruptedException e) {
				return;
			}
			for (WatchEvent<?> event : key.pollEvents()) {
				Path changed = (Path) event.context();
				if (changed.equals(fileName.getFileName())) {
					log.info("calling initpool");
					jsHolder.initPool();
				} else {
					log.info("NOT calling initpool");
				}
			}
			key.reset();
		}
	}

	@PreDestroy
	public void stop() throws IOException {
		if (watchService != null) {
			watchService.close();
		}
		if (watchThread != null) {
			watchThread.interrupt();
		}
	}
}
