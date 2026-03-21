package dev.svenehrke.demo.inbound.web;

import dev.svenehrke.demo.inbound.web.infra.js.JsxRenderer;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.svenehrke.demo.inbound.web.PersonPageModel;
import org.svenehrke.demo.inbound.web.PersonTableModel;
import org.svenehrke.demo.inbound.web.PersonTableRowModel;

import java.util.List;

@Path("/")
public class PersonResource {

	@Inject
	Template home; // matches home.html

	@Inject
	JsxRenderer renderer;

	@GET
	@Path("/home")
	public TemplateInstance home() {
		return home
			.data("name", "Quarkus User")
			.data("items", java.util.List.of("Apple", "Banana", "Carrot"));
	}

	@GET
	@Path("/page")
	@Produces(MediaType.TEXT_HTML)
	public String page() {
		var vm = new PersonPageModel(
			new PersonTableModel(List.of(
				new PersonTableRowModel(
					1, "Sven", "Ehrke", "some street 5"
				)
			),
				1
			)
		);
		return renderer.render("renderPage", vm);
	}
}
