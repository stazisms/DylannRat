package Sync.Intent.Ducks.Commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public abstract class Command {
    String[] alias;

    public Command(String[] stringArray) {
        this.alias = stringArray;
    }

    public abstract void run(String[] var1, TextChannel var2);

    public String[] getAlias() {
        return this.alias;
    }
}
