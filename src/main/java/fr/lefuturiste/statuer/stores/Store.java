package fr.lefuturiste.statuer.stores;

import javax.persistence.EntityManager;

import static fr.lefuturiste.statuer.HibernateService.getEntityManager;

public class Store {

    public static void persist(Object object) {
        persist(object, true);
    }

    public static void persist(Object object, boolean clear) {
        EntityManager entitymanager = getEntityManager();
        entitymanager.getTransaction().begin();
        entitymanager.persist(object);
        entitymanager.getTransaction().commit();
        if (clear) {
            entitymanager.clear();
        }
    }

    public static void delete(Object object) {
        EntityManager entitymanager = getEntityManager();
        entitymanager.getTransaction().begin();
        entitymanager.remove(object);
        entitymanager.getTransaction().commit();
        entitymanager.clear();
    }

}
