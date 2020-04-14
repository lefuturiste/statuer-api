package fr.lefuturiste.statuer.stores;

import fr.lefuturiste.statuer.models.Namespace;
import fr.lefuturiste.statuer.models.Project;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

import static fr.lefuturiste.statuer.HibernateService.getEntityManager;

public class NamespaceStore extends Store {
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

    public static Namespace getOneBySlug(String slug) {
        EntityManager entitymanager = getEntityManager();
        try {
            return entitymanager
                    .createQuery("from Namespace where slug = :slug", Namespace.class)
                    .setParameter("slug", slug)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Will delete recursively the namespace, its projects and its services
     *
     * @param namespace Namespace
     */
    public static int delete(Namespace namespace) {
        int entitiesDeleted = Store.delete(namespace);
        for (Project project: namespace.getProjects())
            entitiesDeleted += ProjectStore.delete(project);
        return entitiesDeleted;
    }
}
