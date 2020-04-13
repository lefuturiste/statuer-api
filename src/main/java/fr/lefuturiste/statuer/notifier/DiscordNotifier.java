package fr.lefuturiste.statuer.notifier;

import fr.lefuturiste.statuer.App;
import fr.lefuturiste.statuer.models.Incident;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.webhook.WebhookClientBuilder;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class DiscordNotifier implements NotifierInterface {

    public void notify(Incident incident) {
        // search for a discord webhooks
        String discordWebhook;
        discordWebhook = incident.getService().getProject().getNamespace().getDiscordWebhook();
        if (discordWebhook == null) {
            discordWebhook = incident.getService().getProject().getDiscordWebhook();
            if (discordWebhook == null) {
                discordWebhook = incident.getService().getDiscordWebhook();
            }
        }
        if (discordWebhook != null) {
            App.logger.debug("Send a discord webhook");
            String status = incident.getService().getAvailable() ? "up": "down";
            EmbedBuilder embed = new EmbedBuilder();
            embed
                    .addField("Incident id", incident.getId(), true)
                    .addBlankField(true)
                    .addField("Service url", incident.getService().getUrl(), true)
                    .setDescription(incident.getService().getPath() + " is now " + status + "!");
            if (status.equals("up")) {
                embed
                    .addField("Started at", DateTimeFormatter.ISO_INSTANT.format(incident.getStartedAt()), true)
                    .setColor(Color.decode("#27ae60")).setTitle("End of an incident!");
                Duration duration = Duration.between(Instant.now(), incident.getService().getLastDownAt()).abs();
                long s = duration.getSeconds();
                embed.addField("Estimated down time", String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60)), true);
            } else {
                embed.setColor(Color.decode("#e74c3c")).setTitle("New incident!");
            }
            new WebhookClientBuilder(discordWebhook).build().send(embed.build());
        }
    }
}
