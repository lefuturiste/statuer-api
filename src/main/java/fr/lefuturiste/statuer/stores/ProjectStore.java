package fr.lefuturiste.statuer.stores;

import fr.lefuturiste.statuer.models.Namespace;
import fr.lefuturiste.statuer.models.Project;
import fr.lefuturiste.statuer.models.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

import static fr.lefuturiste.statuer.HibernateService.getEntityManager;

public class ProjectStore extends Store {
    public static List<Project> getMany() {
        EntityManager entitymanager = getEntityManager();
        return entitymanager.createQuery("from Project", Project.class).getResultList();
    }

    public static Project getOne(UUID uuid) {
        EntityManager entitymanager = getEntityManager();
        try {
            return entitymanager
                    .createQuery("from Project where id = :id", Project.class)
                    .setParameter("id", uuid.toString())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static Project getOneBySlugAndByNamespace(String slug, Namespace namespace) {
        EntityManager entitymanager = getEntityManager();
        try {
            return entitymanager
                    .createQuery("from Project where slug = :slug and namespace = :namespace", Project.class)
                    .setParameter("slug", slug)
                    .setParameter("namespace", namespace)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Will delete recursively the projects and its services
     *
     * @param project Project
     */
    public static int delete(Project project) {
        int entitiesDeleted = Store.delete(project);
        for (Service service: project.getServices())
            entitiesDeleted += ServiceStore.delete(service);
        return entitiesDeleted;
    }
}
