package ru.anime.aseller.menuSetting;

import java.util.List;

public class Menu {
    String DisplayNameMenu;
    String Id;
    String commandOpenMenu;
    String permissionOpenMenu;
    Integer Size;
    List<ButtonMenu> button;
    List<Category> category;

    public Menu(String displayNameMenu, String id, String commandOpenMenu, String permissionOpenMenu,
                Integer size, List<ButtonMenu> button, List<Category> category) {
        this.DisplayNameMenu = displayNameMenu;
        this.Id = id;
        this.commandOpenMenu = commandOpenMenu;
        this.permissionOpenMenu = permissionOpenMenu;
        this.Size = size;
        this.button = button;
        this.category = category;
    }

    public String getDisplayNameMenu() {
        return DisplayNameMenu;
    }

    public String getId() {
        return Id;
    }

    public String getCommandOpenMenu() {
        return commandOpenMenu;
    }

    public String getPermissionOpenMenu() {
        return permissionOpenMenu;
    }

    public Integer getSize() {
        return Size;
    }
    public List<ButtonMenu> getButton() {
        return button;
    }

    public List<Category> getCategory() {
        return category;
    }
}
