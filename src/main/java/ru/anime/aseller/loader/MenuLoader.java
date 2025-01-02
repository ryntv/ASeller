package ru.anime.aseller.loader;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.anime.aseller.Menu.Menu;
import ru.anime.aseller.Menu.MenuButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuLoader {

    private static Map<String, Menu> listMenu = new HashMap<>();

    public static void loadMenus(FileConfiguration config, File dataFolder) {
        // Получаем пути к файлам меню
        for (String menuId : config.getConfigurationSection("menu").getKeys(false)) {
            String filePath = config.getString("menu." + menuId + ".path");
            File menuFile = new File(dataFolder, filePath);

            if (menuFile.exists()) {
                loadMenuFromFile(menuId, menuFile);
            } else {
                System.out.println("Menu file not found: " + menuFile.getPath());
            }
        }
    }

    private static void loadMenuFromFile(String menuId, File menuFile) {
        // Загружаем YAML файл
        FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(menuFile);

        // Получаем данные меню
        String titleMenu = menuConfig.getString("titleMenu");
        int size = menuConfig.getInt("size");
        List<String> commandOpenMenu = menuConfig.getStringList("commandOpenMenu");
        String permissionOpenMenu = menuConfig.getString("permissionOpenMenu");

        // Загружаем кнопки меню
        List<MenuButton> buttons = new ArrayList<>();
        if (menuConfig.contains("menuItems")) {
            for (String buttonKey : menuConfig.getConfigurationSection("menuItems").getKeys(false)) {
                String materialName = menuConfig.getString("menuItems." + buttonKey + ".material");
                Material material = Material.getMaterial(materialName);
                String slotString = menuConfig.getString("menuItems." + buttonKey + ".slot");
                List<Integer> slots = parseSlots(slotString); // Используем parseSlots для обработки слотов
                String titleButton = menuConfig.getString("menuItems." + buttonKey + ".displayButton");
                List<String> loreButton = menuConfig.getStringList("menuItems." + buttonKey + ".loreButton");
                List<String> commands = menuConfig.getStringList("menuItems." + buttonKey + ".command");

                // Создаем кнопки для каждого слота
                for (int slot : slots) {
                    MenuButton menuButton = new MenuButton(slot, titleButton, loreButton, material, commands);
                    buttons.add(menuButton);
                }

            }
        }

        // Создаем объект Menu
        Menu menu = new Menu(menuId, titleMenu, size, commandOpenMenu, permissionOpenMenu, buttons);

        // Добавляем меню в список
        listMenu.put(menuId, menu);
    }

    private static List<Integer> parseSlots(String slotString) {
        List<Integer> slots = new ArrayList<>();
        if (slotString.contains("-")) {
            String[] range = slotString.split("-");
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            for (int i = start; i <= end; i++) {
                slots.add(i);
            }
        } else {
            slots.add(Integer.parseInt(slotString));
        }
        return slots;
    }

    public static Map<String, Menu> getListMenu() {
        return listMenu;
    }
}
