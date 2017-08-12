package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.data.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

@Path("/")
@Stateless
public class UserResource {

    @Inject
    private UserService service;

    @GET
    @Path("all")
    @Produces("application/json")
    public List<User> getAllWithEm() {
        return service.getAllUsingEntityManager();
    }

}
