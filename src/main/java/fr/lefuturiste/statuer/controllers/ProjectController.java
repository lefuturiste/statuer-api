package fr.lefuturiste.statuer.controllers;

import fr.lefuturiste.statuer.App;
import fr.lefuturiste.statuer.Validator;
import fr.lefuturiste.statuer.models.Namespace;
import fr.lefuturiste.statuer.models.Project;
import fr.lefuturiste.statuer.stores.NamespaceStore;
import fr.lefuturiste.statuer.stores.ProjectStore;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Route;

import java.util.List;
import java.util.UUID;

public class ProjectController {
    public static Route getMany = (req, res) -> {
        List<Project> projects = ProjectStore.getMany();
        JSONArray projectsJson = new JSONArray();
        for (Project project : projects) {
            projectsJson.put(project.toJSONObject(1));
        }
        res.status(200);
        return new JSONObject()
                .put("data", projectsJson)
                .put("success", true);
    };

    public static Route getOne = (req, res) -> {
        Project project = ProjectStore.getOne(UUID.fromString(req.params("id")));
        if (project == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Project not Found");
        }
        res.status(200);
        return new JSONObject()
                .put("data", project.toJSONObject(1))
                .put("success", true);
    };

    public static Route store = (req, res) -> {
        JSONObject body = new JSONObject(req.body());
        Project project = new Project();
        project.setId(UUID.randomUUID().toString());
        project.setSlug(body.getString("slug"));
        if (body.has("name"))
            project.setName(body.getString("name"));
        if (body.has("discord_webhook"))
            project.setDiscordWebhook(body.getString("discord_webhook"));
        Namespace namespace = NamespaceStore.getOne(UUID.fromString(body.getString("namespace_id")));
        if (namespace == null) {
            res.status(404);
            return App.returnJSON(res, new JSONObject()
                    .put("success", false)
                    .put("error", "Namespace not Found"));
        }
        project.setNamespace(namespace);
        Validator<Project> validator = new Validator<>(project);
        if (!validator.isValid()) {
            res.status(404);
            return App.returnJSON(res, new JSONObject()
                    .put("success", false)
                    .put("errors", validator.getJSONErrors()));
        }
        ProjectStore.persist(project);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };

    public static Route update = (req, res) -> {
        JSONObject body = new JSONObject(req.body());
        Project project = ProjectStore.getOne(UUID.fromString(req.params("id")));

        if (project == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Project not Found");
        }
        if (body.has("name"))
            project.setName(body.getString("name"));
        if (body.has("slug"))
            project.setSlug(body.getString("slug"));
        if (body.has("discord_webhook"))
            project.setDiscordWebhook(body.getString("discord_webhook"));
        ProjectStore.persist(project);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };

    public static Route delete = (req, res) -> {
        Project project = ProjectStore.getOne(UUID.fromString(req.params("id")));

        if (project == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Project not Found");
        }
        ProjectStore.delete(project);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };
}
