package dev.svenehrke.demo.inbound.web.infra.js;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.Startup;
import io.vertx.core.http.ServerWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.vertx.web.Route;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@IfBuildProfile("dev")
@ApplicationScoped
@Startup
public class DevReloadRoute {

	private final Set<ServerWebSocket> sockets = ConcurrentHashMap.newKeySet();

	@Route(path = "/dev-reload", methods = Route.HttpMethod.GET)
	void handle(io.vertx.ext.web.RoutingContext ctx) {
		ctx.request().toWebSocket(ar -> {
			if (ar.succeeded()) {
				var ws = ar.result();
				sockets.add(ws);
				ws.closeHandler(v -> sockets.remove(ws));
			}
		});
	}

	public void broadcastReload() {
		sockets.forEach(ws -> {
			if (!ws.isClosed()) {
				ws.writeTextMessage("reload");
			}
		});
	}
}
