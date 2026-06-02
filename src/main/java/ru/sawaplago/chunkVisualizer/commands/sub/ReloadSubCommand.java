package ru.sawaplago.chunkVisualizer.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.commands.SubCommand;
import ru.sawaplago.chunkVisualizer.managers.ConfigManager;
import ru.sawaplago.chunkVisualizer.managers.MessageManager;

public class ReloadSubCommand implements SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessageManager mm = ChunkVisualizer.getInstance().getMessageManager();
        ConfigManager cm = ChunkVisualizer.getInstance().getConfigManager();

        if (!sender.hasPermission("chunkvisualizer.admin")) {
            sender.sendMessage(mm.getMessage("no-permission"));
            return;
        }

        mm.reload();
        cm.reload();

        sender.sendMessage(mm.getMessage("commands.reload-success"));
    }

    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        return List.of();
    }
}
