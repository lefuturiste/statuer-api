package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.models.Incident;
import fr.lefuturiste.statuer.models.Service;
import fr.lefuturiste.statuer.stores.IncidentStore;
import fr.lefuturiste.statuer.stores.ServiceStore;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static fr.lefuturiste.statuer.HibernateService.getEntityManager;

public class CheckThread implements Runnable {

    private List<Service> services;

    public void updateService() {
        System.out.println("Update service trigger");
        services = ServiceStore.getMany();
    }

    @Override
    public void run() {
        System.out.println("run...");
        updateService();
        // load the services into a store in memory
        // have a clock
        // at each pulse, look in the store for each service if the time is elapsed
        // if time elapsed perform check
        // update the check time in the memory and in the db
        while (true) {
            System.out.println("New check period...");
            for (Service service : services) {
                System.out.println("Checking service " + service.getName());
                // if the time between now and last checket at is more or equal than the time of check_period go check it
                Duration durationSinceLastCheck = Duration.between(
                        service.getLastCheckAt() != null ? service.getLastCheckAt() : Instant.now().minus(Duration.ofSeconds(service.getCheckPeriod())),
                        Instant.now());
                if (durationSinceLastCheck.getSeconds() >= service.getCheckPeriod()) {
                    System.out.println("    This service was not checked since: " + durationSinceLastCheck.getSeconds());
                    System.out.println("    Now checking: " + service.getName());
                    boolean isAvailable = Checker.isAvailable(service);
                    System.out.println("    Service available: " + isAvailable);
                    if ((service.getAvailable() != null && service.getAvailable() != isAvailable) || (service.getAvailable() == null && !isAvailable)) {
                        // status as changed
                        service.setAvailable(isAvailable);
                        if (!isAvailable) {
                            System.out.println("    Status changed to DOWN");
                            // status as changed as DOWN
                            Instant downInstant = Instant.now();
                            service.setLastDownAt(downInstant);
                            // we can create a incident
                            Incident incident = new Incident();
                            incident.setId(UUID.randomUUID().toString());
                            incident.setStartedAt(downInstant);
                            incident.setService(service);
                            service.setLastIncident(incident);
                            IncidentStore.persist(incident, false);
                        } else {
                            System.out.println("    Status changed to UP");
                            // status as changed as UP
                            // we can update our incident to indicate the end of the incident
                            Incident lastIncident = service.getLastIncident();
                            lastIncident.setFinishedAt(Instant.now());
                            // since the incident is finished we can update the down time percentage of this service (last 90d)
                            // fetch all the last 90d incident for this service
                            EntityManager entitymanager = getEntityManager();
                            TypedQuery<Incident> query = entitymanager.createQuery(
                                    "from Incident where startedAt > :ago or startedAt < :now",
                                    Incident.class);
                            Instant startOfRange = Instant.now().minus(Duration.ofDays(90));
                            Instant endOfRange = Instant.now();
                            query.setParameter("ago", startOfRange);
                            query.setParameter("now", endOfRange);
                            List<Incident> incidents = query.getResultList();
                            Duration totalDownDuration = Duration.ofSeconds(0);
                            for (Incident incident : incidents) {
                                if (incident.getStartedAt().isBefore(startOfRange)) {
                                    totalDownDuration = totalDownDuration.plus(Duration.between(startOfRange, incident.getFinishedAt()));
                                } else {
                                    totalDownDuration = totalDownDuration.plus(Duration.between(incident.getStartedAt(), incident.getFinishedAt()));
                                }
                            }
                            Duration rangeDuration = Duration.between(startOfRange, endOfRange);
                            float percentage = (float) (totalDownDuration.getSeconds() * 100) / rangeDuration.getSeconds();
                            BigDecimal numberBigDecimal = new BigDecimal(percentage);
                            numberBigDecimal  = numberBigDecimal.setScale(8, RoundingMode.HALF_UP);
                            System.out.println("    Updated uptime to " + numberBigDecimal);
                            service.setUptime(numberBigDecimal.floatValue());
                        }
                        // we can now notify of the incident (updated or created)
                        Notifier.notify(service);
                    }
                    service.setLastCheckAt(Instant.now());
                    ServiceStore.persist(service, false);
                } else {
                    System.out.println("    Already checked");
                }
            }

            try {
                Thread.sleep(Duration.ofSeconds(5).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
