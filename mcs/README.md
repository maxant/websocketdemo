# What is this?

A Swarm application with:

- JAX-RS
- CDI
- EJB
- JPA
- JTA
- Project Stages (Configuration)
- Flyway
- Tests using H2, Prod with Mysql

Build and run:

    mvn clean install && java -Dkafka.bootstrap.servers=localhost:9092 -jar target/mcs-swarm.jar

Run tests using Mysql, rather than in-memory H2:

   mvn test -Dtest.use.mysql

#Useful Links

- https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/content/
- [System Properties](https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/content/configuration_properties.html)

# Issues

- none

# TODO

- how come after MCS unit tests with Mysql, there are still entries in the DB?
-- which test is it?
- cors: https://github.com/wildfly-swarm/wildfly-swarm-examples/blob/master/jaxrs/health/src/main/java/org/wildfly/swarm/examples/jaxrs/health/CORSFilter.java
- modify case should create a task in ARO which fires an event. modification should also fire an event.
- dont use JMS, if the image is replaced, jboss losses any messages not yet processed! => we have to put it into the DB.
-- no its ok, it demonstrates how it could work. and commands demonstrate a better way.
- move common stuff to parent
- move nginx into parent folder
- add a process manager which kills instances randomly and triple check we never lose data!
