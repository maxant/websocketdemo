package ch.maxant.websocketdemo.mcs.framework.commands;

import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class CommandRepository {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Command> lockCommands(int batchSize) {
        // We need to lock some rows so that no other instances of this microservice try
        // to handle them.
        //
        // https://stackoverflow.com/questions/20091527/select-only-unlocked-rows-mysql

        List<Command> commands = em.createNamedQuery(Command.NQSelectAllAvailable.NAME, Command.class)
                .setMaxResults(batchSize)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE) //for update = locked until commit
                .getResultList();

        commands.forEach(c -> {
            c.lock();
        });

        return commands;
    }

    public void create(Command command) {
        em.persist(command);
    }

    public void resetLockAfterFailure(Command command) {
        command = em.find(Command.class, command.getId());
        command.resetLocked();
        command.incrementAttempts();
    }

    public void delete(Command command) {
        command = em.find(Command.class, command.getId());
        em.remove(command);
    }

    public int unlockCommands(long timeout) {
        LocalDateTime timeoutTime = LocalDateTime.now().minus(Duration.ofMillis(timeout));
        return em.createNamedQuery(Command.NQSelectLocked.NAME, Command.class)
                .setParameter(1, timeoutTime) //eg anything before 30 seconds ago, which is the same as anything older than 30 seconds
                .getResultList()
                .stream()
                .peek(c -> c.resetLocked())
                .mapToInt(c->1)
                .sum();
    }
}
