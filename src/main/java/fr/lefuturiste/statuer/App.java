package fr.lefuturiste.statuer;

import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import fr.lefuturiste.statuer.controllers.NamespaceController;
import fr.lefuturiste.statuer.controllers.ProjectController;
import fr.lefuturiste.statuer.controllers.QueryController;
import fr.lefuturiste.statuer.controllers.ServiceController;
import fr.lefuturiste.statuer.models.Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import spark.Response;
import spark.Spark;

import java.io.IOException;

public class App {
    private static CheckThread checkThread;

    public static void main(String[] args) {
        checkThread = new CheckThread();
        Spark.port(8080);
        Spark.get("/", (req, res) -> {
            res.status(200);
            return new JSONObject().put("success", true);
        });
        Spark.get("/query/:path", "application/json", QueryController.get);
        Spark.post("/query/:path", "application/json", QueryController.create);
        Spark.put("/query/:path", "application/json", QueryController.update);
        Spark.delete("/query/:path", "application/json", QueryController.delete);
        Spark.path("/namespace", () -> {
            Spark.get("", "application/json", NamespaceController.getMany);
            Spark.get("/:id", "application/json", NamespaceController.getOne);
            Spark.post("", "application/json", NamespaceController.store);
            Spark.put("/:id", "application/json", NamespaceController.update);
            Spark.delete("/:id", "application/json", NamespaceController.delete);
        });
        Spark.path("/project", () -> {
            Spark.get("", "application/json", ProjectController.getMany);
            Spark.get("/:id", "application/json", ProjectController.getOne);
            Spark.post("", "application/json", ProjectController.store);
            Spark.put("/:id", "application/json", ProjectController.update);
            Spark.delete("/:id", "application/json", ProjectController.delete);
        });
        Spark.path("/service", () -> {
            Spark.get("", "application/json", ServiceController.getMany);
            Spark.get("/:id", "application/json", ServiceController.getOne);
            Spark.post("", "application/json", ServiceController.store);
            Spark.put("/:id", "application/json", ServiceController.update);
            Spark.delete("/:id", "application/json", ServiceController.delete);
        });
        Spark.awaitInitialization();
        checkThread.run();
    }

    public static String returnJSON(Response response, JSONObject jsonObject) {
        response.header("Content-Type", "application/json");
        return jsonObject.toString(0);
    }

    public static void notifyUpdateOnService() {
        checkThread.updateService();
    }
}
