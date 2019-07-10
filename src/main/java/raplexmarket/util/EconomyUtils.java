package raplexmarket.util;

import org.bukkit.OfflinePlayer;
import raplexmarket.RaplexMarket;

public class EconomyUtils {
    private static RaplexMarket plugin = RaplexMarket.getInstance();

    public static boolean canBuy(OfflinePlayer player, double amount) {
        return plugin.getEconomy().has(player, amount);
    }
}
