package dev.svenehrke.demo.inbound.web.infra.js;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JsxRenderer {

	private final Logger log = LoggerFactory.getLogger(JsxRenderer.class);

	@Inject
	JsHolder jsHolder;

	public String render(String route, Object vm) {
		log.info("rendering {}", route);
		JsConnection ctx = null;
		try {
			String vmJson = JsonUtil.toJson(vm);
			ctx = jsHolder.jsConnectionPoolSupplier().get().borrow();
			var result = ctx.getEntryFunction("render").execute(route, vmJson);
			return result.asString();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		finally {
			log.info("finished rendering {}", route);
			if (ctx != null) {
				jsHolder.jsConnectionPoolSupplier().get().release(ctx);
			}
		}
	}
}
