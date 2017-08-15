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
