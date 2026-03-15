package dev.svenehrke.demo.inbound.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/home")
public class HomeResource {

	@Inject
	Template home; // matches home.html

	@GET
	public TemplateInstance get() {
		return home
			.data("name", "Quarkus User")
			.data("items", java.util.List.of("Apple", "Banana", "Carrot"));
	}
}
