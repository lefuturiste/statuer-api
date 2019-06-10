package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.models.Service;
import fr.lefuturiste.statuer.stores.ServiceStore;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.List;

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
                        service.setAvailable(isAvailable);
                        if (!isAvailable) {
                            service.setLastDownAt(Instant.now());
                        }
                        Notifier.notify(service);
                    }
                    service.setLastCheckAt(Instant.now());
                    ServiceStore.persist(service, false);
                } else {
                    System.out.println("    Already checked");
                }
            }

            try {
                Thread.sleep(Duration.ofSeconds(60).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
