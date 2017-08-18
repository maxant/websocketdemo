package ch.maxant.websocketdemo.b2e;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.websocket.Session;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Future;

import static ch.maxant.websocketdemo.b2e.WebSocketEndpoint.CONTEXT;
import static java.util.stream.Collectors.toList;

@Stateless
public class EventSender {

    @Inject
    Logger logger;

    @Asynchronous
    public Future<Void> distributeToUi(Collection<Session> sessions, ConsumerRecord<String, String> record) {

        String msg = "{\"topic\": \"" + record.topic() + "\", \"command\": \"" + record.value() + "\", \"timestamp\": " + record.timestamp() + "}";
        logger.info("there are " + sessions.size() + " known sessions.");
        sessions = sessions.stream().filter(s -> s.isOpen() && Objects.equals(s.getUserProperties().get(CONTEXT), record.key())).collect(toList()); //TODO filter needs to be better, ie not using equals, but regexp
        logger.info("there are " + sessions.size() + " relevant sessions.");
        sessions.stream()
                .forEach(s -> {
                    if(s.isOpen()){
                        try{
                            logger.info("sending event to session " + s.getId());
                            s.getAsyncRemote().sendText(msg, r -> {
                                if(r.isOK()){
                                    logger.info("sent to session " + s.getId());
                                }else{
                                    logger.warn("got nok while sending to session " + s.getId(), r.getException());
                                }
                            });
                        }catch(Exception e){
                            logger.warn("failed to send to session " + s.getId(), e);
                        }
                    }
                });

        logger.info("finished distributing to UI");

        return new AsyncResult<>(null);
    }

}
