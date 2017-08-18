package ch.maxant.websocketdemo.b2e.data;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.util.*;

@ApplicationScoped
public class Model {

    private Set<Session> sessions = new HashSet<>();

    /**
     * Design: we receive events from kafka and store them in our cache.
     * when a client joins, it tells us its context and the timestamp
     * of the last event it received in this context. if it just loaded the case,
     * it would provide us with a current timestamp, so there wouldnt be much to deliver to it,
     * if anything at all. if it rejoined after a broken connection, or simply because the client
     * went offline for a while, it would send us a relatively old timestamp. so we go thru the list
     * of events and provide it with everything in its context since that point.
     * if its timestamp is older than our oldest timestamp, we cant help, and it needs to go do a reload
     * for example fetch all documents and tasks for its context. it then reregisters with a current
     * timestamp and hence is up to date.
     *
     * if your reading this, go listen to some good music:
     * - ride
     * - drop nineteens
     * - catherine wheel
     * - chapterhouse
     */
    private Queue<Event> events = new CircularFifoQueue<>(Integer.getInteger("event.history.size", 10000));

    public boolean addSession(Session session){
        return sessions.add(session);
    }

    public boolean removeSession(Session session){
        return sessions.remove(session);
    }

    public Collection<Session> getSessions(){
        return new HashSet<>(sessions);
    }

    /** creates a copy of the events, ie a snapshot */
    public List<Event> getEvents(){
        return new ArrayList<>(events);
    }

    public void addEvent(Event event){
        events.add(event);
    }

    public static class Event {
        private String context;
        private String eventName;
        private long timestamp;
        private long offset;
        private String topic;

        public Event(String context, String eventName, long timestamp, long offset, String topic){
            this.context = context;
            this.eventName = eventName;
            this.timestamp = timestamp;
            this.offset = offset;
            this.topic = topic;
        }

        public String getContext() {
            return context;
        }

        public long getOffset() {
            return offset;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getEventName() {
            return eventName;
        }

        public String getTopic() {
            return topic;
        }
    }

}
