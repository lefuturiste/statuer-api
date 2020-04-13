package fr.lefuturiste.statuer;

import org.hibernate.cfg.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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
            configuration.setProperty("hibernate.connection.url", connectionUrl);
            configuration.setProperty("hibernate.connection.username", username);
            configuration.setProperty("hibernate.connection.password", password);
            configuration.setProperty("hibernate.show_sql", "false");
            String debugConfig = "Hibernate settings " +
                    configuration.getProperty("connection.url") +
                    " " +
                    configuration.getProperty("hibernate.connection.username") +
                    " " +
                    configuration.getProperty("hibernate.connection.password");
            App.logger.debug(debugConfig);
            EntityManagerFactory entityManagerFactory = configuration.buildSessionFactory();
            entityManager = entityManagerFactory.createEntityManager();
        }
        return entityManager;
    }
}
