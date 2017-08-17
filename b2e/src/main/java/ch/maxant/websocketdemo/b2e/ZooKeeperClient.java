package ch.maxant.websocketdemo.b2e;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

//TODO delete this - not needed at all! all instances of this app keep the same events in memory.
// the passive one(s) are just waiting for the active one to die. all clients will then reconnect
// and send their last received timestamp with, and the active instance will send them everything
// thats happened since then

@Singleton
@Lock(LockType.READ)
@Startup
public class ZooKeeperClient implements Watcher {

    static final String APP_NAME = "b2e";

    private ZooKeeper zk;

    @Inject
    Logger logger;

    @PostConstruct
    public void init(){
        try {
            zk = new ZooKeeper(System.getProperty("zookeeper"), 3000, this);
            if(zk.exists("/" + APP_NAME, false) == null){
                String r = zk.create("/" + APP_NAME, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.info("created " + APP_NAME + " directory in zk. " + r);
            }else{
                zk.getChildren("/" + APP_NAME, false).forEach(c -> logger.info("other node found: " + c));
            }

            String address = InetAddress.getLocalHost().getHostAddress() + ":" + System.getProperty("swarm.http.port");

            //using EPHEMERAL_SEQUENTIAL means we don't need to have a preDestroy to remove ourselves, and we don't
            //need to worry about what happens if the preDestroy isnt called :-)
            String r = zk.create("/" + APP_NAME + "/" + address, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            logger.info("Init done. Registered address " + address + " with zk. This node is called: " + r);
        } catch (InterruptedException | KeeperException |IOException e) {
            logger.error("Failed to start connection to ZooKeeper", e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        logger.info("got zk event: " + event);
    }

    public List<String> getOtherB2eHosts() {
        try {
            return zk.getChildren("/" + APP_NAME, false);
        } catch (KeeperException | InterruptedException e) {
            logger.warn("unable to get list of other b2e nodes", e);
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        System.setProperty("swarm.http.port", "8080");
        System.setProperty("zookeeper", "localhost:2181");
        ZooKeeperClient zk = new ZooKeeperClient();
        zk.logger = NOPLogger.NOP_LOGGER;
        zk.init();

        zk.getOtherB2eHosts().forEach(s -> System.out.println(s));
    }

}
