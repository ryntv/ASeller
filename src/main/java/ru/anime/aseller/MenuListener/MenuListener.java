package ru.anime.aseller.MenuListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.Menu.Menu;
import ru.anime.aseller.Menu.MenuButton;
import ru.anime.aseller.MenuManager.MenuManager;
import ru.anime.aseller.buttonCommand.SellAll;
import ru.anime.aseller.buttonCommand.SellZone;
import ru.anime.aseller.utils.Hex;

import java.util.*;

import static ru.anime.aseller.utils.Hex.hex;
import static ru.anime.aseller.utils.Hex.setPlaceholders;

public class MenuListener implements Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger("ASeller");

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Проверяем, что верхний инвентарь принадлежит игроку
        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }

        // Проверяем, что событие связано с быстрым перемещением (Shift + ЛКМ)
        if (event.isShiftClick() && (!clickedInventory.equals(topInventory))) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                return;
            }

            // Проверяем, есть ли свободные слоты в зоне продажи [sell_zone]
            boolean hasFreeSlot = false;
            for (MenuButton button : menu.getButtons()) {
                if (button.getCommand().contains("[sell_zone]")) {
                    int slot = button.getSlotButton();
                    ItemStack itemInSlot = topInventory.getItem(slot);
                    if (itemInSlot == null) {
                        hasFreeSlot = true;
                        break;
                    }
                }
            }

            // Если нет свободных слотов, отменяем перемещение предмета
            if (!hasFreeSlot) {
                event.setCancelled(true);
            } else {
                // Если есть свободные слоты, перенаправляем предмет в первый свободный слот зоны продаж
                for (MenuButton button : menu.getButtons()) {
                    if (button.getCommand().contains("[sell_zone]")) {
                        int slot = button.getSlotButton();
                        ItemStack itemInSlot = topInventory.getItem(slot);
                        if (itemInSlot == null) {
                            topInventory.setItem(slot, clickedItem);
                            event.setCurrentItem(null); // Убираем предмет из текущего слота
                            break;
                        }
                    }
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < topInventory.getSize(); i++) {
                ItemStack itemStack = topInventory.getItem(i);
                if (itemStack != null) {
                    // Проверяем, относится ли слот к зоне продаж (sell_zone)
                    for (MenuButton btn : menu.getButtons()) {
                        if (btn.getSlotButton() == i && btn.getCommand().contains("[sell_zone]")) {
                            itemStacks.add(itemStack);
                            break; // Прекращаем проверку других кнопок
                        }
                    }
                }
            }
            SellZone.checkItem(itemStacks, ASeller.getInstance().getItemPrise(), player);
            // Обновляем лор кнопок
            for (MenuButton btn : menu.getButtons()) {
                // Обновляем лор кнопки

                // Обновляем предмет в инвентаре
                updateLoreButton(btn, topInventory, SellZone.getCountPlayerString(player.getUniqueId(), 0), player);
            }
        }, 1L);

        // Проверяем, что клик был в верхнем инвентаре
        if (!clickedInventory.equals(topInventory)) {
            return;
        }


        ASeller.getInstance().getLogger().info("Игрок вызван клик! " + player.getName() + "в меню " + menu.getTitleMenu());



        for (MenuButton button : menu.getButtons()) {
            if (event.getSlot() == button.getSlotButton()) {
                if (button.getCommand().contains("[sell_zone]")) {


                    return;
                } else if (button.getCommand().contains("[sell_all]")){
                    event.setCancelled(true); // Отменяем стандартное поведение для этого слота

                    // Собираем предметы из зоны продажи и подсчитываем сумму
                    List<ItemStack> itemStacks = new ArrayList<>();
                    for (MenuButton btn : menu.getButtons()) {
                        if (btn.getCommand().contains("[sell_zone]")) {
                            ItemStack item = topInventory.getItem(btn.getSlotButton());
                            if (item != null) {
                                itemStacks.add(item);
                                LOGGER.info("{}",item);
                            }
                        }
                    }

                    // Начисляем сумму и очищаем слоты
                    Double count = SellZone.getCountPlayer(player.getUniqueId());
                    if (count == null){
                        count = 0d;
                    }
                    if (count > 0) {
                        ASeller.getInstance().economy.depositPlayer(player, count);
                        for (int i = 0; i < topInventory.getSize(); i++){
                            ItemStack itemStack = topInventory.getItem(i);
                            if (itemStack != null) {
                                Map<Material, Double> materialDoubleMap = ASeller.getInstance().getMaterialPrise(ASeller.getInstance().getItemPrise());
                                int finalI = i;
                                menu.getButtons().forEach(button2 -> {
                                    if(button2.getSlotButton() == finalI && button2.getCommand().contains("[sell_zone]")){
                                        if (materialDoubleMap.containsKey(itemStack.getType())){       // тут происходит тотальный пиздец, если что это запись для меня или для ребят кто захотел что-то поделать в моём коде
                                            topInventory.setItem(finalI, null);              // тут ты под грибами проверяешь кнопку перед удалением на то что является она sell_zone или нет
                                        }
                                    }
                                });

                            }
                        }
                        player.sendMessage(Hex.hex(ASeller.getCfg().getString("completeSaleMessage") + count));
                    } else {
                        player.sendMessage(Hex.hex(ASeller.getCfg().getString("noItemsToSellMessage", "У вас нет предметов для продажи")));
                    }
                    return;

                } else if (button.getCommand().stream().anyMatch(command -> command.startsWith("[open_menu]"))) {
                    event.setCancelled(true);

                    button.getCommand().stream()
                            .filter(command -> command.startsWith("[open_menu]")) // Ищем команды, которые начинаются с [open_menu]
                            .findFirst() // Берём первую подходящую команду
                            .ifPresent(command -> {
                                // Извлекаем ключ после [open_menu]
                                String key = command.substring("[open_menu]".length()).trim();
                                ASeller.getInstance().getMenuManager().openMenu(player, key);
                            });
                } else if (button.getCommand().contains("[sell_item]")) {
                    Material materialSell = button.getMaterialButton();
                    Double count = ASeller.getInstance().getPriseItem(materialSell.name().toLowerCase());
                    LOGGER.info("{} {}", count, materialSell);
                    if(count != null){
                        PlayerInventory inventory = player.getInventory(); // Получаем инвентарь игрока
                        double sumCount = 0d;
                        // Проходим по всем слотам нижнего инвентаря (0-35)
                        for (int slot = 0; slot < 36; slot++) {
                            ItemStack item = inventory.getItem(slot);
                            // Получаем предмет из слота
                            LOGGER.info("{}",item);
                            if (item != null && item.getType() == materialSell) {
                                double priseItem = count * item.getAmount();
                                ASeller.getInstance().economy.depositPlayer(player, priseItem);
                                sumCount += priseItem;
                                player.getInventory().setItem(slot, null);
                            }
                        }
                        player.sendMessage(Hex.hex("&eВы продали на предмета на сумму: &a" + sumCount));
                    } else {
                        LOGGER.info("Данного материала нету в списке продаваемых предметов");
                    }

                } else {
                    event.setCancelled(true);
                }
                break;
            }
        }



        // Если слот является слотом [sell_zone], разрешаем размещение предмета
        event.setCancelled(true); // Отменить стандартное поведение для не [sell_zone] слотов
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getInventory();

        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Получаем слоты, в которые игрок пытается переместить предметы
        for (Integer slot : event.getRawSlots()) {
            // Проверяем, относится ли слот к верхнему инвентарю
            if (slot >= topInventory.getSize()) {
                continue; // Игнорируем слоты нижнего инвентаря
            }

            boolean isAllowedSlot = false;

            // Проверяем, относится ли слот к разрешенным слотам [sell_zone]
            for (MenuButton button : menu.getButtons()) {
                if (slot == button.getSlotButton() && button.getCommand().contains("[sell_zone]")) {
                    isAllowedSlot = true;
                    break; // Выходим из цикла, так как слот является допустимым
                }
            }

            // Если хотя бы один слот не является разрешенным, отменяем действие
            if (!isAllowedSlot) {
                event.setCancelled(true);
                return;
            }
        }

        // Если все слоты разрешены, запускаем проверку предметов
        Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < topInventory.getSize(); i++) {
                ItemStack itemStack = topInventory.getItem(i);
                if (itemStack != null) {
                    // Проверяем, относится ли слот к зоне продаж (sell_zone)
                    for (MenuButton btn : menu.getButtons()) {
                        if (btn.getSlotButton() == i && btn.getCommand().contains("[sell_zone]")) {
                            itemStacks.add(itemStack);
                            break; // Прекращаем проверку других кнопок
                        }
                    }
                }
            }
            SellZone.checkItem(itemStacks, ASeller.getInstance().getItemPrise(), player);
            for (MenuButton btn : menu.getButtons()) {
                // Обновляем лор кнопки

                // Обновляем предмет в инвентаре
                updateLoreButton(btn, topInventory, SellZone.getCountPlayerString(player.getUniqueId(), 0), player);
            }
        }, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof Menu menu)) {
            return;
        }

        // Логирование закрытия меню
        ASeller.getInstance().getLogger().info("Игрок закрыл меню: " + menu.getTitleMenu());

        // Обработка предметов из sell_zone
        menu.getButtons().stream()
                .filter(button -> button.getCommand().contains("[sell_zone]"))
                .forEach(button -> {
                    ItemStack item = topInventory.getItem(button.getSlotButton());
                    if (item != null) {
                        // Возвращаем предмет игроку или выбрасываем на землю
                        if (player.getInventory().firstEmpty() == -1) {
                            player.getWorld().dropItem(player.getLocation(), item);
                        } else {
                            player.getInventory().addItem(item);
                        }
                        // Очищаем слот после возврата предмета
                        topInventory.setItem(button.getSlotButton(), null);
                    }
                });

        // Очищаем данные sell_zone для игрока
        SellZone.clearPlayer(playerUUID);
    }

    // Метод для обновления предметов в инвентаре
    public void updateLoreButton(MenuButton button, Inventory topInventory, String count, Player player){
        // Проверяем команду и обновляем лор только для тех, у которых нет [sell_zone]
        if (!button.getCommand().contains("[sell_zone]")) {
            ItemStack itemStack = new ItemStack(button.getMaterialButton());
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(hex(button.getTitleButton()));
                meta.setLore(button.getLoreButton().stream()
                        .map(s -> Hex.hex(setPlaceholders(player, s)))
                        .map(s -> s.replace("%seller_pay%", count)).toList());
                itemStack.setItemMeta(meta);
            }
            topInventory.setItem(button.getSlotButton(), itemStack);
        }
    }

}

