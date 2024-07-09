package ru.anime.aseller.loader;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anime.aseller.menuSetting.ButtonMenu;
import ru.anime.aseller.menuSetting.Category;
import ru.anime.aseller.menuSetting.CategoryItem;
import ru.anime.aseller.menuSetting.Menu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class loadMenu {
    private final JavaPlugin plugin;
    private final Map<String, Menu> menus = new HashMap<>();

    public loadMenu(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMenus();
    }

    public Map<String, Menu> getMenus() {
        return menus;
    }

    private void loadMenus() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().warning("Config file not found: " + configFile.getPath());
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.contains("menu")) {
            plugin.getLogger().warning("No menus found in config.yml");
            return;
        }

        config.getConfigurationSection("menu").getKeys(false).forEach(menuId -> {
            String path = config.getString("menu." + menuId + ".path");
            if (path != null) {
                loadMenuFromFile(menuId, path);
            }
        });
    }

    private void loadMenuFromFile(String menuId, String path) {
        File menuFile = new File(plugin.getDataFolder(), path);
        if (!menuFile.exists()) {
            plugin.getLogger().warning("Menu file not found: " + menuFile.getPath());
            return;
        }

        FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(menuFile);

        String displayName = menuConfig.getString("displayName");
        String commandOpen = menuConfig.getString("commandOpen");
        String permissionMenu = menuConfig.getString("permissionMenu");
        int size = menuConfig.getInt("size");

        List<ButtonMenu> buttons = new ArrayList<>();
        if (menuConfig.contains("menuItems")) {
            menuConfig.getConfigurationSection("menuItems").getKeys(false).forEach(key -> {
                plugin.getLogger().info("Loading menu item: " + key);
                String materialStr = menuConfig.getString("menuItems." + key + ".material");
                if (materialStr == null) {
                    plugin.getLogger().warning("Material is null for menu item: " + key);
                    return;
                }
                plugin.getLogger().info("Material for " + key + ": " + materialStr);
                Material material = Material.valueOf(materialStr);
                String slotStr = menuConfig.getString("menuItems." + key + ".slot");
                String displayButton = menuConfig.getString("menuItems." + key + ".displayButton");
                List<String> loreButton = menuConfig.getStringList("menuItems." + key + ".loreButton");
                List<String> command = menuConfig.getStringList("menuItems." + key + ".command");

                if (slotStr.contains("-")) {
                    String[] slotRange = slotStr.split("-");
                    int start = Integer.parseInt(slotRange[0]);
                    int end = Integer.parseInt(slotRange[1]);
                    for (int i = start; i <= end; i++) {
                        buttons.add(new ButtonMenu(i, displayButton, loreButton, material, command));
                    }
                } else {
                    int slot = Integer.parseInt(slotStr);
                    buttons.add(new ButtonMenu(slot, displayButton, loreButton, material, command));
                }
            });
        } else {
            plugin.getLogger().warning("No menuItems found in " + menuFile.getPath());
        }

        List<Category> categories = new ArrayList<>();
        if (menuConfig.contains("category")) {
            String idCategory = menuConfig.getString("category.idCategory");
            List<Integer> slots = new ArrayList<>();
            String slotsRange = menuConfig.getString("category.slots");
            String[] ranges = slotsRange.split("-");
            int start = Integer.parseInt(ranges[0]);
            int end = Integer.parseInt(ranges[1]);
            for (int i = start; i <= end; i++) {
                slots.add(i);
            }
            categories.add(loadCategory(idCategory, slots));
        }

        Menu menu = new Menu(displayName, menuId, commandOpen, permissionMenu, size, buttons, categories);
        menus.put(menuId, menu);
    }

    private Category loadCategory(String idCategory, List<Integer> slots) {
        File categoryFile = new File(plugin.getDataFolder(), "priseItem.yml");
        if (!categoryFile.exists()) {
            plugin.getLogger().warning("Category file not found: " + categoryFile.getPath());
            return null;
        }

        FileConfiguration categoryConfig = YamlConfiguration.loadConfiguration(categoryFile);
        List<CategoryItem> items = new ArrayList<>();

        categoryConfig.getConfigurationSection("item").getKeys(false).forEach(key -> {
            String materialStr = categoryConfig.getString("item." + key + ".material");
            Material material = Material.valueOf(materialStr);
            String displayName = categoryConfig.getString("item." + key + ".displayName");
            int price = categoryConfig.getInt("item." + key + ".prise");
            String idCategoryItem = categoryConfig.getString("item." + key + ".idCategory");
            List<String> lore = categoryConfig.getStringList("item." + key + ".lore"); // Вы можете добавить логику для лора

            // Добавляем проверку на соответствие idCategory
            if (idCategory.equals(idCategoryItem)) {
                items.add(new CategoryItem(key, displayName, lore, price, material, idCategoryItem));
            }
        });

        return new Category(idCategory, items, slots);
    }
}
