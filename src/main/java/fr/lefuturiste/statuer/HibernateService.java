package fr.lefuturiste.statuer;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.EntityManager;

public class HibernateService {

    private static EntityManager entityManager = null;
    private static String connectionUrl;
    private static String username;
    private static String password;

    public static void setConfig(String connectionUrl, String username, String password) {
        HibernateService.connectionUrl = connectionUrl;
        HibernateService.username = username;
        HibernateService.password = password;
    }

    public static EntityManager getEntityManager() {
        if (entityManager == null) {
             Configuration configuration = new Configuration().configure();
             configuration.setProperty("connection.url", connectionUrl);
             configuration.setProperty("hibernate.connection.username", username);
             configuration.setProperty("hibernate.connection.password", password);
            SessionFactory sessionFactory = configuration.buildSessionFactory();
            entityManager = sessionFactory.createEntityManager();
        }
        return entityManager;
    }
}
