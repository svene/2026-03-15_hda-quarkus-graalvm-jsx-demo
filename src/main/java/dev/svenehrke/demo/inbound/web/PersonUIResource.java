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
 * {@link PersonActionResource}. Every GET route that renders an HTML fragment is
 * keyed by {@link JTSPersonRouteName}; most are dispatched through the generic
 * "/uiroute/{name}" endpoint, while routes needing parameters beyond
 * {@code id} get their own dedicated {@code @Path} method (e.g. {@link #personTable}).
 */
@Path("/uiroute")
public class PersonUIResource {

	@Inject
	JsxRenderer renderer;

	@Inject
	PeopleService peopleService;

	/**
	 * Handles every uiroute whose vm only ever depends on an (optional) {@code id}. A route
	 * needing different or additional parameters — like {@link #personTable} below — gets its
	 * own dedicated {@code @Path} method instead of growing this method's signature; JAX-RS
	 * matches the literal path first, so the two coexist without ambiguity.
	 */
	@GET
	@Path("/{name}") // Java-HONO
	@Produces(MediaType.TEXT_HTML)
	public String uiroute(@PathParam("name") String name, @QueryParam("id") Integer id) {
		JTSPersonRouteName route;
		try {
			route = JTSPersonRouteName.valueOf(name);
		} catch (IllegalArgumentException e) {
			throw new NotFoundException("Unknown uiroute: " + name);
		}
		Object vm = switch (route) {
			case Page -> new PersonPageModel(peopleService.personTableModel());
			case PersonDetails, PersonDetailsCard, PersonDetailsRow -> peopleService.personDetailModel(id);
			case PersonRow -> peopleService.personTableRowModel(id);
			case PersonEditor -> peopleService.personEditModel(id);
			default -> throw new IllegalStateException(route + " is served by its own dedicated endpoint, not " + getClass().getSimpleName() + "#uiroute");
		};
		return renderer.render(route, vm);
	}

	@GET
	@Path("/PersonTable") // Java-HONO
	@Produces(MediaType.TEXT_HTML)
	public String personTable(@QueryParam("search") String search) {
		return renderer.render(JTSPersonRouteName.PersonTable, peopleService.peopleForSearch(search));
	}
}
