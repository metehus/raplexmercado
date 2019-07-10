package raplexmarket.util;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import raplexmarket.RaplexMarket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigUtils {
    private RaplexMarket plugin;

    public ConfigUtils(RaplexMarket plugin) {
        this.plugin = plugin;
    }

    public String getLang(String path) {
        if (plugin.getConfig().getString(path) == null) {
            System.out.println("RAPLEXMARKET -------------- MISSING STRING: " + path);
            return " ";
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path));
    }

    public List<String> getFormattedList(String path) {
        List<String> msgs = new ArrayList<>();
        plugin.getConfig().getStringList(path).forEach(m -> {
            if (m == null)
                msgs.add(" ");
            else
                msgs.add(ChatColor.translateAlternateColorCodes('&', m));
        });
        return msgs;
    }

    public String[] getFormattedArray(String path) {
        List<String> msgs = getFormattedList(path);
        String[] arr = new String[msgs.size()];
        for (int i = 0; i < msgs.size(); i++) {
            arr[i] = msgs.get(i);
        }
        return arr;
    }
}
