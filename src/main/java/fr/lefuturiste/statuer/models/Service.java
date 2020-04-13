package fr.lefuturiste.statuer.models;

import org.hibernate.validator.constraints.URL;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Service")
public class Service {

    @Id
    @NotNull
    private String id;

    @NotNull
    @NotEmpty
    private String name;

    @URL
    private String discordWebhook;

    private String status;

    private Integer checkPeriod = 120; // in seconds (60,120,180,240,300,420,600,900,1800,3600)

    private Integer httpExpectedStatus = 200; // 200|400

    @URL
    private String url;

    private String type; // http/socket

    private int timeout; // in seconds

    private Instant lastCheckAt;

    private Instant lastDownAt;

    @ManyToOne
    private Project project;

    @OneToMany(mappedBy = "service", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Incident> incidents = new ArrayList<>();

    @OneToOne
    private Incident lastIncident;

    private float uptime;

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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public JSONObject toJSONObject(int deep) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("url", url);
        jsonObject.put("type", type);
        jsonObject.put("is_available", getAvailable());
        jsonObject.put("status", status);
        jsonObject.put("check_period", checkPeriod);
        jsonObject.put("http_expected_status", httpExpectedStatus);
        jsonObject.put("discord_webhook", discordWebhook);
        jsonObject.put("last_checked_at", lastCheckAt != null ? lastCheckAt.toString() : null);
        jsonObject.put("last_incident", lastIncident != null ? lastIncident.toString() : null);
        jsonObject.put("uptime", uptime);
        jsonObject.put("timeout", timeout);
        if (deep == 1) {
            jsonObject.put("project", getProject().toJSONObject(0));
        }
        return jsonObject;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getHttpExpectedStatus() {
        return httpExpectedStatus;
    }

    public void setHttpExpectedStatus(int httpExpectedStatus) {
        this.httpExpectedStatus = httpExpectedStatus;
    }

    public int getCheckPeriod() {
        return checkPeriod;
    }

    public void setCheckPeriod(int checkPeriod) {
        this.checkPeriod = checkPeriod;
    }

    public Boolean getAvailable() {
        return (status == null) ? null : status.equals("up");
    }

    public void setAvailable(Boolean available) {
        status = available ? "up" : "down";
    }

    public void setStatus(String newStatus) {
        status = newStatus;
    }

    public String getStatus() {
        return status;
    }

    public String getDiscordWebhook() {
        return discordWebhook;
    }

    public void setDiscordWebhook(String discordWebhook) {
        this.discordWebhook = discordWebhook;
    }

    public Instant getLastCheckAt() {
        return lastCheckAt;
    }

    public void setLastCheckAt(Instant lastCheckAt) {
        this.lastCheckAt = lastCheckAt;
    }

    public String getPath() {
        return getProject().getNamespace().getName() + "." + getProject().getName() + "." + this.name;
    }

    public Instant getLastDownAt() {
        return lastDownAt;
    }

    public void setLastDownAt(Instant lastDownAt) {
        this.lastDownAt = lastDownAt;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public Incident getLastIncident() {
        return lastIncident;
    }

    public void setLastIncident(Incident lastIncident) {
        this.lastIncident = lastIncident;
    }

    public float getUptime() {
        return uptime;
    }

    public void setUptime(float uptime) {
        this.uptime = uptime;
    }
}