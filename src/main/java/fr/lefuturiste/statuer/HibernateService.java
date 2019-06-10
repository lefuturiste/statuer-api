package fr.lefuturiste.statuer;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.EntityManager;

public class HibernateService {

    private static EntityManager entityManager = null;

    public static EntityManager getEntityManager() {
        if (entityManager == null) {
            SessionFactory sf = new Configuration().configure().buildSessionFactory();
            entityManager = sf.createEntityManager();
        }
        return entityManager;
    }
}
