package ru.anime.aseller.menuSetting;

import org.bukkit.Material;

import java.util.List;

public class ButtonMenu {
    Integer slotButton;
    String DisplayNameButton;
    List<String> loreButton;
    Material materialButton;
    List<String> command;

    public ButtonMenu(Integer slotButton, String displayNameButton, List<String> loreButton, Material materialButton, List<String> command) {
        this.slotButton = slotButton;
        DisplayNameButton = displayNameButton;
        this.loreButton = loreButton;
        this.materialButton = materialButton;
        this.command = command;
    }

    public Integer getSlotButton() {
        return slotButton;
    }

    public String getDisplayNameButton() {
        return DisplayNameButton;
    }

    public List<String> getLoreButton() {
        return loreButton;
    }

    public Material getMaterialButton() {
        return materialButton;
    }

    public List<String> getCommand() {
        return command;
    }
}
