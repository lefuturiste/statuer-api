package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.models.Service;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class Notifier {
    private static OkHttpClient httpClient = new OkHttpClient();

    public static void notify(Service service) {
        // search for a discord webhooks
        String discordWebhook;
        discordWebhook = service.getDiscordWebhook();
        if (discordWebhook == null) {
            discordWebhook = service.getProject().getDiscordWebhook();
            if (discordWebhook == null) {
                discordWebhook = service.getProject().getNamespace().getDiscordWebhook();
            }
        }
        if (discordWebhook != null) {
            System.out.println(" We will send a discord webhook");
            String status = service.getAvailable() ? "up": "down";
            String extra = "";
            if (status.equals("up")) {
                Duration duration = Duration.between(Instant.now(), service.getLastDownAt()).abs();
                long s = duration.getSeconds();
                extra += " With an estimated down time of " + String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
            }
            Request request = new Request.Builder()
                    .method("POST", RequestBody.create(
                            MediaType.get("application/json; charset=utf-8"),
                            new JSONObject()
                                    .put("content", service.getPath() + " is now " + status + " !" + extra)
                                    .toString(0)))
                    .url(discordWebhook).build();
            try {
                httpClient.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
