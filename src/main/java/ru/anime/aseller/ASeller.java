package ru.anime.aseller;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anime.aseller.GUIMenu.MainMenu;
import ru.anime.aseller.commands.Seller;
import ru.anime.aseller.utils.ASellerPlaceholder;
import ru.anime.aseller.utils.UtilSlots;

public final class ASeller extends JavaPlugin {
    private static ASeller instance;
    public Economy economy;
    private static FileConfiguration cfg;
    private ASellerPlaceholder placeholderExpansion;

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

        getCommand("seller").setExecutor(new Seller());
        Bukkit.getPluginManager().registerEvents(new MainMenu(), this);
        UtilSlots.getNoActiveList();
        UtilSlots.getActiveListSlots();
    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
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
}
