package ru.sawaplago.chunkVisualizer.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.commands.sub.ReloadSubCommand;
import ru.sawaplago.chunkVisualizer.commands.sub.SettingsSubCommand;
import ru.sawaplago.chunkVisualizer.managers.MessageManager;

public class CVCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final MessageManager mm;

    public CVCommand() {
        this.mm = ChunkVisualizer.getInstance().getMessageManager();
        subCommands.put("settings", new SettingsSubCommand());
        subCommands.put("reload", new ReloadSubCommand());
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(mm.getMessage("commands.help-header"));
            sender.sendMessage(mm.getMessage("commands.help-settings"));
            sender.sendMessage(mm.getMessage("commands.help-reload"));
            return true;
        }

        SubCommand target = subCommands.get(args[0].toLowerCase());

        if (target != null) {
            target.execute(sender, args);
        } else {
            // Сообщение о неизвестной команде из конфига
            sender.sendMessage(mm.getMessage("commands.unknown"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .filter(
                            s -> {
                                if (s.equals("reload"))
                                    return sender.hasPermission("chunkvisualizer.admin");
                                if (s.equals("settings"))
                                    return sender.hasPermission("chunkvisualizer.settings");
                                return true;
                            })
                    .collect(Collectors.toList());
        }

        SubCommand target = subCommands.get(args[0].toLowerCase());
        if (target != null) {
            return target.suggest(sender, args);
        }

        return List.of();
    }
}
