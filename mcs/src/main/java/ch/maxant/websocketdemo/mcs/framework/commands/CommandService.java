package ch.maxant.websocketdemo.mcs.framework.commands;

import ch.maxant.websocketdemo.mcs.AroIntegrationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;

@Singleton
@Startup
public class CommandService {

    public static final String UNLOCK_TIMEDOUT_COMMANDS = "UNLOCK_TIMEDOUT_COMMANDS";

    public static final String RETRY_COMMANDS = "RETRY_COMMANDS";

    public static final int MAX_NUM_RETRIES = 5;

    @Inject
    Logger logger;

    @Resource
    SessionContext context;

    @Resource
    TimerService timerService;

    @Inject
    AroIntegrationService aroIntegrationService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    Event<Command> event;

    @Inject
    CommandRepository commandRepo;

    @PostConstruct
    public void init(){
        //use a timer to handle commands that need to be retried
        timerService.createIntervalTimer(5000L, 5000L, new TimerConfig(RETRY_COMMANDS, false));

        //use a timer to unlock commands which are stuck, i.e. not finished by other instances of this app
        timerService.createIntervalTimer(30000L, 30000L, new TimerConfig(UNLOCK_TIMEDOUT_COMMANDS, false));
    }

    /** called by container when timer fires. no one should be calling this method! */
    @Timeout
    public void timeout(TimerConfig config){
        String timer = (String) config.getInfo();
        if(RETRY_COMMANDS.equals(timer)){
            retryCommands();
        }else if(UNLOCK_TIMEDOUT_COMMANDS.equals(timer)){
            unlockTimedoutCommands();
        }else{
            logger.error("unknown timer: " + timer);
        }
    }

    private void retryCommands() {
        Integer batchSize = Integer.getInteger("commandservice.batch.size", 10);
        List<Command> commands = commandRepo.lockCommands(batchSize); //includes commit
        commands.forEach(c -> executeCommand(c)); //execute commands

        if(commands.size() == batchSize){
            self().doImmediateRetryTimeout();
        }
    }

    /** looks weird, but we need to call ourselves as an EJB, not POJO, so get proxy via session context */
    private CommandService self() {
        return context.getBusinessObject(CommandService.class);
    }

    private void unlockTimedoutCommands() {
        int count = commandRepo.updateTimedoutCommands();

        if(count > 0){
            self().doImmediateRetryTimeout();
        }

        //if we unlock something that is currently in process, its ok, since service providers that we call during
        //the command MUST implement idempotency ie be OK with repeated calls!
    }

    public void observe(@Observes(during = TransactionPhase.AFTER_SUCCESS) Command command){
        self().executeCommand(command);
    }

    /** fire timer immedeately but not recursively because it could cause a StackOverflowError */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Future<Void> doImmediateRetryTimeout(){
        timeout(new TimerConfig(RETRY_COMMANDS, false));
        return new AsyncResult<>(null);
    }

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Future<Void> executeCommand(Command command){
        try{
            JsonNode n = objectMapper.readTree(command.getContext());
            Class<?> clazz = Class.forName(command.getCommand());
            ExecutableCommand ec = (ExecutableCommand) context.getBusinessObject(clazz);
            ec.execute(command.getIdempotencyId(), n);

            commandRepo.delete(command); //its no longer needed. if this fails, no worries, timer will try again and ARO is idempotent
        }catch(Exception e){
            if(command.getAttempts() < MAX_NUM_RETRIES){
                logger.error("Failed to execute command " + command.getId() + ". Command will be retried.", e);
            }else{
                logger.error("Failed to execute command " + command.getId() + ". Command will NOT be retried.", e);
            }
            commandRepo.resetLockAfterFailure(command);
        }
        return new AsyncResult<>(null);
    }

    public void persistCommand(Command command) {
        commandRepo.create(command);

        event.fire(command);//fire immedeatly, so we don't have to wait for timer to pick up the work
    }
}
