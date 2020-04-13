package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.controllers.DiscordCommandsController;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordBot {
    private JDA jda;

    private String clientId;

    private String token;

    private static List<String> managerRoles = Arrays.asList("can-deploy", "can-manage", "statuer");

    private static List<String> managerCommands = Arrays.asList("create", "edit", "delete");

    DiscordBot(String clientId, String token) {
        this.clientId = clientId;
        //  https://discordapp.com/oauth2/authorize?client_id=INSERT_CLIENT_ID_HERE&scope=bot&permissions=0
        this.token = token;
    }

    void start() {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .addEventListener(new EventListener())
                    .buildBlocking();
            jda.getPresence().setGame(Game.of(Game.GameType.WATCHING, "??|%%|&&|## and a lot of services"));
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    String getAuthorizeUrl() {
        int permissionInteger = 537128000;
        return "https://discordapp.com/oauth2/authorize?client_id=" + clientId + "&scope=bot&permissions=" + permissionInteger;
    }

    public static void warn(MessageChannel channel) {
        warn(channel, "An unknown error as occurred");
    }

    public static void success(MessageChannel channel) {
        success(channel, "Success!");
    }

    public static void success(MessageChannel channel, String message) {
        channel.sendMessage(":white_check_mark: " + message).complete();
    }

    public static void warn(MessageChannel channel, String message) {
        channel.sendMessage(":warning: " + message).complete();
    }

    public static void usage(MessageChannel channel, String message) {
        channel.sendMessage(":interrobang: " + message).complete();
    }

    private static void error(MessageChannel channel, Exception exception) {
        EmbedBuilder builder = new EmbedBuilder().setTitle(":red_circle: Exception occurred!");
        StringBuilder description = new StringBuilder("**" + exception.toString() + "** \n");
        for (StackTraceElement stackElement : exception.getStackTrace())
            description.append(stackElement.toString().replace("at ", "")).append("\n");
        channel.sendMessage(builder.setDescription(description).build()).complete();
    }

    class EventListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            try {
                if (!event.isFromType(ChannelType.PRIVATE)) {
                    Message message = event.getMessage();
                    if (message.getContentDisplay().length() > 2) {

                        String messagePrefix = message.getContentDisplay().substring(0, 2);
                        if (
                                message.isMentioned(jda.getSelfUser()) ||
                                        messagePrefix.equals("??") ||
                                        messagePrefix.equals("%%") ||
                                        messagePrefix.equals("&&") ||
                                        messagePrefix.equals("##")
                                ) {
                            String rawCommand = message.getContentDisplay().substring(2);
                            String[] commandComponents = rawCommand.split(" ");

                            // verify permission
                            if (managerCommands.contains(commandComponents[0]) &&
                                    event.getMember().getRoles().stream().filter(
                                            role -> DiscordBot.managerRoles.contains(role.getName())
                                    ).collect(Collectors.toList()).size() == 0) {
                                DiscordBot.warn(event.getChannel(), "Get the fuck out of my store, we are closed (permission issue)");
                                return;
                            }

                            // 'create' 'param1="something' 'else"'
                            // we look for components with quotes
                            String pair = null;
                            ArrayList<String> newCommandComponents = new ArrayList<>();
                            for (String commandComponent : commandComponents) {
                                if (commandComponent.indexOf('"') != -1 && pair == null) {
                                    pair = commandComponent;
                                } else if (commandComponent.indexOf('"') != -1 && pair != null) {
                                    newCommandComponents.add(pair.substring(1) + ' ' + commandComponent.substring(0, commandComponent.length() - 1));
                                    pair = null;
                                } else {
                                    newCommandComponents.add(commandComponent);
                                }
                            }
                            if (pair != null) {
                                newCommandComponents.add(pair.substring(1, pair.length() - 1));
                            }

                            commandComponents = newCommandComponents.toArray(new String[0]);
                            switch (commandComponents[0]) {
                                case "debug":
                                    StringBuilder output = new StringBuilder("```\n");
                                    for (String commandComponent : commandComponents)
                                        output.append(commandComponent).append("\n");
                                    output.append("```");
                                    event.getChannel().sendMessage(output.toString()).complete();
                                    break;
                                case "ping":
                                    DiscordCommandsController.ping.run(event, commandComponents);
                                    break;
                                case "about":
                                    DiscordCommandsController.about.run(event, commandComponents);
                                    break;
                                case "help":
                                    DiscordCommandsController.help.run(event, commandComponents);
                                    break;
                                case "get":
                                    DiscordCommandsController.get.run(event, commandComponents);
                                    break;
                                case "create":
                                    DiscordCommandsController.create.run(event, commandComponents);
                                    break;
                                case "edit":
                                    DiscordCommandsController.edit.run(event, commandComponents);
                                    break;
                                case "delete":
                                    DiscordCommandsController.delete.run(event, commandComponents);
                                    break;
                                default:
                                    DiscordBot.warn(event.getChannel(), "Unknown command!");
                                    break;
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                DiscordBot.error(event.getChannel(), exception);
            }
        }
    }
}
