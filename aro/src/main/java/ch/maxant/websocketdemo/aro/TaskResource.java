package ch.maxant.websocketdemo.aro;

import ch.maxant.websocketdemo.aro.data.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
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
    public Task create(Task task) {
        return service.create(task);
    }

}
