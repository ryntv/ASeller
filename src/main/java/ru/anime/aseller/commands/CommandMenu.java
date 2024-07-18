package ru.anime.aseller.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
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
import ru.anime.aseller.buttonCommand.Message;
import ru.anime.aseller.buttonCommand.SellZone;
import ru.anime.aseller.menuSetting.ButtonMenu;
import ru.anime.aseller.menuSetting.Menu;
import ru.anime.aseller.utils.Hex;

import java.util.*;
import java.util.stream.Collectors;

import static ru.anime.aseller.buttonCommand.SellZone.checkInventory;
import static ru.anime.aseller.commands.Seller.openMenus;
import static ru.anime.aseller.utils.Hex.setPlaceholders;

public class CommandMenu implements Listener {
    private final Menu mainMenu;
    private final Player player;
    private Inventory sellerMenu;

    private static final List<Integer> activeListSlots = new ArrayList<>();
    private static final List<Integer> noActiveListSlots = new ArrayList<>();
    private final Map<Integer, String> buttonSlots = new HashMap<>();
    private static final Map<UUID, Double> pay_count = new HashMap<>();

    public CommandMenu(Menu mainMenu, Player player, String nameMenu) {
        this.mainMenu = mainMenu;
        this.player = player;
        if (!Seller.nameMenu.contains(nameMenu)){
            Bukkit.getPluginManager().registerEvents(this, ASeller.getInstance());
            Seller.nameMenu.add(nameMenu);
        }
        if (mainMenu == null) {
            ASeller.getInstance().getLogger().warning("MainMenu is null");
        } else {
            ASeller.getInstance().getLogger().info("MainMenu successfully set: " + mainMenu.getDisplayNameMenu());
        }
    }

    public void createOrUpdateMenu() {
        if (sellerMenu == null) {
            sellerMenu = Bukkit.createInventory(player, mainMenu.getSize(), Hex.hex(mainMenu.getDisplayNameMenu()));
            createMenuContents();

            ASeller.getInstance().getLogger().info("Listener registered for: " + player.getName());
        } else {
            updateMenuContents();
        }

        player.openInventory(sellerMenu);
        ASeller.getInstance().getLogger().info("Inventory opened successfully for player.");
    }

    private void createMenuContents() {
        activeListSlots.clear();
        noActiveListSlots.clear();
        buttonSlots.clear();

        List<ButtonMenu> buttonMenus = mainMenu.getButton();

        if (buttonMenus == null || buttonMenus.isEmpty()) {
            ASeller.getInstance().getLogger().warning("Button menu is empty or null in createMenuContents");
            return;
        }

        buttonMenus.forEach(button -> {
            Material material = button.getMaterialButton();

            if (material == null) {
                ASeller.getInstance().getLogger().warning("Material is null for button item in createMenuContents");
                return;
            }

            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                itemMeta.getPersistentDataContainer().set(new NamespacedKey(ASeller.getInstance(), "num"), PersistentDataType.INTEGER, 5);

                List<String> lore = button.getLoreButton().stream()
                        .map(s -> Hex.hex(setPlaceholders(player, s)))
                        .map(s -> s.replace("%seller_pay%", "0"))
                        .toList();

                itemMeta.setLore(lore);
                itemMeta.setDisplayName(button.getDisplayNameButton());
                itemStack.setItemMeta(itemMeta);
            }

            if (button.getCommand().contains("[sell_zone]")) {
                activeListSlots.add(button.getSlotButton());
            } else if (button.getCommand().contains("[sell]")) {
                buttonSlots.put(button.getSlotButton(), "[sell]");
            }
            button.getCommand().forEach(vault -> {
                if(vault.startsWith("[open_menu]")){
                    buttonSlots.put(button.getSlotButton(), vault);
                }
            });


            int slot = button.getSlotButton();

            if (slot < 0 || slot >= mainMenu.getSize()) {
                ASeller.getInstance().getLogger().warning("Invalid slot " + slot + " for button item in createMenuContents");
                return;
            }

            sellerMenu.setItem(slot, itemStack);
        });

