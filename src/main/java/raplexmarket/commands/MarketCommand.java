package raplexmarket.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import raplexmarket.Category;
import raplexmarket.MarketItem;
import raplexmarket.RaplexMarket;
import raplexmarket.util.ConfigUtils;
import raplexmarket.util.ItemSerialization;
import raplexmarket.util.MiscUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MarketCommand implements CommandExecutor {
    private final RaplexMarket plugin;
    private final Sound errorSound = Sound.BLAZE_HIT;
    private ConfigUtils configUtils;
    private FileConfiguration config;

    public MarketCommand(RaplexMarket plugin) {
        this.plugin = plugin;
        this.configUtils = plugin.getConfigUtils();
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "Command avaliable only for players.");
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(configUtils.getFormattedArray("help"));
            player.playSound(player.getLocation(), errorSound, 1, 1);
            return true;
        } else if (args.length == 1) {
            String sub = args[0];
            if (sub.equals("ver") || sub.equals("view")) {
                player.openInventory(mainInventory(player));
                return true;
            }
        }


        return true;
    }

    public Inventory mainInventory(Player player) {
        String title = configUtils.getLang("view.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&3");
        Inventory inv = Bukkit.createInventory(null, 9 * plugin.getConfig().getInt("view.rows"), title);

        List<Category> categories = new ArrayList<>();
        categories.addAll(plugin.getCategories().values());

        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            inv.setItem(c.getSlot(), c.getIcon());
        }
        inv.setItem(config.getInt("view.collect-slot"), MiscUtils.getCollectItem(player));

        return inv;
    }
}