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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anime.aseller.Menu.Menu;
import ru.anime.aseller.MenuListener.MenuListener;
import ru.anime.aseller.MenuManager.MenuManager;
import ru.anime.aseller.commands.Seller;
import ru.anime.aseller.createDefaultYml.ConfigManager;
import ru.anime.aseller.createDefaultYml.PriseItemFileCreator;
import ru.anime.aseller.loader.MenuLoader;
import ru.anime.aseller.loader.PriseItemLoader;
import ru.anime.aseller.utils.ASellerPlaceholder;
import ru.anime.aseller.utils.Metrics;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public final class ASeller extends JavaPlugin {
    private static ASeller instance;
    public Economy economy;
    private static FileConfiguration cfg;
    private ASellerPlaceholder placeholderExpansion;
    private MenuManager menuManager;
    private  Map<String, Double> itemPrise;


    public static ASeller getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        vaultCheck(); // проверка и подключение Vault

        saveDefaultConfig();
        cfg = getConfig();
        new ConfigManager(this);
        new PriseItemFileCreator(this);

        MenuLoader.loadMenus(cfg, getDataFolder()); // загрузчик
        menuCheck(); // вывод загруженных меню

        menuManager = new MenuManager(MenuLoader.getListMenu());

        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        createCommand(); // создаём все команды

        String path = cfg.getString("priseItem.path");
        File itemFile = new File(getDataFolder(), path);
        itemPrise = PriseItemLoader.loadItemValuesFromFile(itemFile);



        new Metrics(this, 23115);
    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
    }

    private void vaultCheck(){
        if (!setupEconomy()) {
            getLogger().info("Vault не найден!");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new ASellerPlaceholder(this);
            placeholderExpansion.register();
        } else {
            getLogger().warning("PlaceholderAPI не обнаружен! Некоторые функции могут быть недоступны.");
        }
    }
    public void menuCheck(){
        MenuLoader.getListMenu().forEach((key, vault) -> {
            getLogger().info("Меню: " + key + " загружен");
            getLogger().info("Имя: " + vault.getTitleMenu());
            vault.getButtons().forEach(edit -> {
                getLogger().info(edit.getTitleButton());
            });
        });
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> registeredServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredServiceProvider == null) {
            return false;
        }
        economy = registeredServiceProvider.getProvider();
        return true;
    }
    private void createCommand(){
        // ниже регистрация всех команд
        List<String> commandNames = new ArrayList<>();
        Map<String,Menu> listMenu = MenuLoader.getListMenu();
        listMenu.forEach((key, item) -> {
            commandNames.addAll(item.getCommandOpenMenu());
        });
        for (String commandName : commandNames) {
            registerCommand(commandName, new Seller());
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

    public static FileConfiguration getCfg() {
        return cfg;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public Map<String, Double> getItemPrise() {
        return itemPrise;
    }
    public Map<Material, Double> getMaterialPrise(Map<String, Double> itemPrise){
        Map<Material, Double> materialCoal = new HashMap<>();
        itemPrise.forEach((key, vault) -> {
            Material material = Material.valueOf(key.toUpperCase());
            materialCoal.put(material, vault);
        });
        return materialCoal;
    }
    public Double getPriseItem(String material){
        itemPrise.forEach((key, vault) -> {
            ASeller.getInstance().getLogger().info("Название предмета: " + key + "цена: " + vault);
        });
        return itemPrise.get(material);
    }
}
