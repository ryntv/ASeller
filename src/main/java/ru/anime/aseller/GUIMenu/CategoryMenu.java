package ru.anime.aseller.GUIMenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.utils.Hex;
import ru.anime.aseller.utils.UtilSlots;
import java.util.Objects;

public class CategoryMenu implements Listener {
    static String displayName = "Категория";

    public static void openSellerMenu(Player player) {
        // Создание уникального инвентаря для каждого игрока
        Inventory sellerMenu = Bukkit.createInventory(player, 54, "test");
        // Открытие инвентаря для игрока
        player.openInventory(sellerMenu);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(Hex.hex(displayName))) { // Проверяем по названию вашего инвентаря
            Inventory clickedInventory = event.getClickedInventory();
            Player player = (Player) event.getWhoClicked();
            if (clickedInventory != null && event.getSlotType() != InventoryType.SlotType.OUTSIDE) {
                int slot = event.getRawSlot();
                for (Integer item : UtilSlots.noActiveListSlots) {
                    if (Objects.equals(slot, item)) {
                        event.setCancelled(true); // Отменяем событие, чтобы предметы нельзя было взять из указанных слотов
                        player.updateInventory(); // Обновляем инвентарь игрока, чтобы предметы не отображались неправильно
                    }
                }

            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(Hex.hex(displayName))) { // Проверяем по названию вашего инвентаря
            for (Integer slot : event.getRawSlots()) {
                for (Integer item : UtilSlots.noActiveListSlots) {
                    if (Objects.equals(slot, item)) {
                        event.setCancelled(true); // Отменяем событие, если в эти слоты пытаются перетащить предметы
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(Hex.hex(displayName))) { // Проверяем по названию инвентаря
            Player player = (Player) event.getPlayer();


            // Обновляем инвентарь игрока
            Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), player::updateInventory, 1L); // Задержка в тиках
        }
    }
}
