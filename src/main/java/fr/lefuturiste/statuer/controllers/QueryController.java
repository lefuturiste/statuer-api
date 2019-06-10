package fr.lefuturiste.statuer.controllers;

import fr.lefuturiste.statuer.App;
import fr.lefuturiste.statuer.Validator;
import fr.lefuturiste.statuer.models.Namespace;
import fr.lefuturiste.statuer.models.Project;
import fr.lefuturiste.statuer.models.Service;
import fr.lefuturiste.statuer.stores.NamespaceStore;
import fr.lefuturiste.statuer.stores.ProjectStore;
import fr.lefuturiste.statuer.stores.QueryStore;
import fr.lefuturiste.statuer.stores.ServiceStore;
import org.json.JSONObject;
import spark.Route;

import java.util.UUID;

public class QueryController {
    public static Route get = (req, res) -> {
        String path = req.params("path");
        QueryStore.ObjectQueryResult objectQueryResult = QueryStore.getObjectsFromQuery(path);

        if (objectQueryResult == null) {
            res.status(400);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Invalid path parameter");
        }
        JSONObject data = new JSONObject();
        if (objectQueryResult.namespace == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Namespace not Found");
        }
        if (objectQueryResult.projectName == null && objectQueryResult.serviceName == null) {
            data.put("namespace", objectQueryResult.namespace.toJSONObject(1));
            data.put("type", "namespace");
        } else if (objectQueryResult.serviceName == null) {
            if (objectQueryResult.project == null) {
                res.status(404);
                return new JSONObject()
                        .put("success", false)
                        .put("error", "Project not Found");
            }
            data.put("type", "project");
            data.put("project", objectQueryResult.project.toJSONObject(0));
        } else {
            if (objectQueryResult.service == null) {
                res.status(404);
                return new JSONObject()
                        .put("success", false)
                        .put("error", "Service not Found");
            }
            data.put("type", "service");
            data.put("service", objectQueryResult.service.toJSONObject(0));
        }

        return App.returnJSON(res, new JSONObject()
                .put("success", true)
                .put("data", data));
    };

    public static Route create = (req, res) -> {
        String path = req.params("path");
        QueryStore.ObjectQueryResult objectQueryResult = QueryStore.getObjectsFromQuery(path);

        if (objectQueryResult == null) {
            res.status(400);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Invalid path parameter");
        }

        JSONObject created = new JSONObject();
        Namespace namespace;
        if (objectQueryResult.namespace == null) {
            // create that namespace
            namespace = new Namespace();
            namespace.setId(UUID.randomUUID().toString());
            namespace.setName(objectQueryResult.namespaceName);
            NamespaceStore.persist(namespace);
            created.put("namespace", namespace.toJSONObject(0));
        } else {
            namespace = objectQueryResult.namespace;
        }
        if (objectQueryResult.projectName != null) {
            Project project;
            if (objectQueryResult.project == null) {
                project = new Project();
                project.setId(UUID.randomUUID().toString());
                project.setName(objectQueryResult.projectName);
                project.setNamespace(namespace);
                ProjectStore.persist(project);
                created.put("project", project.toJSONObject(0));
            } else {
                project = objectQueryResult.project;
            }
            if (objectQueryResult.serviceName != null) {
                Service service;
                if (objectQueryResult.service == null) {
                    service = new Service();
                    service.setId(UUID.randomUUID().toString());
                    service.setName(objectQueryResult.serviceName);
                    service.setProject(project);
                    ServiceStore.persist(service);
                    created.put("service", service.toJSONObject(1));
                }
            }
        }

        return App.returnJSON(res, new JSONObject()
                .put("success", true)
                .put("have_created", created.length() != 0)
                .put("created", created));
    };

