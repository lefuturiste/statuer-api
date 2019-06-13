package fr.lefuturiste.statuer.models;

import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.ZoneId;

@Entity(name = "Incident")
public class Incident {

    @Id
    private String id;

    private String name;

    private String description;

    private String impact; // 'high' or 'low'

    @NotNull
    private Instant startedAt;

    private Instant finishedAt;

    @ManyToOne
    private Service service;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public JSONObject toJSONObject(int deep) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("description", description);
        jsonObject.put("impact", impact);
        jsonObject.put("started_at", startedAt);
        jsonObject.put("finished_at", finishedAt);
        if (deep == 1) {
            jsonObject.put("service", getService().toJSONObject(0));
        }
        return jsonObject;
    }

}