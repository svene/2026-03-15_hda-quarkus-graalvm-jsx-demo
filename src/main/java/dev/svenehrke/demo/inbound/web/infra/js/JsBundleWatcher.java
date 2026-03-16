package dev.svenehrke.demo.inbound.web.infra.js;

import dev.svenehrke.demo.core.config.AppConfig;
import dev.svenehrke.demo.core.config.DevConfig;
import dev.svenehrke.demo.core.config.QuarkusProfile;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.nio.file.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.arc.profile.IfBuildProfile;

@ApplicationScoped
@IfBuildProfile("dev")
public class JsBundleWatcher {

	@Inject
	QuarkusProfile profile;

	@Inject
	DevConfig devConfig;

    private final JsxRenderer jsxRenderer;

	private WatchService watchService;
	private Thread watchThread;

    public JsBundleWatcher(JsxRenderer jsxRenderer) {
        this.jsxRenderer = jsxRenderer;
	}

	@PostConstruct
	public void start() throws IOException {

		if (!profile.isDev()) {
			return;
		}

		Path file = Path.of(devConfig.ssr().filename());
		Path dir = file.getParent();

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
				if (changed.equals(fileName)) {
                    jsxRenderer.reloadBundle();
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
