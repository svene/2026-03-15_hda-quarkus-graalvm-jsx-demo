package dev.svenehrke.demo.inbound.web.infra.js;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@ApplicationScoped
public class JsxRenderer {

	@Inject
	JsHolder jsHolder;

	@Inject
	JsBundleWatcher watcher;

	private final Logger log = LoggerFactory.getLogger(JsxRenderer.class);

	//	private final Supplier<SimplePool<JsConnection>> jsConnectionPoolSupplier;

/*
	public JsxRenderer(
		Supplier<SimplePool<JsConnection>> jsConnectionPoolSupplier
	) {
		this.jsConnectionPoolSupplier = jsConnectionPoolSupplier;
	}
*/

	public String render(String entryFunctionName, Object vm) {
		JsConnection ctx = null;
		try {
			String vmJson = JsonUtil.toJson(vm);
			ctx = jsHolder.jsConnectionPoolSupplier().get().borrow();
			var result = ctx.getEntryFunction(entryFunctionName).execute(vmJson);
			return result.asString();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		finally {
			if (ctx != null) {
				jsHolder.jsConnectionPoolSupplier().get().release(ctx);
			}
		}
	}
}
