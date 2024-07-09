package ru.anime.aseller.menuSetting;

import java.util.List;

public class Category {
    String idCategory;
    List<CategoryItem> categoryItemsList;
    List<Integer>slots;

    public Category(String idCategory, List<CategoryItem> categoryItemsList, List<Integer> slots) {
        this.idCategory = idCategory;
        this.categoryItemsList = categoryItemsList;
        this.slots = slots;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public List<CategoryItem> getCategoryItemsList() {
        return categoryItemsList;
    }

    public List<Integer> getSlots() {
        return slots;
    }
}

