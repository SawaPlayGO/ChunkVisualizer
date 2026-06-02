package ru.sawaplago.chunkVisualizer.commands;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    void execute(CommandSender sender, String[] args);

    List<String> suggest(CommandSender sender, String[] args);
}
