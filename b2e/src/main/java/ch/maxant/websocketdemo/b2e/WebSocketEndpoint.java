package ch.maxant.websocketdemo.b2e;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/b2e")
public class WebSocketEndpoint {

        @OnMessage
        public void message(String message, Session session) {
            session.getOpenSessions()
                    .forEach(s -> s.getAsyncRemote().sendText(message));
        }
}
