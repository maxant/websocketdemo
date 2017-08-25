package ch.maxant.websocketdemo.mcs;

import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
public class AroIntegrationService {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @Inject
    @ARO
    WebTarget client;

    public void createAroTask(String idempotencyId, long caseNr, String textForTask){
        Response response = null;
        try {
            response = client
                    .path("aro/tasks/create")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .buildPost(Entity.json(new AroTask(caseNr, textForTask)))
                    .invoke();
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new RuntimeException("Unable to create ARO Task " + idempotencyId + ". Return code was " + response.getStatus());
            }
        }finally{
            if(response != null){
                response.close(); //very important!
            }
        }
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
