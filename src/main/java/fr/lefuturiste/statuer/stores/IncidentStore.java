package fr.lefuturiste.statuer.stores;

import fr.lefuturiste.statuer.models.Incident;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

import static fr.lefuturiste.statuer.HibernateService.getEntityManager;

public class IncidentStore extends Store {
    public static List<Incident> getMany() {
        EntityManager entitymanager = getEntityManager();
        return entitymanager.createQuery("from Incident", Incident.class).getResultList();
    }

    public static Incident getOne(UUID uuid) {
        EntityManager entitymanager = getEntityManager();
        try {
            return entitymanager
                    .createQuery("from Incident where id = :id", Incident.class)
                    .setParameter("id", uuid.toString())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
