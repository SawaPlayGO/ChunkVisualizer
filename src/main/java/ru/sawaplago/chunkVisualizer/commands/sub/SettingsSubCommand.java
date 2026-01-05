package ru.sawaplago.chunkVisualizer.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.commands.SubCommand;
import ru.sawaplago.chunkVisualizer.menus.SettingsMenu;

import java.util.List;

public class SettingsSubCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chunkvisualizer.settings")) {
            sender.sendMessage(ChunkVisualizer.getInstance().getMessageManager().getMessage("no-permission"));
            return;
        }

        if (sender instanceof Player player) {
            new SettingsMenu().open(player);
        } else {
            sender.sendMessage(ChunkVisualizer.getInstance().getMessageManager().getMessage("only-players"));
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        return List.of();
    }
}