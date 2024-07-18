package ru.anime.aseller.buttonCommand;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.menuSetting.ButtonMenu;
import ru.anime.aseller.utils.Hex;

import java.util.*;

import static ru.anime.aseller.utils.Hex.setPlaceholders;

public class SellZone {

    public static void sellItem(Player player, List<Integer> activeListSlots, Inventory inventory, Map<Material, Double> countMaterial, Map<UUID, Double> pay_count) {
        UUID playerUUID = player.getUniqueId();

        // Устанавливаем начальное значение для игрока
        pay_count.put(playerUUID, 0d);

        for (Integer slot : activeListSlots) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && item.getType() != Material.AIR) {
                if (countMaterial.containsKey(item.getType())) {
                    Double count = pay_count.get(playerUUID);
                    count += countMaterial.get(item.getType()) * item.getAmount();
                    pay_count.put(playerUUID, count);

                    // Удаление предметов из инвентаря
                    inventory.setItem(slot, null);
                }
            }
        }

        // Логирование суммы
     //   System.out.println("Сумма: " + pay_count.get(playerUUID));
        if (pay_count.get(playerUUID) > 0){
            player.sendMessage(("Вы успешно продали предметов на: "+ pay_count.get(playerUUID) + "$").replaceAll("\\.0$", ""));
        }

        // Депозит игрока
        ASeller.getInstance().economy.depositPlayer(player, pay_count.get(playerUUID));

        // Сброс значения
        pay_count.put(playerUUID, 0d);
    }

    public static void checkInventory(Player player, Inventory inventory, Map<Material, Double> countMaterial, ButtonMenu button, List<Integer> activeListSlots, Map<UUID, Double> pay_count) {
        //   System.out.println("Метод checkInventory вызван");
        UUID playerUUID = player.getUniqueId();

        // Устанавливаем начальное значение для игрока
        pay_count.put(playerUUID, 0d);

        for (Integer slot : activeListSlots) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && item.getType() != Material.AIR) {
                if (countMaterial.containsKey(item.getType())) {
                    Double count = pay_count.get(playerUUID);
                    count += countMaterial.get(item.getType()) * item.getAmount();
                    pay_count.put(playerUUID, count);
                }
            }
        }

        ItemStack item2 = new ItemStack(button.getMaterialButton());
        ItemMeta itemMeta = item2.getItemMeta();
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(ASeller.getInstance(), "num"), PersistentDataType.INTEGER, 5);
        List<String> lore = button.getLoreButton().stream()
                .map(s -> Hex.hex(setPlaceholders(player, s)))
                .map(s -> s.replace("%seller_pay%", String.valueOf(pay_count.get(playerUUID))))
                .map(s -> s.replaceAll("\\.0$", ""))
                .toList();

        itemMeta.setLore(lore);
        itemMeta.setDisplayName(button.getDisplayNameButton());

        item2.setItemMeta(itemMeta);
        inventory.setItem(button.getSlotButton(), item2);

    }


}
