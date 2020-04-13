package fr.lefuturiste.statuer.checker;

import fr.lefuturiste.statuer.models.Service;

public interface CheckerInterface {

    /**
     * Will perform a check on the service url to work out if this service is UP or DOWN
     *
     * @param service The service to check
     * @return boolean
     */
    boolean isAvailable(Service service);
}
