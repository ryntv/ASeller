package ru.anime.aseller.utils;

import ru.anime.aseller.ASeller;

import java.util.ArrayList;
import java.util.List;

public class UtilSlots {
    public static List<Integer> noActiveListSlots = new ArrayList<>();
    public static List<Integer> activeListSlots = new ArrayList<>();

    private static List<Integer> sortListSlots(List<String> rawList){

        List<Integer> list = new ArrayList<>();
        // Извлекаем и обрабатываем данные

        for (String entry : rawList) {
            if (entry.contains("-")) {
                // Обработка диапазона
                String[] parts = entry.split("-");
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                for (int i = start; i <= end; i++) {
                    list.add(i);
                }
            } else {
                // Обработка отдельного числа
                list.add(Integer.parseInt(entry));
            }
        }

        return list;
    }

    public static List<Integer> getNoActiveList() {
        noActiveListSlots = sortListSlots(ASeller.getCfg().getStringList("noActiveSlots"));
        System.out.println("NO_ACTIVE");
        for (int number : noActiveListSlots) {
            System.out.println(number);
        }
        return noActiveListSlots;
    }

    public static List<Integer> getActiveListSlots() {
        activeListSlots = sortListSlots(ASeller.getCfg().getStringList("activeSlots"));
        System.out.println("ACTIVE");
        for (int number : activeListSlots) {
            System.out.println(number);
        }
        return activeListSlots;
    }
}
