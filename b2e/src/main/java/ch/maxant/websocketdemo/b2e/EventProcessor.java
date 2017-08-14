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
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import java.util.*;

@Singleton
@Lock(LockType.READ)
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

        mes.execute(() -> read());

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

    private void read() {
        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {

            //TODO async commit?? => remove commit stuff in props above???

            //TODO put into model

            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            logger.info("RECORD RECEIVED");
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

}
