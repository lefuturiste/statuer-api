package fr.lefuturiste.statuer;


public class OnStart implements Runnable {

    @Override
    public void run() {
//        System.out.println(Objects.requireNonNull(IncidentStore.getOne(UUID.fromString("798641ce-b874-4167-a8ec-80c71903d3d6"))).toJSONObject(1));
        /*Incident incident = new Incident();
        incident.setId(UUID.randomUUID().toString());
        incident.setName("My incident #1");
        incident.setStartedAt(Instant.now());
        incident.setFinishedAt(Instant.now().plus(Duration.ofMinutes(30)));
        incident.setService(ServiceStore.getOne(UUID.fromString("ae5eb5d1-ee0b-4d77-b081-ce7704f8baa7")));
        IncidentStore.persist(incident);*/
        System.exit(0);
    }
}
