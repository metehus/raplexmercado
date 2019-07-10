package raplexmarket.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import raplexmarket.RaplexMarket;

import java.util.ArrayList;
import java.util.List;

public class MiscUtils {
    private static RaplexMarket plugin = RaplexMarket.getInstance();

    public static ItemStack getCollectItem(Player player) {
        int collect = 3;
        ItemStack chest = new ItemStack(Material.ENDER_CHEST, collect);
        ItemMeta meta = chest.getItemMeta();
        meta.setDisplayName(plugin.getConfigUtils().getLang("item.collect-chest.name"));
        List<String> lore = new ArrayList<>();
        String p = "";
        if (collect == 0)
            p = "-none";
        else if (collect > 1)
            p = "-plural";
        plugin.getConfigUtils().getFormattedList("item.collect-chest.lore" + p).forEach(l -> {
            lore.add(l.replace("{amount}", Integer.toString(collect)));
        });
        meta.setLore(lore);
        chest.setItemMeta(meta);
        return chest;
    }

    public static ItemStack getBackItem() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(plugin.getConfigUtils().getLang("item.back.name"));
        meta.setLore(plugin.getConfigUtils().getFormattedList("item.back.lore"));
        back.setItemMeta(meta);
        return back;
    }

    public static ItemStack getPageControlItem(String direction) {
        ItemStack back = new ItemStack(Material.PAPER);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(plugin.getConfigUtils().getLang("category.page." + direction + ".name"));
        meta.setLore(plugin.getConfigUtils().getFormattedList("category.page." + direction + ".lore"));
        back.setItemMeta(meta);
        return back;
    }
}
