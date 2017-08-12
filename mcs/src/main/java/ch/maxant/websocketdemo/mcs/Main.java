package ch.maxant.websocketdemo.mcs;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.jpa.JPAFraction;
import org.wildfly.swarm.logging.LoggingFraction;

public class Main {

    public static final String PRIMARY_DS = "primaryDS";

    public static void main(String[] args) throws Exception {

        Swarm swarm = buildSwarm();

        swarm.start();

        /*
        call using:
            GET /all HTTP/1.1
            Host: localhost:8080
            Cache-Control: no-cache
         */

        swarm.deploy();
    }

    static Swarm buildSwarm() throws Exception {
        Swarm swarm = new Swarm();

        //TODO i think this can be done easier, no? ie with just project-stages.yml and no code here?
        //see https://groups.google.com/forum/#!topic/wildfly-swarm/0E0-FyRJzJk ?
        swarm.fraction(new DatasourcesFraction()
                .jdbcDriver(swarm.stageConfig().resolve("primary_database.jdbcDriver.name").getValue(), (d) -> {
                    d.driverClassName(swarm.stageConfig().resolve("primary_database.jdbcDriver.driverClassName").getValue());
                    d.xaDatasourceClass(swarm.stageConfig().resolve("primary_database.jdbcDriver.xaDatasourceClass").getValue());
                    d.driverModuleName(swarm.stageConfig().resolve("primary_database.jdbcDriver.driverModuleName").getValue());
                })
                .dataSource(PRIMARY_DS, (ds) -> {
                    ds.driverName(swarm.stageConfig().resolve("primary_database.datasource.driverName").getValue());
                    ds.connectionUrl(swarm.stageConfig().resolve("primary_database.datasource.url").getValue());
                    ds.userName(swarm.stageConfig().resolve("primary_database.datasource.username").getValue());
                    ds.password(swarm.stageConfig().resolve("primary_database.datasource.password").getValue());
                })
        );

        swarm.fraction(new JPAFraction()
                               .defaultDatasource("jboss/datasources/primaryDS")
        );

        //see https://groups.google.com/forum/#!topic/wildfly-swarm/0E0-FyRJzJk
        swarm.fraction(
                new LoggingFraction()
                        .defaultFormatter()
                        .consoleHandler(Level.INFO, "PATTERN")
                        //.fileHandler(FILE_HANDLER_KEY, "sql-file.log", Level.FINE, "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n")
                        .rootLogger(Level.INFO, "CONSOLE")
                        //.logger("wildflyswarm.filelogger", l -> l.level(Level.FINE).handler(FILE_HANDLER_KEY).useParentHandlers(false))
        );

        return swarm;
    }
}
