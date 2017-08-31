package ch.maxant.websocketdemo.mcs;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Properties;
import java.util.concurrent.Future;

@MessageDriven(
        activationConfig={@ActivationConfigProperty(propertyName="destination", propertyValue="/jms/queue/events")},
        messageListenerInterface=MessageListener.class)
public class EventService {

    @Inject
    Logger logger;

    Producer<String, String> producer;

    @PostConstruct
    public void init(){
        //http://kafka.apache.org/documentation.html#producerconfigs
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getProperty("kafka.bootstrap.servers"));
        //props.put("retries", 0); //dont retry, since we're using JMS, we will do the retry ourselves
        //props.put("acks", "all"); set to "all" with enable.idempotence entry
        props.put("enable.idempotence", true); //TODO or String? => it means "retries" is not necessary
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("client.id", "mcs"); //useful for debugging

        producer = new KafkaProducer<>(props); //docs say its threadsafe
    }

    @PreDestroy
    public void predestroy(){
        if(producer != null){
            producer.close();
        }
        logger.info("mdb destroyed");
    }

    public void onMessage(Message m) throws JMSException {
        String msg = ((TextMessage)m).getText();
        String context = m.getStringProperty("context");
        logger.info("ON MESSAGE: " + msg);
        try{
            Future<RecordMetadata> response = producer.send(new ProducerRecord<>("mcs", context, msg), (metadata, ex) -> {
                if (ex == null) {
                    logger.info("ACK of '" + msg + "': " + metadata);
                } else {
                    logger.error("NOK of '" + msg + "': " + metadata, ex);
                }
            });
            logger.info("sent");

            //one possible solution:
            //RecordMetadata metadata = response.get();//am i crazy, blocking a thread doing this? well I don't want to commit until this is definitely done!
            //logger.info("finished sending '" + msg + "': " + metadata);
            //another: update a status in the DB using a new transaction once the callback is called. that way we know, robustly, what has not yet been sent
            //the big question here is how important it is to guarantee sending the event. in our use case, failure to send an event is no big
            //deal, as its just an indicator to the UI that it should do an update. the user can always manually click, if they think
            //data is missing. and when the context changes, the UI can go get all data anyway.
        }catch (Exception e){
            //according to the docs, because we're using idempotency, we MUST NOT attempt applicatory retries.
            //no rollback!
            logger.error("Message passed to kafka producer, but an exception occurred. was it really sent? " + msg, e);
        }
    }

}
