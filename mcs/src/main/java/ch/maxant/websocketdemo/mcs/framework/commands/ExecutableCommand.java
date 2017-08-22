package ch.maxant.websocketdemo.mcs.framework.commands;

import com.fasterxml.jackson.databind.JsonNode;

public interface ExecutableCommand {

    void execute(String idempotencyId, JsonNode context);
}
