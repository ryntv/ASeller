package ru.anime.aseller;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anime.aseller.GUIMenu.CategoryMenu;
import ru.anime.aseller.GUIMenu.MainMenu;
import ru.anime.aseller.commands.CommandMenu;
import ru.anime.aseller.commands.Seller;
import ru.anime.aseller.loader.loadMenu;
import ru.anime.aseller.menuSetting.Menu;
import ru.anime.aseller.utils.ASellerPlaceholder;
import ru.anime.aseller.utils.UtilSlots;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class ASeller extends JavaPlugin {
    private static ASeller instance;
    public Economy economy;
    private static FileConfiguration cfg;
    private ASellerPlaceholder placeholderExpansion;
    public static Map<String, Menu> menus = new HashMap<>();
    private final Map<Material, Double> countMaterial = new HashMap<>();



    public static ASeller getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        File menuFolder = new File(getDataFolder(), "Menu");
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
        }

        // Создаем файлы category.yml и seller.yml в папке Menu
        createYamlFile(new File(menuFolder, "category.yml"));
        createYamlFile(new File(menuFolder, "seller.yml"));
        createYamlFile(new File(getDataFolder(), "priseItem.yml"));
        loadMenu menuLoader = new loadMenu(this);

        if (!setupEconomy()) {
            getLogger().info("Vault не найден!");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new ASellerPlaceholder(this);
            placeholderExpansion.register();
        } else {
            getLogger().warning("PlaceholderAPI не обнаружен! Некоторые функции могут быть недоступны.");
        }

        saveDefaultConfig();
        cfg = getConfig();
        List<String> commandNames = getConfig().getStringList("commandOpenSeller");
        for (String commandName : commandNames) {
            registerCommand(commandName, new Seller());
        }
      //  Bukkit.getPluginManager().registerEvents(new MainMenu(), this);
        //   Bukkit.getPluginManager().registerEvents(new CategoryMenu(), this);

        menus = menuLoader.getMenus();
        menus.forEach((id, menu) -> {
            System.out.println(menu.getId());
            System.out.println(menu.getSize());
        });
        loadPriseItems();


    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
    }
    private void registerCommand(String commandName, CommandExecutor executor) {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(getServer());

            Command command = new BukkitCommand(commandName) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return executor.onCommand(sender, this, label, args);
                }
            };

            command.setAliases(Collections.emptyList());

            commandMap.register(getDescription().getName(), command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private void loadPriseItems() {
        File file = new File(getDataFolder(), "priseItem.yml");
        if (!file.exists()) {
            saveResource("priseItem.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = config.getConfigurationSection("item").getKeys(false);
        for (String key : keys) {
            String materialStr = config.getString("item." + key + ".material");
            double prise = config.getDouble("item." + key + ".prise");

            Material material = Material.getMaterial(materialStr);
            if (material != null) {
                countMaterial.put(material, prise);
            } else {
                getLogger().warning("Invalid material: " + materialStr);
            }
        }
    }


    private void createYamlFile(File file) {
        // Если файл уже существует, прерываем выполнение
        if (file.exists()) {
            getLogger().warning("Файл " + file.getName() + " уже существует!");
            return;
        }

        // Создаем новый файл
        try {
            file.createNewFile();
            getLogger().info("Создан файл: " + file.getName());

            // Получаем конфигурацию YAML из файла
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Заполняем файл данными в зависимости от его имени
            if (file.getName().equalsIgnoreCase("category.yml")) {
                config.set("idMenu", "category");
                config.set("displayName", "Категории");
                config.set("commandOpen", "cat");
                config.set("permissionMenu", "none");
                config.set("size", 54);

                // Заполняем меню mobs
                config.createSection("menuMobs");
                config.set("menuMobs.material", "ROTTEN_FLESH");
                config.set("menuMobs.slot", 15);
                config.set("menuMobs.displayButton", "Ресурсы с мобов");
                config.set("menuMobs.loreButton", new String[]{" ", "Нажмите чтобы настроить", " "});
                config.set("menuMobs.command", "[open_menu] mobs");

                // Заполняем меню mine
                config.createSection("menuMine");
                config.set("menuMine.material", "ROTTEN_FLESH");
                config.set("menuMine.slot", 16);
                config.set("menuMine.displayButton", "Ресурсы с шахты");
                config.set("menuMine.loreButton", new String[]{" ", "Нажмите чтобы настроить", " "});
                config.set("menuMine.command", "[open_menu] mine");
            } else if (file.getName().equalsIgnoreCase("seller.yml")) {
                config.set("displayName", "Скупщик");
                config.set("commandOpen", "open_seller");
                config.set("permissionMenu", "none");
                config.set("size", 54);

                // Заполняем меню sellerItems
                config.createSection("menuItems.buttonSell");
                config.set("menuItems.buttonSell.material", "AIR");
                config.set("menuItems.buttonSell.slot", "0-40");
                config.set("menuItems.buttonSell.displayButton", " ");
                config.set("menuItems.buttonSell.loreButton", new String[]{});
                config.set("menuItems.buttonSell.command", new String[]{"[sell_zone]"});

                config.createSection("menuItems.buttonSell2");
                config.set("menuItems.buttonSell2.material", "DIRT");
                config.set("menuItems.buttonSell2.slot", 49);
                config.set("menuItems.buttonSell2.displayButton", " ");
                config.set("menuItems.buttonSell2.loreButton", new String[]{"&fНажмите чтобы сдать все предметы", "&fПредметов на сумму: &e%seller_pay%"});
                config.set("menuItems.buttonSell2.command", new String[]{"[sell]"});
            } else if (file.getName().equalsIgnoreCase("priseItem.yml")) {
                // Заполняем файл priseItem.yml
                config.createSection("item.rotten_flesh");
                config.set("item.rotten_flesh.material", "ROTTEN_FLESH");
                config.set("item.rotten_flesh.displayName", "Гнилая плоть");
                config.set("item.rotten_flesh.prise", 100);
                config.set("item.rotten_flesh.idCategory", "mobs");

                config.createSection("item.bone");
                config.set("item.bone.material", "BONE");
                config.set("item.bone.displayName", "Кость");
                config.set("item.bone.prise", 150);
                config.set("item.bone.idCategory", "mobs");

                config.createSection("item.iron_ingot");
                config.set("item.iron_ingot.material", "IRON_INGOT");
                config.set("item.iron_ingot.displayName", "Железный слиток");
                config.set("item.iron_ingot.prise", 200);
                config.set("item.iron_ingot.idCategory", "mine");

                config.createSection("item.gold_ingot");
                config.set("item.gold_ingot.material", "GOLD_INGOT");
                config.set("item.gold_ingot.displayName", "Золотой слиток");
                config.set("item.gold_ingot.prise", 300);
                config.set("item.gold_ingot.idCategory", "mine");
            }

            // Сохраняем конфигурацию обратно в файл
            config.save(file);
        } catch (IOException e) {
            getLogger().warning("Не удалось создать файл: " + file.getName());
            e.printStackTrace();
        }
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> registeredServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredServiceProvider == null) {
            return false;
        }
        economy = registeredServiceProvider.getProvider();
        return true;
    }
    public static FileConfiguration getCfg() {
        return cfg;
    }
    public Map<Material, Double> getCountMaterial() {
        return countMaterial;
    }
}
