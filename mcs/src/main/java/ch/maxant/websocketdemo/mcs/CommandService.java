package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.data.Command;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Singleton
@Startup
public class CommandService {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @Resource
    TimerService timerService;

    @PostConstruct
    public void init(){
        timerService.createIntervalTimer(5000L, 5000L, new TimerConfig(null, false));
    }

    @Timeout
    public void processCommands(){
        select anything with no InProgress or with a timedout inProgress
        set inProgress to now
        commit each one individually
        for thos with no optimisiticLockException => process and then delete if successful
        if fail to save, no problem, as service provider must be idempotent
        why all this complex stuff? coz when there are multiple MCS running, we only want each
                command processed once.
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processCommandsImmediately(@Observes(during = TransactionPhase.AFTER_SUCCESS) Command command){

        TODO call an async service so this doesnt block the response to the UI

        command = em.find(Command.class, command.getId(), LockT); //load it fresh
        command.setInProgress
    }

}
