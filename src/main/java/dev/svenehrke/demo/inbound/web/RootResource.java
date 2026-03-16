package dev.svenehrke.demo.inbound.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class RootResource {

	@GET
	public Response redirect() {
		return Response.seeOther(URI.create("/page")).build();
	}
}
