package ch.maxant.websocketdemo.b2e;

import ch.maxant.websocketdemo.b2e.data.Model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ServerEndpoint("/b2e/{context}")
public class WebSocketEndpoint {

    public static final String CONTEXT = "CONTEXT";

    @Inject
    Logger logger;

    @Inject
    Model model;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        logger.info("ws open: " + session.getId() + " with config " + config);
        model.addSession(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode n = om.readTree(message);
            String command = n.get("command").textValue();
            if(command.equals("newContext")){
                String context = n.get("context").textValue();
                session.getUserProperties().put(CONTEXT, context);
                long latestTimestamp = n.get("latestTimestamp").longValue();
                List<Model.Event> relevantEvents = model.getEvents().stream()
                        .filter(e ->
                                e.getContext().equals(context) &&
                                e.getTimestamp() >= latestTimestamp
                        ).collect(toList());
                if(relevantEvents.isEmpty()){
                    session.getAsyncRemote().sendText("noData"); //causes the client to do a reload
                }else{
                    session.getAsyncRemote().sendText(om.writeValueAsString(relevantEvents));
                }
            }else{
                throw new Exception("unknown command " + command);
            }
        } catch (IOException e) {
            logger.warn("unable to parse message from " + session.getId());
            //TODO how do you NOK a message from the client? i guess with an error message back to it
            session.getAsyncRemote().sendText("ERROR failed to parse '" + message + "'");
        } catch (Exception e) {
            //TODO how do you NOK a message from the client? i guess with an error message back to it
            session.getAsyncRemote().sendText("ERROR failed to parse '" + message + "': " + e.getMessage());
        }

        if(message.startsWith(CONTEXT + ":")){
        }


        //copy back to all connected clients
        session.getOpenSessions().forEach(s -> s.getAsyncRemote().sendText(message));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.error("ws error " + session.getId() + ".", t);
        //lets dump it. need more experience with this really... client will reconnect if they want to...
        try {
            model.removeSession(session);
            session.close();
        } catch (IOException e) {
            logger.error("error closing websocket " + session.getId());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.info("ws closing " + session.getId() + ". reason: " + reason.getReasonPhrase() + " / " + reason.getCloseCode().getCode());
        model.removeSession(session);
    }
}
