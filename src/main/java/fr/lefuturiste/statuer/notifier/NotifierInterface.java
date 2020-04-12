package fr.lefuturiste.statuer.notifier;

import fr.lefuturiste.statuer.models.Incident;

public interface NotifierInterface {

    static void notify(Incident incident) {};
}
