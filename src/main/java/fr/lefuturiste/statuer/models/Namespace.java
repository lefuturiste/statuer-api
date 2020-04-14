package fr.lefuturiste.statuer.models;

import org.hibernate.validator.constraints.URL;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The namespace entity represent a organization, a corporation eg. Github, Google or ONU
 */
@Entity(name = "Namespace")
public class Namespace {

    /**
     * A UUID for the namespace
     */
    @Id
    @NotNull
    private String id;

    /**
     * The usable slug of the namespace eg. stc or google
     */
    @NotNull
    @NotEmpty
    private String slug;

    /**
     * The full name of the namespace eg. STAN-TAb Corp. or Google Inc.
     */
    private String name;

    /**
     * A url which reference to the logo of the namespace
     */
    @URL
    private String imageUrl;

    @URL
    private String discordWebhook;

    /**
     * All the projects of the namespace
     */
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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
        jsonObject.put("slug", name);
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

    public String getHidedDiscordWebpack() {
        return discordWebhook.substring(0, discordWebhook.length() - 30) + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    }
}