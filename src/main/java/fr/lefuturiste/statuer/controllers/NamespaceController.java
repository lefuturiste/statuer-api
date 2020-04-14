package fr.lefuturiste.statuer.controllers;

import fr.lefuturiste.statuer.models.Namespace;
import fr.lefuturiste.statuer.stores.NamespaceStore;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Route;

import java.util.List;
import java.util.UUID;

public class NamespaceController {

    public static Route getMany = (req, res) -> {
        List<Namespace> namespaces = NamespaceStore.getMany();
        JSONArray namespacesJson = new JSONArray();
        for (Namespace namespace : namespaces) {
            namespacesJson.put(namespace.toJSONObject(1));
        }
        res.status(200);
        return new JSONObject()
                .put("data", namespacesJson)
                .put("success", true);
    };

    public static Route getOne = (req, res) -> {
        Namespace namespace = NamespaceStore.getOne(UUID.fromString(req.params("id")));

        if (namespace == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Namespace not Found");
        }
        res.status(200);
        return new JSONObject()
                .put("data", namespace.toJSONObject(1))
                .put("success", true);
    };

    public static Route store = (req, res) -> {
        JSONObject body = new JSONObject(req.body());
        Namespace namespace = new Namespace();
        namespace.setId(UUID.randomUUID().toString());
        namespace.setSlug(body.getString("slug"));
        NamespaceStore.persist(namespace);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };

    public static Route update = (req, res) -> {
        JSONObject body = new JSONObject(req.body());
        Namespace namespace = NamespaceStore.getOne(UUID.fromString(req.params("id")));
        if (namespace == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Namespace not Found");
        }
        if (body.has("slug"))
            namespace.setSlug(body.getString("slug"));
        if (body.has("name"))
            namespace.setName(body.getString("name"));
        NamespaceStore.persist(namespace);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };

    public static Route delete = (req, res) -> {
        Namespace namespace = NamespaceStore.getOne(UUID.fromString(req.params("id")));

        if (namespace == null) {
            res.status(404);
            return new JSONObject()
                    .put("success", false)
                    .put("error", "Namespace not Found");
        }
        NamespaceStore.delete(namespace);
        res.status(200);
        return new JSONObject()
                .put("success", true);
    };
}
