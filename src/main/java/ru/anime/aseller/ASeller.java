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
import java.lang.reflect.Field;
import java.util.*;

public final class ASeller extends JavaPlugin {
    private static ASeller instance;
    public Economy economy;
    private static FileConfiguration cfg;
    private ASellerPlaceholder placeholderExpansion;
    public static Map<String, Menu> menus = new HashMap<>();
    private Map<Material, Double> countMaterial = new HashMap<>();
    private boolean commandMenuListenerRegistered = false;

    loadMenu menuLoader = new loadMenu(this);
    private CommandMenu commandMenuListener;

    public static ASeller getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

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
    public void registerCommandMenuListener(CommandMenu commandMenu) {
        if (!commandMenuListenerRegistered) {
            this.commandMenuListener = commandMenu;
            getServer().getPluginManager().registerEvents(commandMenuListener, this);
            commandMenuListenerRegistered = true;
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
