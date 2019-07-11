package raplexmarket.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import raplexmarket.RaplexMarket;
import raplexmarket.util.ConfigUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryClose implements Listener {
    private RaplexMarket plugin;
    private ConfigUtils configUtils;
    private FileConfiguration config;

    public InventoryClose(RaplexMarket plugin) {
        this.plugin = plugin;
        this.configUtils = plugin.getConfigUtils();
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String chestsell = configUtils.getLang("sell.chest.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&c");

        if (event.getInventory().getName().equals(chestsell)) {
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack item : event.getInventory().getContents()) {
                if (item == null) continue;
                if (item.getType() == Material.AIR) continue;
                if (item.hasItemMeta())
                    if (item.getItemMeta().hasDisplayName()) {
                        String in = item.getItemMeta().getDisplayName();
                        System.out.println(in);
                        if (in.equals(configUtils.getLang("sell.chest.cancel.name"))
                                || in.equals(configUtils.getLang("sell.chest.confirm.name"))
                                || in.equals(configUtils.getLang("sell.chest.chest.name"))) continue;
                    }
                if (!RaplexMarket.getInstance().getPlayerSelectionManager().isClosingGUI((Player) event.getPlayer())) event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), item);
            }
        }
    }
}
