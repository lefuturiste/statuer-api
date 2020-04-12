package fr.lefuturiste.statuer.controllers;

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

import java.awt.*;
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
        switch (pathDecomposed.length) {
            case 1:
                // search for a namespace
                Namespace namespace = NamespaceStore.getOneByName(pathDecomposed[0]);
                if (namespace == null) {
                    DiscordBot.warn(event.getChannel(), "Invalid path: namespace not found");
                    break;
                }

                event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle(namespace.getName())
                        .setDescription("A Statuer's namespace")
                        .setColor(Color.decode("#e74c3c"))
                        .setImage(namespace.getImageUrl())
                        .addField("#uuid", namespace.getId(), false)
                        .addField("Discord webhook", namespace.getDiscordWebhook() == null ? "NULL" : namespace.getDiscordWebhook(), false)
                        .addField("Project count", String.valueOf(namespace.getProjects().size()), false)
                        .addField("Projects", String.join(", ",
                                namespace.getProjects().stream()
                                        .map(Project::getName)
                                        .collect(Collectors.joining(", "))),
                                false)
                        .build()
                ).complete();
                break;
        }
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
            ServiceStore.persist(objectQueryResult.service);
        } else if (objectQueryResult.project != null) {
            // edit project
            if (parameters.containsKey("name")) {
                objectQueryResult.project.setName(parameters.get("name"));
            }
            ProjectStore.persist(objectQueryResult.project);
        } else if (objectQueryResult.namespace != null) {
            // edit project
            if (parameters.containsKey("name")) {
                objectQueryResult.namespace.setName(parameters.get("name"));
            }
            NamespaceStore.persist(objectQueryResult.namespace);
        } else {
            DiscordBot.warn(event.getChannel(), "Invalid path: entity not found");
            return;
        }

        DiscordBot.success(event.getChannel());
    };
}
