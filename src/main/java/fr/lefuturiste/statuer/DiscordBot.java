package fr.lefuturiste.statuer;

import fr.lefuturiste.statuer.controllers.DiscordCommandsController;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class DiscordBot {
    private JDA jda;

    private String clientId;

    DiscordBot(String clientId, String token) {
        this.clientId = clientId;
        //  https://discordapp.com/oauth2/authorize?client_id=INSERT_CLIENT_ID_HERE&scope=bot&permissions=0

        try {
            jda = new JDABuilder(token).build();
            jda.addEventListener(new EventListener());
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
        } catch (LoginException e) {
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

    class EventListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
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
                        switch (commandComponents[0]) {
                            case "ping":
                                DiscordCommandsController.ping.run(event, commandComponents);
                                break;
                            case "about":
                                DiscordCommandsController.about.run(event, commandComponents);
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
                            default:
                                event.getChannel().sendMessage("Invalid command").complete();
                                break;
                        }
                    }
                }
            }
        }
    }
}
