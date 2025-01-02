package ru.anime.aseller.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.anime.aseller.ASeller;

public class Seller implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только игрокам.");
            return true;
        }
        ASeller.getInstance().getMenuManager().getListMenu().forEach((key,vault) ->{
            vault.getCommandOpenMenu().forEach(item -> {
                if (item.equals(label)){
                    ASeller.getInstance().getMenuManager().openMenu(player, key);
                }
            });
        });

        return false;
    }
}
