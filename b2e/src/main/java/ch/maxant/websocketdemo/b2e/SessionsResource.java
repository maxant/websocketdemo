package ch.maxant.websocketdemo.b2e;

import ch.maxant.websocketdemo.b2e.data.Model;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static java.util.stream.Collectors.toList;

@Path("/sessions")
@ApplicationScoped
public class SessionsResource {

    @Inject
    Model model;

    @GET
    @Path("/all")
    @Produces("application/json")
    public Response getAll() {
        return Response
                .ok(model.getSessions().stream()
                        .map(s->s.getId() + "::" + s.getUserProperties().get(WebSocketEndpoint.CONTEXT))
                        .collect(toList()))
                .build();
    }

}
