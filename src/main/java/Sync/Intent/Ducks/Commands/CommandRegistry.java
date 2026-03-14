package Sync.Intent.Ducks.Commands;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandRegistry {
    private static final Map<String, Command> COMMANDS = new ConcurrentHashMap<>();

    public static void register(Command command) {
        String[] aliases = command.getAlias();
        if (aliases == null || aliases.length == 0) {
            throw new IllegalArgumentException("Command must have at least one alias.");
        }

        for (String alias : aliases) {
            if (COMMANDS.containsKey(alias)) {
                throw new IllegalStateException("Alias '" + alias + "' is already registered.");
            }
            COMMANDS.put(alias, command);
        }
    }

    public static Command get(String name) {
        return COMMANDS.get(name);
    }

    public static Map<String, Command> getAll() {
        return Collections.unmodifiableMap(COMMANDS);
    }

}
