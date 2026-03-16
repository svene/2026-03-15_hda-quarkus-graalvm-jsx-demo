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
import java.nio.file.Path;

@ApplicationScoped
public class JsxRenderer {

	@Inject
	AppConfig appConfig;

	private final JsContextPool jsContextPool;

	private final Engine engine;
	private volatile Source source;
	private final Logger log = LoggerFactory.getLogger(JsxRenderer.class);


	public JsxRenderer() {
		engine = Engine.create();

		int poolSize = Runtime.getRuntime().availableProcessors();
		jsContextPool = new JsContextPool(poolSize, this::buildJsInitializer);
	}

	@PostConstruct
	public void init() {
		reloadBundle();
	}

	private JsInitializer buildJsInitializer() {
		return new JsInitializer(engine, source);
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
			jsContextPool.reset();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String render(String entryFunctionName, Object vm) {
		JsInitializer ctx = null;
		try {
			ctx = jsContextPool.borrow();
			String vmJson = JsonUtil.toJson(vm);
			var result = ctx.getEntryFunction(entryFunctionName).execute(vmJson);
			return result.asString();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		finally {
			if (ctx != null) {
				jsContextPool.release(ctx);
			}
		}
	}
}
