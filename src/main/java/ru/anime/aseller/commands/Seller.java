package ru.anime.aseller.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.anime.aseller.GUIMenu.MainMenu;

public class Seller implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            MainMenu.openSellerMenu(player);
            return true;
        } else {
            sender.sendMessage("Команда доступна только игрокам.");
            return false;
        }
    }
}
