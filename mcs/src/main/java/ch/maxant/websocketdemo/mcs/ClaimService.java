package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.data.Case;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

@Stateless
public class ClaimService {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @Inject
    private JMSContext context;

    @Resource(name = "java:/jms/queue/events")
    private Queue queue;

    @Inject
    AroService aroService;

    public Case getCase(Long nr) {
        try{
            return em.createNamedQuery(Case.NQFindByNumber.NAME, Case.class)
                    .setParameter(Case.NQFindByNumber.PARAM_NR, nr)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
    }

    public void mergeCase(Case insuranceCase) throws JMSException {
        em.merge(insuranceCase);

        aroService.createTask(insuranceCase.getNr(), "Some text telling the user what to do...");

        fire("MODIFIED_CASE", insuranceCase.getNr());
    }

    private void fire(String event, long nr) throws JMSException {
        //use local JMS to create non-transactional kafka into a transactional resource
        //TODO use kafkas tx API so we don't have to bother with JMS
        //TODO JMS is no good anyway => if we deploy in docker, we'd possibly overwrite local queue! see "command" solution below
        logger.info("Sending event to local JMS: " + event);
        Message msg = context.createTextMessage(event);
        msg.setStringProperty("context", "" + nr);
        context.createProducer().send(queue, msg);
        logger.info("Sent event to local JMS");
    }

}
