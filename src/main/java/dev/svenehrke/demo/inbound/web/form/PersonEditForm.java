package dev.svenehrke.demo.inbound.web.form;

import jakarta.ws.rs.FormParam;
import org.svenehrke.demo.inbound.web.PersonEditModel;

public class PersonEditForm {

	@FormParam("id")
	public int id;

	@FormParam("firstName")
	public String firstName;

	@FormParam("lastName")
	public String lastName;

	@FormParam("streetName")
	public String streetName;

	public PersonEditModel toModel(int idFromPath) {
		return new PersonEditModel(idFromPath, firstName, lastName, streetName);
	}}
