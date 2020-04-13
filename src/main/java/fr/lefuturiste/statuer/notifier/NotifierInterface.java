package fr.lefuturiste.statuer.notifier;

import fr.lefuturiste.statuer.models.Incident;

public interface NotifierInterface {

    /**
     * Will notify of start/end of a incident using a notify method
     *
     * @param incident The incident to notify
     */
    void notify(Incident incident);
}
