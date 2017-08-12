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
- remove Integration Tests with final Swarm running (mysql), including login with keycloak
- jax-rs2 client call to ARO to create task
- cors: https://github.com/wildfly-swarm/wildfly-swarm-examples/blob/master/jaxrs/health/src/main/java/org/wildfly/swarm/examples/jaxrs/health/CORSFilter.java
- upgrade to latest swarm version
- project-stages.yml: see https://issues.jboss.org/browse/SWARM-967
