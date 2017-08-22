package ch.maxant.websocketdemo.mcs;

import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
public class AroIntegrationService {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @Inject
    Client client;

    public void createAroTask(String idempotencyId, long caseNr, String textForTask){
        Response response = client.target(System.getProperty("aro.url"))
                .path("/tasks/create")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.json(new AroTask(caseNr, textForTask)))
                .invoke();
        if(response.getStatus() != Response.Status.CREATED.getStatusCode()){
            throw new RuntimeException("Unable to create ARO Task " + idempotencyId + ". Return code was " + response.getStatus());
        }
        response.close(); //very important!
    }

    public static final class AroTask {
        private long caseNr;
        private String description;

        public AroTask(long caseNr, String description){
            this.caseNr = caseNr;
            this.description = description;
        }
    }

}
