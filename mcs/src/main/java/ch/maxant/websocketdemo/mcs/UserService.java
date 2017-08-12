package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.data.User;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    Logger logger;

    @Inject
    EntityManager em;

    @Inject
    private JMSContext context;

    @Resource(lookup = "/jms/queue/events")
    private Queue queue;

    public List<User> getAllUsingEntityManager(){
        List result = em.createNamedQuery(User.NQFindAll.NAME).getResultList();

        fire("read from DB");

        return result;
    }

    public void fire(String event){
        logger.info("Sending event to local JMS: " + event);
        context.createProducer().send(queue, event);
        logger.info("Sent event to local JMS");
    }

}
