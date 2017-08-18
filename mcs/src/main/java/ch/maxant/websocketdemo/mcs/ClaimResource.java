package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.data.Case;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.ws.rs.*;

@Path("/cases")
@ApplicationScoped
public class ClaimResource {

    @Inject
    ClaimService service;

    @GET
    @Path("case/{nr}")
    @Produces("application/json")
    public Case getCase(@PathParam("nr") Long nr) {
        return service.getCase(nr);
    }

    @PUT
    @Path("case")
    @Produces("application/json")
    public void putCase(Case insuranceCase) throws JMSException {
        service.mergeCase(insuranceCase);
    }

}
