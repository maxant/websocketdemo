# What is this?

A Swarm application with:

- CDI
- Static Web Content
- Web Sockets

This contains the webapp used to subscribe to events from the backend.

- Roundtrip from postman->mcs->kafka->b2e->browser works.
- the following test test works: shutdown b2e, send message, restart b2e. clients receive missed messages. IMPORTANT: wait 5 secs before subscribing to kafka, to allow time for clients to reconnect. see code in the EventProcessor class related to that.

#Useful Links

- https://github.com/emag-wildfly-swarm-sandbox/wildfly-swarm-quickstarts/blob/master/websocket/src/main/webapp/index.html

# Issues

- none

# TODO

- remove main: Custom main() usage is intended to be deprecated in a future release and is no longer supported,
               please refer to http://reference.wildfly-swarm.io for YAML configuration that replaces it.
- jax-rs2 client call to ARO to create task
- cors: https://github.com/wildfly-swarm/wildfly-swarm-examples/blob/master/jaxrs/health/src/main/java/org/wildfly/swarm/examples/jaxrs/health/CORSFilter.java
- comms from one b2e to others in the cluster. might be good to investigate zookeeper here?
- is swarm jms proper XA?
- hows nginx react when we kill one of the b2e instances when its in load balancing mode? will it send everything on to just the one instance? is it nice enough to not kill the one connection, but try to reestablish it with the instance thats still online?
- when clients context changes, we need to inform other b2e instances as they are passive
- dont use JMS, if the image is replaced, jboss losses any messages not yet processed! => we have to put it into the DB.