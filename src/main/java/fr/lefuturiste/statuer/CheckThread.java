package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.checker.HttpChecker;
import fr.lefuturiste.statuer.models.Incident;
import fr.lefuturiste.statuer.models.Service;
import fr.lefuturiste.statuer.notifier.DiscordNotifier;
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
        App.logger.info("CheckThread was forced to update the services cache");
        services = ServiceStore.getMany();
    }

    @Override
    public void run() {
        Duration sleepDuration = Duration.ofSeconds(10);
        App.logger.info("Starting check thread with sleepDuration of " + sleepDuration.toString());
        updateService();
        // load the services into a store in memory
        // have a clock
        // at each pulse, look in the store for each service if the time is elapsed
        // if time elapsed perform check
        // update the check time in the memory and in the db
        HttpChecker httpChecker = new HttpChecker();
        DiscordNotifier discordNotifier = new DiscordNotifier();
        while (true) {
            for (Service service : services) {
                if (service.getUrl() == null || service.getUrl().equals("")) {
                    App.logger.debug("Skipped service " + service.getSlug() + " (no url)");
                    break;
                }
                App.logger.debug("Checking service " + service.getSlug());
                // if the time between now and last checked at is more or equal than the time of check_period go check it
                Duration durationSinceLastCheck = Duration.between(
                        service.getLastCheckAt() != null ? service.getLastCheckAt() : Instant.now().minus(Duration.ofSeconds(service.getCheckPeriod())),
                        Instant.now());
                if (durationSinceLastCheck.getSeconds() >= service.getCheckPeriod()) {
                    App.logger.debug("This service was not checked since: " + durationSinceLastCheck.getSeconds());
                    boolean isAvailable = httpChecker.isAvailable(service);
                    App.logger.debug("Service available: " + isAvailable);
                    if ((service.getAvailable() != null && service.getAvailable() != isAvailable) || (service.getAvailable() == null && !isAvailable)) {
                        service.setAvailable(isAvailable);
                        // status has changed
                        Incident lastIncident;
                        if (!isAvailable) {
                            // status as changed as DOWN
                            Instant downInstant = Instant.now();
                            service.setLastDownAt(downInstant);
                            // we can create a incident
                            lastIncident = new Incident();
                            lastIncident.setId(UUID.randomUUID().toString());
                            lastIncident.setStartedAt(downInstant);
                            lastIncident.setService(service);
                            service.setLastIncident(lastIncident);
                            IncidentStore.persist(lastIncident, false);
                        } else {
                            // status as changed as UP
                            // we can update our incident to indicate the end of the incident
                            lastIncident = service.getLastIncident();
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
                            numberBigDecimal = numberBigDecimal.setScale(8, RoundingMode.HALF_UP);
                            App.logger.debug("Updated uptime to " + numberBigDecimal);
                            service.setUptime(numberBigDecimal.floatValue());
                        }
                        App.logger.info("Status of service " + service.getSlug() + " changed to " + (isAvailable ? "UP" : "DOWN"));
                        // we can now notify of the incident (updated or created)
                        discordNotifier.notify(lastIncident);
                        ServiceStore.persist(service, false);
                    }
                    if (service.getAvailable() == null) {
                        service.setAvailable(isAvailable);
                        ServiceStore.persist(service, false);
                    }
                }
            }

            try {
                Thread.sleep(sleepDuration.toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
