package Sync.Intent.Bloat;

import Sync.Intent.Ducks.Packets.ButtonListener;
import Sync.Intent.Ducks.Packets.CommandListener;
import Sync.Intent.Ducks.Packets.StartListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Network {
    private static JDA jda;

    public static void JDA() {
        jda = JDABuilder.createDefault(Spark.pool).enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS).addEventListeners(new StartListener(), new CommandListener(), new ButtonListener()).build();
    }
    public static JDA getJda() {
        return jda;
    }
}
