package ru.anime.aseller.MenuManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.Menu.Menu;
import ru.anime.aseller.utils.Hex;

import java.util.*;

import static ru.anime.aseller.utils.Hex.hex;
import static ru.anime.aseller.utils.Hex.setPlaceholders;

public class MenuManager {
    private final Map<String, Menu> listMenus;
    private static final Logger LOGGER = LoggerFactory.getLogger("ASeller");

    public MenuManager(Map<String, Menu> listMenus) {
        this.listMenus = listMenus;
    }
    public void openMenu(Player player, String menuName) {
        Menu menu = listMenus.get(menuName);
        if (menu == null) {
            player.sendMessage("Меню не найдено!");
            return;
        }

        Inventory inventory = Bukkit.createInventory(menu, menu.getSize(), menu.getTitleMenu());

        menu.getButtons().forEach(entry -> {
            ItemStack itemStack = new ItemStack((entry.getMaterialButton()));
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(hex(entry.getTitleButton()));
                meta.setLore(entry.getLoreButton().stream()
                        .map(s -> Hex.hex(setPlaceholders(player, s)))
                        .map(s -> s.replace("%seller_pay%", "0")).toList());
                itemStack.setItemMeta(meta);
            }
            int slot = entry.getSlotButton();
            if (slot >= 0 && slot < menu.getSize()) {
                inventory.setItem(slot, itemStack);
            }
        });

        Bukkit.getScheduler().runTaskLater(ASeller.getInstance(), () -> player.openInventory(inventory), 1L);
    }

    public Map<String, Menu> getListMenu(){
        return listMenus;
    }


}
