package ch.maxant.websocketdemo.aro;

import ch.maxant.websocketdemo.aro.data.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/tasks")
@ApplicationScoped
public class TaskResource {

    @Inject
    TaskService service;

    @GET
    @Path("/{caseNr}")
    @Produces("application/json")
    public List<Task> getTasks(@PathParam("caseNr") Long caseNr) {
        return service.getCases(caseNr);
    }

    @POST
    @Path("/create")
    @Produces("application/json")
    public Response create(Task task) {
        task = service.create(task);
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

}
