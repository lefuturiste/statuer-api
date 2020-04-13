package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.controllers.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;
import spark.Spark;

import java.time.Duration;
import java.util.Objects;

public class App {
    private static CheckThread checkThread;
    public static Logger logger = LoggerFactory.getLogger(App.class);
    private static long startTime;

    public static void main(String[] args) {
        startTime = System.currentTimeMillis();
        logger.info("Starting application...");
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing()
                .directory(System.getProperty("user.dir"))
                .load();

        HibernateService.setConfig(
                dotenv.get("MYSQL_CONNECTION_URL"),
                dotenv.get("MYSQL_USERNAME"),
                dotenv.get("MYSQL_PASSWORD")
        );
        System.setProperty("user.timezone", "Europe/Paris");
        new DiscordBot(
                dotenv.get("DISCORD_CLIENT_ID"),
                dotenv.get("DISCORD_BOT_TOKEN")
        ).start();
        checkThread = new CheckThread();
        Spark.port(Integer.valueOf(Objects.requireNonNull(dotenv.get("PORT"))));
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
        while (true) {
            try {
                checkThread.run();
            } catch (Exception checkThreadException) {
                checkThreadException.printStackTrace();
                logger.error("Check thread has exited due to an exception. Restarting in 10 seconds...");
                try {
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }

    public static String returnJSON(Response response, JSONObject jsonObject) {
        response.header("Content-type", "application/json");
        return jsonObject.toString(0);
    }

    public static void notifyUpdateOnService() {
        checkThread.updateService();
    }

    public static Duration getUpTime() {
        return Duration.ofMillis(System.currentTimeMillis() - startTime);
    }
}
