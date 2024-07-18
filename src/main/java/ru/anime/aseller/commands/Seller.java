package ru.anime.aseller.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.buttonCommand.SellZone;
import ru.anime.aseller.menuSetting.Menu;

import java.util.*;

public class Seller implements CommandExecutor {
    public static Map<Player, CommandMenu> openMenus = new HashMap<>();
    public static List<String> nameMenu = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только игрокам.");
            return true;
        }

        // Проверяем, есть ли уже открытое меню для игрока
        CommandMenu commandMenu = openMenus.get(player);
        if (commandMenu == null) {
            String menuName = ASeller.getCfg().getString("sellerMenu");
            if (menuName == null) {
                ASeller.getInstance().getLogger().warning("Configuration value 'sellerMenu' is missing or null.");
                return true;
            }

            Menu menu = ASeller.menus.get(menuName);
            if (menu == null) {
                ASeller.getInstance().getLogger().warning("Menu not found: " + menuName);
                return true;
            }

            // Создаём новый экземпляр CommandMenu для игрока
            commandMenu = new CommandMenu(menu, player, menu.getDisplayNameMenu());
            openMenus.put(player, commandMenu);
        }

        // Открываем или обновляем меню
        commandMenu.createOrUpdateMenu();

        return true;
    }

    // Добавляем метод для закрытия меню и удаления из openMenus
    public void closeMenu(Player player) {
        CommandMenu commandMenu = openMenus.remove(player);
        if (commandMenu != null) {
            commandMenu.unregister(); // Дополнительно отключаем слушатели, если требуется
        }
    }
}