        for (int i = 0; i < mainMenu.getSize(); i++) {
            if (!activeListSlots.contains(i)) {
                noActiveListSlots.add(i);
            }
        }
    }

    private void updateMenuContents() {
        sellerMenu.clear();
        createMenuContents();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isActive()) return;

        Inventory clickedInventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();

        if (!openMenus.containsKey(player)) return;

        CommandMenu commandMenu = openMenus.get(player);
        if (!event.getView().getTitle().equals(Hex.hex(commandMenu.getMainMenu().getDisplayNameMenu()))) return;

        if (clickedInventory != null && event.getSlotType() != InventoryType.SlotType.OUTSIDE) {
            int slot = event.getRawSlot();
            System.out.println("Игрок: " + player.getName() + " вызвал onInventoryClick");
            if (event.isShiftClick()) {
                if (clickedInventory == player.getInventory()) {
                    ItemStack currentItem = event.getCurrentItem();

                    if (currentItem != null) {
                        Inventory topInventory = event.getView().getTopInventory();
                        int firstEmptySlot = topInventory.firstEmpty();

                        if (noActiveListSlots.contains(firstEmptySlot)) {
                            event.setCancelled(true);
                            player.updateInventory();
                            return;
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
                        mainMenu.getButton().forEach(button -> {
                            if (button.getCommand().contains("[sell]")) {
                                checkInventory(player, event.getView().getTopInventory(), ASeller.getInstance().getCountMaterial(), button, activeListSlots, pay_count);
                            }
                        });
                    }, 1L);
                    return;
                }
            }

            if (clickedInventory == event.getView().getTopInventory()) {
                Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
                    mainMenu.getButton().forEach(button -> {
                        if (button.getCommand().contains("[sell]")) {
                            checkInventory(player, event.getView().getTopInventory(), ASeller.getInstance().getCountMaterial(), button, activeListSlots, pay_count);
                        }
                    });
                }, 1L);
            }

            if (noActiveListSlots.contains(slot)) {
                event.setCancelled(true);
                player.updateInventory();
            }


            if (buttonSlots.containsKey(slot)) {
                if ("[sell]".equals(buttonSlots.get(slot))) {
                    Inventory topInventory = event.getView().getTopInventory();
                    SellZone.sellItem(player, activeListSlots, topInventory, ASeller.getInstance().getCountMaterial(), pay_count);
                    System.out.println("Игрок: " + player.getName() + " вызвал команду sell");
                } else if (buttonSlots.get(slot).startsWith("[open_menu]")) {
                    String nameMenu = buttonSlots.get(slot).substring("[open_menu]".length()).trim();
                    System.out.println(nameMenu);

                    // Закрываем текущее меню и удаляем из карты openMenus
                    player.closeInventory();
                    openMenus.remove(player);

                    // Открываем новое меню
                    OpenMenu.openMenu(nameMenu, player);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isActive()) return;
        Player clickedPlayer = (Player) event.getWhoClicked();
        if (!openMenus.containsKey(clickedPlayer)) {
            return;
        }

        CommandMenu commandMenu = openMenus.get(clickedPlayer);
        if (!event.getView().getTitle().equals(Hex.hex(commandMenu.getMainMenu().getDisplayNameMenu()))) {
            return;
        }
        Inventory topInventory = event.getView().getTopInventory();

        if (event.getInventory().equals(topInventory)) {
            Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> {
                mainMenu.getButton().forEach(button -> {
                    if (button.getCommand().contains("[sell]")) {
                        checkInventory((Player) event.getWhoClicked(), topInventory, ASeller.getInstance().getCountMaterial(), button, activeListSlots, pay_count);
                    }
                });
            }, 1L);
        }

        for (Integer slot : event.getRawSlots()) {
            if (noActiveListSlots.contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!isActive()) return;

        if (event.getView().getTitle().equals(Hex.hex(mainMenu.getDisplayNameMenu()))) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            if (player.equals(this.player)) {
                returnItemsToPlayerOrDrop(player, inventory);

                unregister();

             //   openMenus.remove(player);
            //    if (inventory.equals(sellerMenu)) {
             //       sellerMenu = null;
             //   }
                if (getPay_count().containsKey(player.getUniqueId())) {
                    clearPayCount(player.getUniqueId());
                }

                Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), player::updateInventory, 1L);
                ASeller.getInstance().getLogger().info("Inventory closed, updating player inventory...");

            }
        }
    }

    public void unregister() {

    }

    public boolean isActive() {
        return openMenus.get(player) == this;
    }

    private void returnItemsToPlayerOrDrop(Player player, Inventory inventory) {
        for (Integer slot : activeListSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), item);
                } else {
                    player.getInventory().addItem(item);
                }
                inventory.setItem(slot, null);
            }
        }
    }

    public Menu getMainMenu() {
        return mainMenu;
    }

    public static void clearPayCount(UUID playerUUID) {
        pay_count.remove(playerUUID);
    }

    public static Map<UUID, Double> getPay_count() {
        return pay_count;
    }

    public static void addPayCount(UUID uuid, Double d) {
        pay_count.put(uuid, d);
    }
}