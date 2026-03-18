package dev.svenehrke.demo.inbound.web.infra.js;

import dev.svenehrke.demo.core.config.AppConfig;
import io.quarkus.runtime.Startup;
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
import java.util.function.Supplier;

@ApplicationScoped
@Startup
public class JsHolder {
	private final Engine engine;
	private SimplePool<JsConnection> jsConnectionPool;
	private final Logger log = LoggerFactory.getLogger(JsHolder.class);

	@Inject
	AppConfig appConfig;

	public JsHolder() {
		engine = Engine.create();
	}

	public void initPool() {
		log.info("initializing pool");
		jsConnectionPool = buildPool();
	}

	private SimplePool<JsConnection> buildPool() {
		var source = buildSource();
		return new SimplePool<>(
			Runtime.getRuntime().availableProcessors(),
			() -> new JsConnection(engine, source)
		);
	}

	public Supplier<SimplePool<JsConnection>> jsConnectionPoolSupplier() {
		if (jsConnectionPool == null) {
			jsConnectionPool = buildPool();
		}
		return () -> jsConnectionPool;
	}

	/**
	 * Reloads the JS bundle and rebuilds the pool.
	 * Called by JsBundleWatcher when the file changes.
	 */
	public synchronized Source buildSource() {
		try {
			log.info("Reloading JS-Code '{}'", appConfig.ssr().resource());
			InputStream is = getClass().getClassLoader().getResourceAsStream(appConfig.ssr().resource());
			String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			return Source.newBuilder("js", code, appConfig.ssr().resource()).build();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@PostConstruct
	public void init() {
	}
}
