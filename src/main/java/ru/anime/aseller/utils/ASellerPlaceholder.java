package ru.anime.aseller.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.anime.aseller.ASeller;
import ru.anime.aseller.GUIMenu.MainMenu;
import ru.anime.aseller.commands.CommandMenu;

public class ASellerPlaceholder extends PlaceholderExpansion {

    private final ASeller plugin;

    public ASellerPlaceholder(ASeller plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return null;
    }
    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    @Override
    public String getIdentifier() {

        return "seller";
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (identifier.startsWith("pay")) {


        }
        if (identifier.startsWith("next_")) {


        }
        if (identifier.startsWith("time_")) {



        }
            return null;
        }
}
