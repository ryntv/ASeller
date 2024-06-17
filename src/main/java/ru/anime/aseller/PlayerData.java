package ru.anime.aseller;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    private Inventory sellerInventory;
    private double payCount;

    private PlayerData(Player player) {
        this.sellerInventory = Bukkit.createInventory(player, 54, "Скупщик");
        this.payCount = 0;
    }

    public static PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(player));
    }

    public Inventory getSellerInventory() {
        return sellerInventory;
    }

    public double getPayCount() {
        return payCount;
    }

    public void setPayCount(double payCount) {
        this.payCount = payCount;
    }
}
