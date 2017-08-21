package ch.maxant.websocketdemo.b2e;

import ch.maxant.websocketdemo.b2e.data.Model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

@ServerEndpoint("/ws")
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
                                Objects.equals(context, e.getContext()) &&
                                e.getTimestamp() >= latestTimestamp
                        ).collect(toList());

                if(relevantEvents.isEmpty()){
                    session.getAsyncRemote().sendText("noData"); //causes the client to do a reload
                }else{
                    removeDuplicates(relevantEvents);
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
            logger.error("Failed to process message from client " + session.getId(), e);
            //TODO how do you NOK a message from the client? i guess with an error message back to it
            session.getAsyncRemote().sendText("ERROR failed to parse '" + message + "'. Error was " + e.getMessage());
        }
    }

    private void removeDuplicates(List<Model.Event> relevantEvents) {
        //ensure we aren't sending the UI effectively the same event multiple times, as it will cause it
        // to go and call the backend once for each event. we want to send the UI just one for each
        // combination of topic and eventname. and we want to send it the last one, so it has the latest
        // timestamp. so reverse, remove duplicates, re-reverse and then send
        Collections.reverse(relevantEvents);
        Set<String> uniques = new HashSet<>(relevantEvents.size());
        Iterator<Model.Event> iterator = relevantEvents.iterator();
        while(iterator.hasNext()){
            Model.Event e = iterator.next();
            String uniqueKey = e.getTopic() + "::" + e.getEventName(); //we already filtered on context - they all belong to the same one!
            if(uniques.contains(uniqueKey)){
                iterator.remove();
            }else{
                uniques.add(uniqueKey);
            }
        }
        Collections.reverse(relevantEvents); //return the order to the way it was
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
