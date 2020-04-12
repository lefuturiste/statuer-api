package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.controllers.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import spark.Response;
import spark.Spark;

public class App {
    private static CheckThread checkThread;
    private static DiscordBot discordBot;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .load();
        HibernateService.setConfig(
                dotenv.get("MYSQL_CONNECTION_URL"),
                dotenv.get("MYSQL_USERNAME"),
                dotenv.get("MYSQL_PASSWORD")
        );
        System.setProperty("user.timezone", "Europe/Paris");
//        new OnStart().run();
        discordBot = new DiscordBot(
                dotenv.get("DISCORD_CLIENT_ID"),
                dotenv.get("DISCORD_BOT_TOKEN")
        );
        System.out.println(discordBot.getAuthorizeUrl());
        checkThread = new CheckThread();
        Spark.port(8080);
        Spark.get("/", (req, res) -> {
            res.status(200);
            return new JSONObject().put("success", true);
        });
        Spark.post("/graphql", "application/json", GraphQLController.execute);
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
        response.header("Content-type", "application/json");
        return jsonObject.toString(0);
    }

    public static void notifyUpdateOnService() {
        checkThread.updateService();
    }
}
