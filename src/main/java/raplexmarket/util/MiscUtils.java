package raplexmarket.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import raplexmarket.MarketItem;
import raplexmarket.RaplexMarket;

import java.util.ArrayList;
import java.util.List;

public class MiscUtils {
    private static RaplexMarket plugin = RaplexMarket.getInstance();

    public static ItemStack getCollectItem(Player player) {
        int collect = MiscUtils.getPlayerCollectables(player);
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

    public static void sendActionBar(Player player, String msg) {
    }

    public static int getPlayerCollectables(Player player) {
        return MiscUtils.getPlayerExpiredItens(player).size();
    }

    public static List<MarketItem> getPlayerExpiredItens(Player player) {
        List<MarketItem> items = new ArrayList<>();
        for (MarketItem marketItem : RaplexMarket.getInstance().getMarketItemsList()) {
            if (plugin.getConfig().getBoolean("debug-log")) {
                System.out.println("ItemOwner -> " + marketItem.getOwner().getUniqueId());
                System.out.println("Player    -> " + player.getUniqueId());
                System.out.println(!marketItem.getOwner().getUniqueId().toString().equals(player.getUniqueId().toString()));
                System.out.println(marketItem.isExpired());
            }
            if (!marketItem.getOwner().getUniqueId().toString().equals(player.getUniqueId().toString())) continue;
            if (!marketItem.isExpired()) continue;
            items.add(marketItem);
        }
        return items;
    }

    public static List<MarketItem> getPlayerSellingItems(Player player) {
        List<MarketItem> items = new ArrayList<>();
        for (MarketItem marketItem : RaplexMarket.getInstance().getMarketItemsList()) {
            if (marketItem.getOwner().getUniqueId() != player.getUniqueId()) continue;
            if (marketItem.isExpired()) continue;
            items.add(marketItem);
        }
        return items;
    }

    public static int getPlayerLimitOnPermission(Player player, String perm, int max) {
        int limit = max;
        boolean found = false;
        while (!found) {
            if (limit < 0) break;
            if (player.hasPermission(perm + limit)) found = true;
            else limit--;
        }
        return limit;
    }
}
