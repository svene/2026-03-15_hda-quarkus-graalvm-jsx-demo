package dev.svenehrke.demo.inbound.web;

import dev.svenehrke.demo.core.PeopleService;
import dev.svenehrke.demo.inbound.web.form.PersonEditForm;
import dev.svenehrke.demo.inbound.web.infra.js.JsxRenderer;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.svenehrke.demo.inbound.web.*;

import java.util.List;

@Path("/")
public class PersonResource {

	@Inject
	Template home; // matches home.html

	@Inject
	JsxRenderer renderer;

	@Inject
	PeopleService peopleService;

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
		var vm = new PersonPageModel(peopleService.personTableModel());
		return renderer.render("renderPage", vm);
	}

	@GET
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.PERSON_DETAILS)
	@Produces(MediaType.TEXT_HTML)
	public String details(@PathParam("id") int id) {
		PersonDetailModel vm = peopleService.personDetailModel(id);
		return renderer.render("personDetails", vm);
	}

	@GET
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.PERSON_ROW)
	@Produces(MediaType.TEXT_HTML)
	public String row(@PathParam("id") int id) {
		PersonTableRowModel vm = peopleService.personTableRowModel(id);
		return renderer.render("personRow", vm);
	}

	@GET
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.PERSON_EDIT)
	@Produces(MediaType.TEXT_HTML)
	public String edit(@PathParam("id") int id) {
		PersonEditModel vm = peopleService.personEditModel(id);
		return renderer.render("personEdit", vm);
	}

	@GET
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.PERSON_DETAILS_CARD)
	@Produces(MediaType.TEXT_HTML)
	public String detailsCard(@PathParam("id") int id) {
		PersonDetailModel vm = peopleService.personDetailModel(id);
		return renderer.render("personDetailsCard", vm);
	}

	@PUT
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.PERSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updatePerson(@PathParam("id") int id, @BeanParam PersonEditForm personEditForm) {
		peopleService.updatePerson(id, personEditForm.toModel(id));
		return Response
			.ok()
			.header(
				HTMXConsts.HX_TRIGGER,
				"""
					{"%s": {"id": %d}}\
					""".formatted("personUpdated", id)
			)
			.build();
	}
	@GET
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.PERSON_DETAILS_ROW)
	@Produces(MediaType.TEXT_HTML)
	public String detailsRow(@PathParam("id") int id) {
		PersonDetailModel vm = peopleService.personDetailModel(id);
		return renderer.render("personDetailsRow", vm);
	}

	@GET
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.PERSON_TABLE)
	@Produces(MediaType.TEXT_HTML)
	public String peopleUrl(@QueryParam("search") String search) {
		PersonTableModel vm = peopleService.peopleForSearch(search);
		return renderer.render("personTable", vm);
	}
	@DELETE
	@Path(HonoWebApiSharedConsts.HonoWebApiConsts.DELETE)
	public Response deleteRows(@QueryParam("selection") List<Integer> selection) {
		peopleService.deleteByIds(selection);
		return Response
			.ok()
			.header(HTMXConsts.HX_REDIRECT, HonoWebApiSharedConsts.HonoWebApiConsts.PAGE)
			.build();

	}

}
