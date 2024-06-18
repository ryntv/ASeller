package ru.anime.aseller.GUIMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.utils.Hex;
import ru.anime.aseller.utils.UtilSlots;

import java.util.*;
import static ru.anime.aseller.utils.Hex.setPlaceholders;

public class MainMenu implements Listener {
    static Map<UUID, Double> pay_count = new HashMap<>();
    Map<Material, Double> count_material = new HashMap<>();

    public static void openSellerMenu(Player player) {
        // Создание уникального инвентаря для каждого игрока
        Inventory sellerMenu = Bukkit.createInventory(player, 54, "Скупщик");

        // Пример добавления предметов в инвентарь
        ItemStack item2 = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta itemMeta = item2.getItemMeta();
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(ASeller.getInstance(), "num"), PersistentDataType.INTEGER, 5);
        pay_count.put(player.getUniqueId(), 0d);
        itemMeta.setLore(ASeller.getCfg().getStringList("lore_button_sell").stream().map(s -> Hex.hex(setPlaceholders(player, s))).toList());
        itemMeta.setDisplayName(ASeller.getCfg().getString(Hex.hex("display_button_sell")));

        item2.setItemMeta(itemMeta);
        sellerMenu.setItem(ASeller.getCfg().getInt("position_button_sell"), item2);

        // Открытие инвентаря для игрока

        player.openInventory(sellerMenu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Скупщик")) { // Проверяем по названию вашего инвентаря
            Inventory clickedInventory = event.getClickedInventory();
            Player player = (Player) event.getWhoClicked();

            if (clickedInventory != null && event.getSlotType() != InventoryType.SlotType.OUTSIDE) {
                int slot = event.getRawSlot();

                if (event.isShiftClick()) {
                    // Проверяем, что клик был сделан в инвентаре игрока
                    if (clickedInventory == player.getInventory()) {
                        Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
                            checkInventory(player, event.getView().getTopInventory());
                        }, 1L); // Задержка в 1 тик
                        return; // Выход из метода, так как обработка завершена
                    }
                }

                // Проверяем, что клик был сделан в инвентаре "Скупщик"
                if (clickedInventory == event.getView().getTopInventory()) {
                    Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
                        checkInventory(player, clickedInventory);
                    }, 1L); // Задержка в 1 тик
                }

                for (Integer item : UtilSlots.noActiveListSlots) {
                    if (Objects.equals(slot, item)){
                        event.setCancelled(true); // Отменяем событие, если в эти слоты пытаются перетащить предметы
                        player.updateInventory(); // Обновляем инвентарь игрока, чтобы предметы не отображались неправильно
                    }
                }
                if (slot == ASeller.getCfg().getInt("position_button_sell")) {
                    if (pay_count.get(player.getUniqueId()) > 0){
                        sellItem(player, clickedInventory);
                    } else {
                        player.sendMessage(Hex.hex(Objects.requireNonNull(ASeller.getCfg().getString("messageItemsDeficit"))));
                    }

                }
            }
        }
    }
    public void sellItem(Player player, Inventory inventory){
        for (Integer slot : UtilSlots.activeListSlots) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && item.getType() != Material.AIR) {
                if (count_material.containsKey(item.getType())) {
                    inventory.setItem(slot, null);
                }
            }
        }
        ASeller.getInstance().economy.depositPlayer(player, pay_count.get(player.getUniqueId()));
        player.sendMessage(Hex.hex(setPlaceholders(player, Objects.requireNonNull(ASeller.getCfg().getString("messageSale")))));
        pay_count.put(player.getUniqueId(), 0d);

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals("Скупщик")) { // Проверяем по названию вашего инвентаря
            Inventory topInventory = event.getView().getTopInventory();
            if (event.getInventory().equals(topInventory)) {
                Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
                    checkInventory((Player) event.getWhoClicked(), topInventory);
                }, 1L); // Задержка в 1 тик
            }
            for (Integer slot : event.getRawSlots()) {
                if (UtilSlots.noActiveListSlots.contains(slot)) {
                    event.setCancelled(true); // Отменяем событие, если в эти слоты пытаются перетащить предметы
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Скупщик")) { // Проверяем по названию инвентаря
            Inventory inventory = event.getInventory();
            Player player = (Player) event.getPlayer();

            // Возвращаем оставшиеся предметы игроку или выбрасываем под ноги
            returnItemsToPlayerOrDrop(player, inventory);

            // Обновляем инвентарь игрока
            Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), player::updateInventory, 1L); // Задержка в тиках
            pay_count.put(player.getUniqueId(), 0d);
        }
    }

    private void returnItemsToPlayerOrDrop(Player player, Inventory inventory) {
        // Проходимся по слотам с 0 по 44
        for (Integer slot : UtilSlots.activeListSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                // Проверяем, есть ли место в инвентаре игрока
                if (player.getInventory().firstEmpty() == -1) {
                    // Если места нет, выбрасываем предмет под ноги игроку
                    player.getWorld().dropItem(player.getLocation(), item);
                } else {
                    // Если место есть, добавляем предмет в инвентарь игрока
                    player.getInventory().addItem(item);
                }
                // Очищаем слот
                inventory.setItem(slot, null);
            }
        }
    }

    private void checkInventory(Player player, Inventory inventory) {
        pay_count.put(player.getUniqueId(), 0d);

         Map<String, Object> test = ASeller.getCfg().getConfigurationSection("priseItem").getValues(false);
        test.forEach((key, value) -> count_material.put(Material.getMaterial(key), (Double) value));

        for (Integer slot : UtilSlots.activeListSlots) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && item.getType() != Material.AIR) {
                if (count_material.containsKey(item.getType())) {
                    Double count = pay_count.get(player.getUniqueId());
                    count += count_material.get(item.getType()) * item.getAmount();
                    pay_count.put(player.getUniqueId(), count);
                }
            }
        }
        ItemStack item2 = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta itemMeta = item2.getItemMeta();
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(ASeller.getInstance(), "num"), PersistentDataType.INTEGER, 5);
        itemMeta.setLore(ASeller.getCfg().getStringList("lore_button_sell").stream().map(s -> Hex.hex(setPlaceholders(player, s))).toList());
        itemMeta.setDisplayName(ASeller.getCfg().getString(Hex.hex("display_button_sell")));

        item2.setItemMeta(itemMeta);
        inventory.setItem(ASeller.getCfg().getInt("position_button_sell"), item2);
    }
    public static double getPay_count(Player player) {
        return pay_count.get(player.getUniqueId());
    }
}
