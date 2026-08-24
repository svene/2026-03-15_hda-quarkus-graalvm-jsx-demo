package dev.svenehrke.demo.inbound.web;

import dev.svenehrke.demo.core.PeopleService;
import dev.svenehrke.demo.inbound.web.infra.js.JsxRenderer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Component URLs — a separate concept from the REST-ish mutation endpoints in
 * {@link PersonActionResource}. Every GET route that renders a JSX fragment is
 * dispatched through this single "/uiroute/{name}" endpoint, keyed by
 * {@link JTSPersonRouteName}, instead of getting its own {@code @Path}.
 */
@Path("/uiroute")
public class PersonUIResource {

	@Inject
	JsxRenderer renderer;

	@Inject
	PeopleService peopleService;

	@GET
	@Path("/{name}")
	@Produces(MediaType.TEXT_HTML)
	public String uiroute(@PathParam("name") String name, @QueryParam("id") Integer id, @QueryParam("search") String search) {
		JTSPersonRouteName route;
		try {
			route = JTSPersonRouteName.valueOf(name);
		} catch (IllegalArgumentException e) {
			throw new NotFoundException("Unknown uiroute: " + name);
		}
		Object vm = switch (route) {
			case page -> new PersonPageModel(peopleService.personTableModel());
			case personDetails, personDetailsCard, personDetailsRow -> peopleService.personDetailModel(id);
			case personTable -> peopleService.peopleForSearch(search);
			case personRow -> peopleService.personTableRowModel(id);
			case personEdit -> peopleService.personEditModel(id);
		};
		return renderer.render(route, vm);
	}
}
