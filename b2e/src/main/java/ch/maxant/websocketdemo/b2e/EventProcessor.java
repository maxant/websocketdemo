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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import static java.lang.String.format;

@Singleton
@Startup
public class EventProcessor {

    @Inject
    Logger logger;

    @Inject
    Model model;

    @Inject
    EventSender eventSender;

    @Resource
    ManagedExecutorService mes;

    Consumer<String, String> consumer;

    Object consumerLock = new Object();

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
        consumer.subscribe(topics);

        logger.info("subscribed to kafka topics: " + topics);

        mes.execute(() -> sleepThenRead());

        logger.info("post construction done.");
    }

    @PreDestroy
    public void predestroy() {
        logger.info("closing kafka consumer...");
        synchronized (consumerLock){ //ugly, but its not thread safe and i really want to call close before the server goes down and theres no guarantee that will happen if we say use a boolean which is set here to tell the next running execution to do the close. that might never run, and leave the consumer in an ugly state
            consumer.close();
        }
        logger.info("closed consumer. pre destruction done.");
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
        logger.debug("reading kafka...");
        ConsumerRecords<String, String> records;
        synchronized (consumerLock){
            logger.debug("polling kafka...");
            records = consumer.poll(1000);
        }
        logger.debug("polled kafka...");
        for (ConsumerRecord<String, String> record : records) {

            // TODO async commit?? => remove commit stuff in props above??? => we need to handle the offset ourselves,
            // because when new versions of swarm are deployed in the cloud, they overwrite old images and that data
            // gets lost. same is true if harddisk dies.

            logger.info(format("got message from kafka: topic= %s, offset = %d, key = %s, value = %s%n", record.topic(), record.offset(), record.key(), record.value()));

            model.addEvent(new Model.Event(record.key(), record.value(), record.timestamp(), record.offset(), record.topic()));

            eventSender.distributeToUi(model.getSessions(), record);
        }

        logger.debug("re-executing kafka polling...");
        mes.execute(() -> read()); //if we are on the way down, this new task simply never gets executed.
    }

}
