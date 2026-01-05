package ru.sawaplago.chunkVisualizer.commands;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface SubCommand {
    void execute(CommandSender sender, String[] args);
    List<String> suggest(CommandSender sender, String[] args);
}