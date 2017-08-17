package ch.maxant.websocketdemo.b2e;

import ch.maxant.websocketdemo.b2e.data.Model;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/events")
@ApplicationScoped
public class EventResource {

    @Inject
    Model model;

    @GET
    @Path("/all")
    @Produces("application/json")
    public Response getAll() {
        return Response.ok(model.getEvents()).build();
    }

}
