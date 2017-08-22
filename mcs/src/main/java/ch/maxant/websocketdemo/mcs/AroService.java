package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.framework.commands.Command;
import ch.maxant.websocketdemo.mcs.framework.commands.CommandService;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class AroService {

    @Inject
    CommandService commandService;

    /** will create a command which causes a task to be created in ARO, asynchronously, but robustly. */
    public void createTask(long caseNr, String textForTask) {
        String context = createContext(caseNr, textForTask);

        Command command = new Command(AroCreateTaskCommandExecutor.class, context);

        commandService.persistCommand(command);
    }

    private String createContext(long nr, String textForTask) {
        //TODO use object mapper rather than build string ourselves...
        return "{\"caseNr\": " + nr + ", \"textForTask\": \"" + textForTask + "\"}";
    }

}
