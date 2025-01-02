package ru.anime.aseller.buttonCommand;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.anime.aseller.ASeller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellAll {
    public static Inventory sellAll(Double count, Player player, List<ItemStack> itemStacks, Map<String, Double> prise, Inventory topInventory) {
        if (count > 0) {
            ASeller.getInstance().economy.depositPlayer(player, count); // Начисление денег игроку

            // Перевод карты с ценами материалов в карту с Material
            Map<Material, Double> materialPrise = new HashMap<>();
            prise.forEach((key, vault) -> {
                Material material = Material.valueOf(key.toUpperCase());
                materialPrise.put(material, vault);
            });

            // Удаление предметов из инвентаря
            for (int i = 0; i < topInventory.getSize(); i++) {
                ItemStack currentItem = topInventory.getItem(i);
                if (currentItem != null && itemStacks.contains(currentItem)) {
                    topInventory.setItem(i, null); // Убираем предмет из слота
                    ASeller.getInstance().getLogger().info("Удалён предмет из слота: " + i);
                    itemStacks.remove(currentItem); // Удаляем предмет из списка для продажи
                }
            }
            return topInventory;
        }
        return topInventory;
    }
}
