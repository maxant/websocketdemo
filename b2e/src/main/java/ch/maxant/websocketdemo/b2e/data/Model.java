package ch.maxant.websocketdemo.b2e.data;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@ApplicationScoped
public class Model {

    private Set<Session> sessions = new HashSet<>();

    public boolean addSession(Session session){
        return sessions.add(session);
    }

    public boolean removeSession(Session session){
        return sessions.remove(session);
    }

    public Stream<Session> getSessions(){
        return sessions.stream();
    }

}
