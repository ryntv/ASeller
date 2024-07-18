package ru.anime.aseller.buttonCommand;

import org.bukkit.entity.Player;
import ru.anime.aseller.commands.CommandMenu;
import ru.anime.aseller.utils.Hex;

import java.util.List;

import static ru.anime.aseller.utils.Hex.setPlaceholders;

public class Message {
    public static void sayMessage(String msg, Player player){
        String newText = msg.replace("%seller_pay%", String.valueOf(CommandMenu.getPay_count().get(player.getUniqueId())));
        player.sendMessage(Hex.hex(setPlaceholders(player, newText)));

    }
}
