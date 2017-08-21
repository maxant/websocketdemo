package ch.maxant.websocketdemo.aro;

import ch.maxant.websocketdemo.aro.data.Task;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TaskService {

    @PersistenceContext
    EntityManager em;

    @Inject
    Logger logger;

    @Inject
    EventService eventService;

    public List<Task> getCases(Long caseNr) {
        return em.createNamedQuery(Task.NQFindByCaseNumber.NAME, Task.class)
                .setParameter(Task.NQFindByCaseNumber.PARAM_NR, caseNr)
                .getResultList();
    }

    public Task create(Task task) {
        em.persist(task);
        eventService.fire(task.getCaseNr(), "CREATED_TASK");
        return task;
    }

}
