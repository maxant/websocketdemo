package ch.maxant.websocketdemo.mcs.framework.commands;

import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
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
            c.setLocked(System.currentTimeMillis());
        });

        return commands;
    }

    public void create(Command command) {
        em.persist(command);
    }

    public void resetLockAfterFailure(Command command) {
        command = em.find(Command.class, command.getId());
        command.setLocked(null);
        command.incrementAttempts();
    }

    public void delete(Command command) {
        em.remove(command);
    }

    public int updateTimedoutCommands() {
        return em.createNamedQuery(Command.NQUpdateLocked.NAME)
                .setParameter(Command.NQUpdateLocked.PARAM_TIMESTAMP, System.currentTimeMillis() + 30000L)
                .executeUpdate();
    }
}
