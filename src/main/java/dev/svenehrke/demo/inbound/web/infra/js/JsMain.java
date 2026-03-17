package dev.svenehrke.demo.inbound.web.infra.js;

import dev.svenehrke.demo.core.config.AppConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

//@ApplicationScoped
public class JsMain {

	private final SimplePool<JsConnection> jsConnectionPool;
	private final Engine engine;
	private volatile Source source;
	private final Logger log = LoggerFactory.getLogger(JsMain.class);

	@Inject
	AppConfig appConfig;

	public JsMain() {
		engine = Engine.create();

		int poolSize = Runtime.getRuntime().availableProcessors();
		jsConnectionPool = new SimplePool<>(poolSize, () -> new JsConnection(engine, source));
	}

	@PostConstruct
	public void init() {
		reloadBundle();
	}

	/**
	 * Reloads the JS bundle and rebuilds the pool.
	 * Called by JsBundleWatcher when the file changes.
	 */
	public synchronized void reloadBundle() {
		try {
			log.info("Reloading JS-Code");
			InputStream is = getClass().getClassLoader().getResourceAsStream(appConfig.ssr().resource());
			String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//			Path filePath = Path.of("target/classes/static", appConfig.ssr().resource());
//			source = Source.newBuilder("js", code, filePath.toString()).build();
			source = Source.newBuilder("js", code, appConfig.ssr().resource()).build();
//			jsConnectionPool.reset();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
