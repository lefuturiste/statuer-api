package fr.lefuturiste.statuer.stores;

import fr.lefuturiste.statuer.models.Namespace;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

import static fr.lefuturiste.statuer.HibernateService.getEntityManager;

public class NamespaceStore {
    public static List<Namespace> getMany() {
        EntityManager entitymanager = getEntityManager();
        return entitymanager.createQuery("from Namespace", Namespace.class).getResultList();
    }

    public static Namespace getOne(UUID uuid) {
        EntityManager entitymanager = getEntityManager();
        try {
            return entitymanager
                    .createQuery("from Namespace where id = :id", Namespace.class)
                    .setParameter("id", uuid.toString())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static Namespace getOneByName(String name) {
        EntityManager entitymanager = getEntityManager();
        try {
            return entitymanager
                    .createQuery("from Namespace where name = :name", Namespace.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static void persist(Namespace namespace) {
        EntityManager entitymanager = getEntityManager();
        entitymanager.getTransaction().begin();
        entitymanager.persist(namespace);
        entitymanager.getTransaction().commit();
        entitymanager.clear();
    }

    public static void delete(Namespace namespace) {
        EntityManager entitymanager = getEntityManager();
        entitymanager.getTransaction().begin();
        entitymanager.remove(namespace);
        entitymanager.getTransaction().commit();
    }
}
