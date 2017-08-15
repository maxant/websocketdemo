package ch.maxant.websocketdemo.b2e;

import ch.maxant.websocketdemo.b2e.data.Model;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import java.util.*;

import static java.lang.String.format;

@Singleton
@Startup
public class EventProcessor {

    @Inject
    Logger logger;

    @Inject
    Model model;

    @Resource
    ManagedExecutorService mes;

    Consumer<String, String> consumer;

    boolean running = true;

    boolean finished = false;

    @PostConstruct
    public void init() {
        //http://kafka.apache.org/documentation.html#newconsumerconfigs
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getProperty("kafka.bootstrap.servers"));
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "b2e");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props); //docs say its NOT threadsafe

        List<String> topics = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(System.getProperty("kafka.topics"), ",; ");
        while (st.hasMoreTokens()) {
            topics.add(st.nextToken());
        }
        //consumer.subscribe(topics);
        consumer.subscribe(Arrays.asList("mcs"));

        logger.info("subscribed to kafka topics: " + topics);

        mes.execute(() -> sleepThenRead());

        logger.info("post construction done.");
    }

    @PreDestroy
    public void predestroy() {
        running = false;
        while(!finished){
            try {
                logger.info("waiting for kafka to shutdown...");
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("pre destruction done.");
    }

    private void sleepThenRead() {
        //test: shutdown B2E, send event via mcs. restart B2E. message sent?
        //problematic because event is received BEFORE clients reconnect. we have to give
        //them time to reconnect. so defer connecting to kafka for say 10 secs?
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            logger.error("Failed to wait a little before consuming events from kafka. its important to sleep, to allow clients time to reconnect via websocket.", e);
        }
        mes.execute(() -> read());
    }

    private void read() {
        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {

            //TODO async commit?? => remove commit stuff in props above???

            logger.info(format("topic= %s, offset = %d, key = %s, value = %s%n", record.topic(), record.offset(), record.key(), record.value()));

            distributeToUi(record);

        }

        if (running) {
            mes.execute(() -> read());
        } else {
            logger.info("closing kafka consumer...");
            consumer.close();
            logger.info("kafka consumer closed.");
            finished = true;
        }
    }

    private void distributeToUi(ConsumerRecord<String, String> record) {
        model.getSessions()
                .filter(s -> true) //TODO filter based on event contents. session needs IDs e.g. under s.getUserProperties(), for schadenfall, teilfall, etc. ie context
                .forEach(s -> {
                    s.getAsyncRemote().sendText(record.value(), r -> {
                        if(r.isOK()){
                            logger.info("sent to session " + s.getId());
                        }else{
                            logger.warn("failed to send to session " + s.getId(), r.getException());
                        }
                    });
                });

        //TODO need to tell other nodes in cluster that the message was successfully sent to the client!
    }

}