    public static Route update = (req, res) -> {
        String path = req.params("path");
        QueryStore.ObjectQueryResult objectQueryResult = QueryStore.getObjectsFromQuery(path);

        if (objectQueryResult == null) {
            res.status(400);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Invalid path parameter");
        }

        JSONObject body = new JSONObject(req.body());

        if (objectQueryResult.service != null) {
            // update service
            if (body.has("name")) {
                objectQueryResult.service.setName(body.getString("name"));
            }
            if (body.has("type")) {
                objectQueryResult.service.setType(body.getString("type"));
            }
            if (body.has("check_period")) {
                objectQueryResult.service.setCheckPeriod(body.getInt("check_period"));
            }
            if (body.has("timeout")) {
                objectQueryResult.service.setTimeout(body.getInt("timeout"));
            }
            if (body.has("url")) {
                objectQueryResult.service.setUrl(body.getString("url"));
            }
            if (body.has("http_expected_status")) {
                objectQueryResult.service.setHttpExpectedStatus(body.getInt("http_expected_status"));
            }
            Validator<Service> validator = new Validator<>(objectQueryResult.service);
            if (!validator.isValid()) {
                res.status(404);
                return App.returnJSON(res, new JSONObject()
                        .put("success", false)
                        .put("errors", validator.getJSONErrors()));
            }

            ServiceStore.persist(objectQueryResult.service);
            App.notifyUpdateOnService();
        } else {
            if (objectQueryResult.serviceName != null) {
                res.status(404);
                return new JSONObject()
                        .put("success", false)
                        .put("error", "Service not found");
            }
            if (objectQueryResult.project != null) {
                // update project
                if (body.has("name")) {
                    objectQueryResult.project.setName(body.getString("name"));
                }
                if (body.has("discord_webhook")) {
                    objectQueryResult.project.setDiscordWebhook(body.getString("discord_webhook"));
                }

                Validator<Project> validator = new Validator<>(objectQueryResult.project);
                if (!validator.isValid()) {
                    res.status(404);
                    return App.returnJSON(res, new JSONObject()
                            .put("success", false)
                            .put("errors", validator.getJSONErrors()));
                }

                ProjectStore.persist(objectQueryResult.project);
                App.notifyUpdateOnService();
            } else {
                if (objectQueryResult.projectName != null) {
                    res.status(404);
                    return new JSONObject()
                            .put("success", false)
                            .put("error", "Project not found");
                }
                if (objectQueryResult.namespace != null) {
                    // update project
                    if (body.has("name")) {
                        objectQueryResult.namespace.setName(body.getString("name"));
                    }
                    if (body.has("discord_webhook")) {
                        objectQueryResult.namespace.setDiscordWebhook(body.getString("discord_webhook"));
                    }

                    Validator<Namespace> validator = new Validator<>(objectQueryResult.namespace);
                    if (!validator.isValid()) {
                        res.status(404);
                        return App.returnJSON(res, new JSONObject()
                                .put("success", false)
                                .put("errors", validator.getJSONErrors()));
                    }

                    NamespaceStore.persist(objectQueryResult.namespace);
                    App.notifyUpdateOnService();
                } else {
                    res.status(404);
                    return new JSONObject()
                            .put("success", false)
                            .put("error", "Namespace not found");
                }
            }
        }

        return App.returnJSON(res, new JSONObject()
                .put("success", true));
    };

    public static Route delete = (req, res) -> {
        String path = req.params("path");
        QueryStore.ObjectQueryResult objectQueryResult = QueryStore.getObjectsFromQuery(path);

        if (objectQueryResult == null) {
            res.status(400);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Invalid path parameter");
        }

        if (objectQueryResult.service != null) {
            // just delete the service
            ServiceStore.delete(objectQueryResult.service);
        } else {
            if (objectQueryResult.serviceName != null) {
                res.status(404);
                return new JSONObject()
                        .put("success", false)
                        .put("error", "Service not found");
            }
            if (objectQueryResult.project != null) {
                ProjectStore.delete(objectQueryResult.project);
            } else {
                if (objectQueryResult.projectName != null) {
                    res.status(404);
                    return new JSONObject()
                            .put("success", false)
                            .put("error", "Project not found");
                }
                if (objectQueryResult.namespace != null) {
                    NamespaceStore.delete(objectQueryResult.namespace);
                } else {
                    res.status(404);
                    return new JSONObject()
                            .put("success", false)
                            .put("error", "Namespace not found");
                }
            }
        }

        return App.returnJSON(res, new JSONObject()
                .put("success", true));
    };
}
