package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.framework.commands.ExecutableCommand;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class AroCreateTaskCommandExecutor implements ExecutableCommand {

    @Inject
    AroIntegrationService aroIntegrationService;

    @Override
    public void execute(String idempotencyId, JsonNode context) {
        long caseNr = context.get("caseNr").longValue();
        String textForTask = context.get("textForTask").textValue();
        aroIntegrationService.createAroTask(idempotencyId, caseNr, textForTask);

    }
}
