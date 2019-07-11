package raplexmarket;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import raplexmarket.listeners.InventoryClick;
import raplexmarket.util.PlayerSelectionManager;

public class MarketTask extends BukkitRunnable {

    private final RaplexMarket plugin;
    private final MarketItem item;
    private final PlayerSelectionManager playerSelectionManager;

    public MarketTask(RaplexMarket plugin, MarketItem item) {
        this.plugin = plugin;
        this.item = item;
        this.playerSelectionManager = plugin.getPlayerSelectionManager();
    }

    @Override
    public void run() {
        if (plugin.getConfig().getBoolean("debug-log")) {
            plugin.getLogger().info("Removing item " + item.getItemId());
        }
        item.setExpired(true);
        item.reloadPlayers();
    }
}
