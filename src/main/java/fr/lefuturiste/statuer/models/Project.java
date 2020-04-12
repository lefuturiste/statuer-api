package fr.lefuturiste.statuer.models;

import org.hibernate.validator.constraints.URL;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Project")
public class Project {

    @Id
    private String id;

    @NotNull
    @NotEmpty
    private String name;

    private String imageUrl;

    @URL
    private String discordWebhook;

    @ManyToOne
    private Namespace namespace;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    private List<Service> services = new ArrayList<>();

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

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public JSONObject toJSONObject(int deep) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("discord_webhook", discordWebhook);
        JSONArray servicesJson = new JSONArray();
        for (Service service : getServices()) {
            servicesJson.put(service.toJSONObject(0));
        }
        jsonObject.put("services", servicesJson);
        if (deep == 1) {
            jsonObject.put("namespace", getNamespace().toJSONObject(0));
        }
        return jsonObject;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDiscordWebhook() {
        return discordWebhook;
    }

    public void setDiscordWebhook(String discordWebhook) {
        this.discordWebhook = discordWebhook;
    }

    public List<Service> getServices() {
        return services;
    }

    public String getPath() {
        return this.getNamespace().getName() + "." + this.name;
    }
}