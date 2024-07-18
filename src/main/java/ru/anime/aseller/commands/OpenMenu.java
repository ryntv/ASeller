package ru.anime.aseller.commands;

import org.bukkit.entity.Player;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.menuSetting.Menu;

import static ru.anime.aseller.commands.Seller.openMenus;

public class OpenMenu {
    public static void openMenu(String menuName, Player player){
        CommandMenu commandMenu = openMenus.get(player);
        if (commandMenu == null) {
            if (menuName == null) {
                ASeller.getInstance().getLogger().warning("Configuration value  is missing or null.");
            }

            Menu menu = ASeller.menus.get(menuName);

            if (menu == null) {
                ASeller.getInstance().getLogger().warning("Menu not found: " + menuName);
            }
            System.out.println(menu.getDisplayNameMenu());
            // Создаём новый экземпляр CommandMenu для игрока
            commandMenu = new CommandMenu(menu, player, menu.getDisplayNameMenu());
            openMenus.put(player, commandMenu);
        }

        // Открываем или обновляем меню
        commandMenu.createOrUpdateMenu();

    }
}
