# What is this?

A Swarm application with:

- JAX-RS
- CDI
- EJB
- JPA
- JTA
- Project Stages (Configuration)
- Flyway

#Useful Links

- https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/content/
- [System Properties](https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/content/configuration_properties.html)

# Issues

- none

# TODO

- remove main: Custom main() usage is intended to be deprecated in a future release and is no longer supported,
               please refer to http://reference.wildfly-swarm.io for YAML configuration that replaces it.
- cors: https://github.com/wildfly-swarm/wildfly-swarm-examples/blob/master/jaxrs/health/src/main/java/org/wildfly/swarm/examples/jaxrs/health/CORSFilter.java
- modify case should create a task in ARO which fires an event. modification should also fire an event.
- dont use JMS, if the image is replaced, jboss losses any messages not yet processed! => we have to put it into the DB.
- fix updating of cases and UUID problems