package ch.maxant.websocketdemo.mcs;

import org.flywaydb.core.Flyway;
import org.junit.After;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public abstract class DbTest {

    protected EntityManager em;

    protected EntityManagerFactory emf;

    public void setup(){
        //do EM first, then flyway, otherwise the in memory DB is closed before the EM starts up, and the em version has no tables!
        emf = Persistence.createEntityManagerFactory("primary_test");
        em = emf.createEntityManager();

        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:test", "sa", "");
        flyway.migrate();
    }

    @After
    public void teardown(){
        emf.close();
    }

}
