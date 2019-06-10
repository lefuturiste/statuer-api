package fr.lefuturiste.statuer.controllers;

import fr.lefuturiste.statuer.App;
import fr.lefuturiste.statuer.Validator;
import fr.lefuturiste.statuer.models.Project;
import fr.lefuturiste.statuer.models.Service;
import fr.lefuturiste.statuer.stores.ProjectStore;
import fr.lefuturiste.statuer.stores.ServiceStore;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Route;

import java.util.List;
import java.util.UUID;

public class ServiceController {
    public static Route getMany = (req, res) -> {
        List<Service> services = ServiceStore.getMany();
        JSONArray servicesJson = new JSONArray();
        for (Service service : services) {
            servicesJson.put(service.toJSONObject(1));
        }
        res.status(200);
        return new JSONObject()
                .put("data", servicesJson)
                .put("success", true);
    };

    public static Route getOne = (req, res) -> {
        Service service = ServiceStore.getOne(UUID.fromString(req.params("id")));

        if (service == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Service not Found");
        }
        res.status(200);
        return new JSONObject()
                .put("data", service.toJSONObject(1))
                .put("success", true);
    };

    public static Route store = (req, res) -> {
        JSONObject body = new JSONObject(req.body());
        Service service = new Service();
        service.setId(UUID.randomUUID().toString());
        service.setName(body.getString("name"));
        service.setUrl(body.getString("url"));
        if (body.has("type")) {
            service.setType(body.getString("type"));
        }
        if (body.has("timeout")) {
            service.setTimeout(body.getInt("timeout"));
        }
        if (body.has("http_expected_status")) {
            service.setHttpExpectedStatus(body.getInt("http_expected_status"));
        }
        service.setCheckPeriod(body.getInt("check_period"));
        if (body.has("discord_webhook")) {
            service.setDiscordWebhook(body.getString("discord_webhook"));
        }
        Validator<Service> validator =  new Validator<>(service);
        if (!validator.isValid()) {
            res.status(400);
            return App.returnJSON(res, new JSONObject()
                    .put("success", false)
                    .put("errors", validator.getJSONErrors()));
        }
        Project project = ProjectStore.getOne(UUID.fromString(body.getString("project_id")));
        if (project == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Project not Found");
        }
        service.setProject(project);
        ServiceStore.persist(service);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };

    public static Route update = (req, res) -> {
        JSONObject body = new JSONObject(req.body());
        Service service = ServiceStore.getOne(UUID.fromString(req.params("id")));

        if (service == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Service not Found");
        }
        if (body.has("name")) {
            service.setName(body.getString("name"));
        }
        ServiceStore.persist(service);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };

    public static Route delete = (req, res) -> {
        Service service = ServiceStore.getOne(UUID.fromString(req.params("id")));

        if (service == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Service not Found");
        }
        ServiceStore.delete(service);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };
}
