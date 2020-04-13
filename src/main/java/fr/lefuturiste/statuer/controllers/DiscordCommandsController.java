package fr.lefuturiste.statuer.controllers;

import fr.lefuturiste.statuer.App;
import fr.lefuturiste.statuer.DiscordBot;
import fr.lefuturiste.statuer.models.Namespace;
import fr.lefuturiste.statuer.models.Project;
import fr.lefuturiste.statuer.models.Service;
import fr.lefuturiste.statuer.stores.NamespaceStore;
import fr.lefuturiste.statuer.stores.ProjectStore;
import fr.lefuturiste.statuer.stores.QueryStore;
import fr.lefuturiste.statuer.stores.ServiceStore;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.hibernate.validator.internal.util.logging.formatter.DurationFormatter;

import java.awt.*;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DiscordCommandsController {

    public interface DiscordCommandRoute {
        void run(MessageReceivedEvent event, String[] commandComponents);
    }

    public static DiscordCommandRoute ping = (event, commandComponents) -> event.getChannel().sendMessage("Pong!").complete();

    public static DiscordCommandRoute about = (event, commandComponents) -> event.getChannel().sendMessage(new EmbedBuilder()
            .setTitle("About statuer")
            .setColor(Color.decode("#e74c3c"))
            .addField("Version", "v1.0", false)
            .addField("Developer", "<@169164454255263745>", false)
            .build()
    ).complete();

    public static DiscordCommandRoute get = (event, commandComponents) -> {
        if (commandComponents.length == 1) {
            DiscordBot.warn(event.getChannel(), "Usage: get <path>");
            return;
        }
        String[] pathDecomposed = commandComponents[1].split("\\.");
        Namespace namespace = NamespaceStore.getOneByName(pathDecomposed[0]);
        if (namespace == null) {
            DiscordBot.warn(event.getChannel(), "Invalid path: namespace not found");
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        Project project = null;
        if (pathDecomposed.length >= 2)
            project = ProjectStore.getOneByNameAndByNamespace(pathDecomposed[1], namespace);
        if (project == null && pathDecomposed.length >= 2) {
            DiscordBot.warn(event.getChannel(), "Invalid path: project not found");
            return;
        }
        switch (pathDecomposed.length) {
            case 1: // show namespace details
                builder.setTitle(namespace.getName())
                        .setDescription("A Statuer's namespace")
                        .setColor(Color.decode("#e74c3c"))
                        .setThumbnail(namespace.getImageUrl())
                        .addField("#uuid", namespace.getId(), false)
                        .addField("Discord webhook", namespace.getDiscordWebhook() == null ? "None" : namespace.getHidedDiscordWebpack(), false)
                        .addField("Project count", String.valueOf(namespace.getProjects().size()), true)
                        .addField("Projects", String.join(", ",
                                namespace.getProjects().stream()
                                        .map(Project::getName)
                                        .collect(Collectors.joining(", "))),
                                true);
                break;
            case 2: // search for a project
                builder.setTitle(project.getPath())
                        .setDescription("A Statuer's project")
                        .setColor(Color.decode("#e74c3c"))
                        .setThumbnail(project.getImageUrl())
                        .addField("#uuid", project.getId(), false)
                        .addField("Services count", String.valueOf(project.getServices().size()), true)
                        .addField("Services", String.join(", ",
                                project.getServices().stream()
                                        .map(Service::getName)
                                        .collect(Collectors.joining(", "))),
                                true);
                break;
            case 3: // search for a service
                Service service = ServiceStore.getOneByNameAndByProject(pathDecomposed[2], project);
                if (service == null) {
                    DiscordBot.warn(event.getChannel(), "Invalid path: service not found");
                    return;
                }
                builder.setTitle(service.getPath())
                        .setDescription("A Statuer's service")
                        .setColor(Color.decode("#e74c3c"))
                        .addField("#uuid", service.getId(), true)
                        .addField("Check period",
                                new DurationFormatter(Duration.ofSeconds(service.getCheckPeriod())).toString(), true)
                        .addField("Url", service.getUrl() == null ? "None": service.getUrl(), true)
                        .addField("Type", service.getType() == null ? "None": service.getType(), true)
                        .addField("Status",
                                service.getStatus() != null ?
                                        service.getStatus().substring(0, 1).toUpperCase() + service.getStatus().substring(1)
                                : "None", true)
                        .addField("Last incident", service.getLastIncident() == null ? "None" : DateTimeFormatter.ISO_INSTANT.format(service.getLastIncident().getFinishedAt()), true)
                        .addField("Uptime", String.valueOf(service.getUptime()), true);

        }
        event.getChannel().sendMessage(builder.build()).complete();
    };

    public static DiscordCommandRoute create = (event, commandComponents) -> {
        if (commandComponents.length == 1) {
            DiscordBot.warn(event.getChannel(), "Usage: create <path>");
            return;
        }
        QueryStore.ObjectQueryResult objectQueryResult = QueryStore.getObjectsFromQuery(commandComponents[1]);

        if (objectQueryResult == null) {
            DiscordBot.warn(event.getChannel(), "Invalid Path");
            return;
        }

        int createdCount = 0;
        Namespace namespace;
        if (objectQueryResult.namespace == null) {
            // create that namespace
            namespace = new Namespace();
            namespace.setId(UUID.randomUUID().toString());
            namespace.setName(objectQueryResult.namespaceName);
            NamespaceStore.persist(namespace);
            createdCount++;
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
                createdCount++;
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
                    createdCount++;
                }
            }
        }
        event.getChannel().sendMessage("Entities created: " + createdCount).complete();
    };

    public static DiscordCommandRoute edit = (event, commandComponents) -> {
        if (commandComponents.length <= 2) {
            DiscordBot.usage(event.getChannel(), "Usage: edit <path> key=value key1=value1 ...");
            return;
        }
        QueryStore.ObjectQueryResult objectQueryResult = QueryStore.getObjectsFromQuery(commandComponents[1]);

        if (objectQueryResult == null) {
            DiscordBot.warn(event.getChannel(), "Invalid path");
            return;
        }
        Map<String, String> parameters = new HashMap<>();
        for (String component: Arrays.copyOfRange(commandComponents, 2, commandComponents.length)) {
            String[] parameterComponents = component.split("=");
            parameters.put(parameterComponents[0], parameterComponents[1]);
        }
        if (objectQueryResult.service != null) {
            // edit service
            if (parameters.containsKey("name")) {
                objectQueryResult.service.setName(parameters.get("name"));
            }
            if (parameters.containsKey("check_period") || parameters.containsKey("period")) {
                String rawPeriod = parameters.containsKey("check_period") ? parameters.get("check_period") : parameters.get("period");
                int period = 0;
                String[] periodComponents = rawPeriod.split(" ");
                for (String component : periodComponents) {
                    if (component.contains("s"))
                        period += Integer.valueOf(component.replace("s", ""));
                    if (component.contains("m"))
                        period += Integer.valueOf(component.replace("m", "")) * 60;
                    if (component.contains("h"))
                        period += Integer.valueOf(component.replace("h", "")) * 3600;
                }
                objectQueryResult.service.setCheckPeriod(period);
            }
            if (parameters.containsKey("url")) {
                objectQueryResult.service.setUrl(parameters.get("url"));
            }
            ServiceStore.persist(objectQueryResult.service);
        } else if (objectQueryResult.project != null) {
            // edit project
            if (parameters.containsKey("name")) {
                objectQueryResult.project.setName(parameters.get("name"));
            }
            if (parameters.containsKey("imageUrl")) {
                objectQueryResult.project.setImageUrl(parameters.get("imageUrl"));
            }
            ProjectStore.persist(objectQueryResult.project);
        } else if (objectQueryResult.namespace != null) {
            // edit namespace
            if (parameters.containsKey("name")) {
                objectQueryResult.namespace.setName(parameters.get("name"));
            }
            if (parameters.containsKey("discordWebhook")) {
                objectQueryResult.namespace.setDiscordWebhook(parameters.get("discordWebhook"));
            }
            if (parameters.containsKey("imageUrl")) {
                objectQueryResult.namespace.setImageUrl(parameters.get("imageUrl"));
            }
            NamespaceStore.persist(objectQueryResult.namespace);
        } else {
            DiscordBot.warn(event.getChannel(), "Invalid path: entity not found");
            return;
        }

        DiscordBot.success(event.getChannel());
    };

    public static DiscordCommandRoute delete = (event, commandComponents) -> {
        if (commandComponents.length == 1) {
            DiscordBot.warn(event.getChannel(), "Usage: delete <path>");
            return;
        }
        String[] pathDecomposed = commandComponents[1].split("\\.");
        Namespace namespace = NamespaceStore.getOneByName(pathDecomposed[0]);
        if (namespace == null) {
            DiscordBot.warn(event.getChannel(), "Invalid path: namespace not found");
            return;
        }
        Project project = null;
        if (pathDecomposed.length >= 2)
            project = ProjectStore.getOneByNameAndByNamespace(pathDecomposed[1], namespace);
        if (project == null && pathDecomposed.length >= 2) {
            DiscordBot.warn(event.getChannel(), "Invalid path: project not found");
            return;
        }
        int deletedCount = 0;
        switch (pathDecomposed.length) {
            case 1:
                deletedCount = NamespaceStore.delete(namespace);
                break;
            case 2:
                deletedCount = ProjectStore.delete(project);
                break;
            case 3:
                Service service = ServiceStore.getOneByNameAndByProject(pathDecomposed[2], project);
                if (service == null) {
                    DiscordBot.warn(event.getChannel(), "Invalid path: service not found");
                    return;
                }
                deletedCount = ServiceStore.delete(service);
        }
        event.getChannel().sendMessage("Entities deleted: " + deletedCount).complete();
    };
}
