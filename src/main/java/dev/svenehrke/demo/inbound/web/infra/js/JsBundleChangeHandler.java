package dev.svenehrke.demo.inbound.web.infra.js;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JsBundleChangeHandler implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(JsBundleChangeHandler.class);

	@Inject
	JsHolder jsHolder;

	@Inject
	DevReloadSSE reload;

	@Override
	public void run() {
		log.info("SSR bundle changed → reloading");
		jsHolder.initPool();
		reload.broadcastReload(); // notify browser about ssr.js change
	}
}
