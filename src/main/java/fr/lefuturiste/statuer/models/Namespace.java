package fr.lefuturiste.statuer.models;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Namespace")
public class Namespace {

    @Id
    private String id;

    private String name;

    private String imageUrl;

    private String discordWebhook;

    @OneToMany(mappedBy = "namespace", cascade = CascadeType.REMOVE)
    private List<Project> projects = new ArrayList<>();

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

    public List<Project> getProjects() {
        return projects;
    }

    public void addProject(Project project) {
        this.projects.add(project);
    }

    public JSONObject toJSONObject(int deep) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        JSONArray projectsJson = new JSONArray();
        for (Project project : getProjects()) {
            projectsJson.put(project.toJSONObject(0));
        }
        jsonObject.put("projects", projectsJson);
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
}