package Sync.Intent.Ducks.Packets;

import Sync.Intent.Ducks.Commands.Command;
import Sync.Intent.Ducks.Commands.CommandRegistry;
import Sync.Intent.Loader;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {

    private final String prefix = ".";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.getAuthor().isBot() || !message.getContentRaw().startsWith(prefix)) {
            return;
        }

        if (!event.isFromGuild()) {
            return;
        }

        if (!(event.getChannel() instanceof TextChannel)) {
            return;
        }

        TextChannel textChannel = event.getChannel().asTextChannel();

        if (textChannel.getParentCategory() == null || !textChannel.getParentCategory().getName().equalsIgnoreCase(Loader.name)) {
            return;
        }

        String content = message.getContentRaw().substring(prefix.length()).trim();
        String[] args = content.split("\\s+");
        String commandName = args[0];

        Command cmd = CommandRegistry.get(commandName);
        if (cmd != null) {
            cmd.run(args, textChannel);
        }
    }
}
