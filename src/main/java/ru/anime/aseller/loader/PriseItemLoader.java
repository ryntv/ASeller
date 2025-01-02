package ru.anime.aseller.loader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.anime.aseller.ASeller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PriseItemLoader {
    // Метод для загрузки данных из файла в Map
    public static Map<String, Double> loadItemValuesFromFile(File file) {
        Map<String, Double> itemValues = new HashMap<>();
        if (!file.exists()) {
            ASeller.getInstance().getLogger().warning("Файл " + file.getName() + " не найден!");
            return itemValues;
        }

        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        // Проход по всем ключам верхнего уровня в конфигурации
        for (String key : fileConfig.getKeys(false)) {
            double value = fileConfig.getDouble(key);
            itemValues.put(key, value);
        }

        return itemValues;
    }
}
