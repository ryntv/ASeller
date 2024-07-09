package ru.anime.aseller.menuSetting;

import org.bukkit.Material;

import java.util.List;

public class CategoryItem {
    String idItem;
    String displayNameItem;
    List<String> loreItem;
    Integer priseItem;
    Material materialItem;
    String idCategoryItem;

    public CategoryItem(String idItem, String displayNameItem, List<String> loreItem, Integer priseItem,
                        Material materialItem, String idCategoryItem) {
        this.displayNameItem = displayNameItem;
        this.loreItem = loreItem;
        this.priseItem = priseItem;
        this.materialItem = materialItem;
        this.idCategoryItem = idCategoryItem;
        this.idItem = idItem;
    }

    public String getDisplayNameItem() {
        return displayNameItem;
    }

    public List<String> getLoreItem() {
        return loreItem;
    }

    public Integer getPriseItem() {
        return priseItem;
    }

    public Material getMaterialItem() {
        return materialItem;
    }

    public String getIdCategoryItem() {
        return idCategoryItem;
    }

    public String getIdItem() {
        return idItem;
    }
}
