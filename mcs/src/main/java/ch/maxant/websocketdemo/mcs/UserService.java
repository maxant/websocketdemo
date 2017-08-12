package ch.maxant.websocketdemo.mcs;

import ch.maxant.websocketdemo.mcs.data.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    EntityManager em;

    public List<User> getAllUsingEntityManager(){
        return em.createNamedQuery(User.NQFindAll.NAME).getResultList();
    }
}
