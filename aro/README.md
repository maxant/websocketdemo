# What is this?

A Swarm application with:

- JAX-RS
- CDI
- EJB
- JPA
- JTA
- Project Stages (Configuration)
- Flyway

Build and run:

    mvn clean install && java -Dkafka.bootstrap.servers=localhost:9092 -jar target/aro-swarm.jar

#Useful Links

- https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/content/
- [System Properties](https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/content/configuration_properties.html)

# Issues

- none

# TODO

- here we are connecting to kafka in a non-transactional manner. what happens if the messge was sent, but we can't commit?
-- doesnt really matter so much, it leads to the UI reloading tasks. no big deal at all!
-- what if we get a NOK from kafka? shouldnt we rollback the newly created task? otherwise we will save it and the UI will never know about it
- move nginx config into this project
- add idempotency to aro
-