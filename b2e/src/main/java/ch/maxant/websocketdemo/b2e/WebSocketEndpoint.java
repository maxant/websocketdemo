package ch.maxant.websocketdemo.b2e;

import ch.maxant.websocketdemo.b2e.data.Model;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/b2e")
public class WebSocketEndpoint {

    @Inject
    Logger logger;

    @Inject
    Model model;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        logger.info("ws open: " + session.getId());
        model.addSession(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {

        logger.info("ws message: " + message);

        //copy back to all connected clients
        session.getOpenSessions().forEach(s -> s.getAsyncRemote().sendText(message));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.error("ws error " + session.getId() + ".", t);
        //TODO remove from model?
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.info("ws closing " + session.getId() + ". reason: " + reason.getReasonPhrase() + " / " + reason.getCloseCode().getCode());
        model.removeSession(session);
    }
}
