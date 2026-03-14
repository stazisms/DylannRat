package Sync.Intent.Ducks.Packets;

import Sync.Intent.Loader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Optional;

public class StartListener extends ListenerAdapter {
    private static final String TARGET_GUILD_ID = "";

    @Override
    public void onReady(ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(TARGET_GUILD_ID);
        if (guild != null) {
            handleGuildReady(guild);
        }
    }

    private void handleGuildReady(Guild guild) {
        Optional<Category> categoryOpt = guild.getCategories().stream().filter(category -> category.getName().equalsIgnoreCase(Loader.name)).findFirst();
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            Optional<TextChannel> channelOpt = category.getTextChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase("GEN")).findFirst();

            channelOpt.ifPresent(textChannel -> textChannel.sendMessageEmbeds(
                    createEmbed(Loader.name + " is back online!", Color.MAGENTA).build()
            ).queue());
        } else {
            guild.createCategory(Loader.name).queue(category -> {
                category.createTextChannel("gen").queue();
                category.createVoiceChannel("Voice").queue();
            });
        }
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (event.getChannelType() == ChannelType.TEXT) {
            TextChannel textChannel = event.getChannel().asTextChannel();
            Category parentCategory = textChannel.getParentCategory();
            if (textChannel.getName().equalsIgnoreCase("gen") && parentCategory != null && parentCategory.getName().equalsIgnoreCase(Loader.name)) {
                textChannel.sendMessageEmbeds(createEmbed(Loader.name + " has been added to slimnet creds: https://github.com/stazisms", Color.RED).build()).queue();
            }
        }
    }

    private EmbedBuilder createEmbed(String title, Color color) {
        return new EmbedBuilder().setTitle(title, null).setColor(color);
    }
}
