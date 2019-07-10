package raplexmarket.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import raplexmarket.Category;
import raplexmarket.MarketItem;
import raplexmarket.RaplexMarket;
import raplexmarket.commands.MarketCommand;
import raplexmarket.util.ConfigUtils;
import raplexmarket.util.MiscUtils;
import raplexmarket.util.PageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryClick implements Listener {
    private RaplexMarket plugin;
    private ConfigUtils configUtils;
    private FileConfiguration config;
    private PageManager pageManager;

    public InventoryClick(RaplexMarket plugin) {
        this.plugin = plugin;
        this.configUtils = plugin.getConfigUtils();
        this.config = plugin.getConfig();
        this.pageManager = plugin.getPageManager();
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        final Player player = (Player) event.getWhoClicked();
        boolean pageturn = inv.getName().endsWith(RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&5&7&2"));
        if (!inv.getName().contains(RaplexMarket.guiC)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() == Material.AIR && !pageturn) return;
        if (event.getClickedInventory() == player.getInventory()) event.setCancelled(true); // TODO: CHEST SELL
        String cattitle = configUtils.getLang("view.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&3");
        if (inv.getName().equals(cattitle)) {
            if (!event.getClick().isLeftClick())
                player.openInventory(inv);
            event.setCancelled(true);
            handleCategorySelect(player, event.getSlot(), event.getCurrentItem());
            return;
        }
        if (pageturn) {
            if (!event.getClick().isLeftClick())
                player.openInventory(inv);
            event.setCancelled(true);
            handleCategoryClick(player, event.getSlot(), event.getCurrentItem());
            return;
        }
    }

    public void handleCategorySelect(Player player, int slot, ItemStack item) {
        HashMap<Integer, Category> catslots = new HashMap<>();

        List<Category> categories = new ArrayList<>();
        categories.addAll(plugin.getCategories().values());

        if (slot == config.getInt("view.collect-slot")) {
            // TODO: open collect gui
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            player.sendMessage("collect gui");
            return;
        }

        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            catslots.put(c.getSlot(), c);
        }

        if (catslots.get(slot) != null) {
            Category category = catslots.get(slot);
            if (!item.getItemMeta().getDisplayName().equals(category.getDisplayName())) return;

            pageManager.setPlayerCategory(player, category);
            pageManager.setPlayerPage(player, 0);
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            player.openInventory(getCategoryPage(player, category, 0));
        }
    }
    public void handleCategoryClick(Player player, int slot, ItemStack item) {
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            pageManager.clearPlayer(player);
            MarketCommand mcmd = new MarketCommand(plugin);
            player.openInventory(mcmd.mainInventory(player));
            return;
        }
        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            getCategoryPage(player, pageManager.getPlayerCategory(player), pageManager.getPlayerPage(player));
            return;
        }
        int page = pageManager.getPlayerPage(player);
        boolean previousPage = page > 0;
        boolean nextPage = false;

        System.out.println("AAA " + page);

        Category category = pageManager.getPlayerCategory(player);
        List<MarketItem> marketItems = category.getMarketItems();
        for (int i = (45 * page); i < marketItems.size(); i++) {
            if (i == (45 * (page + 1))) {
                nextPage = true;
                break;
            }
        }
        if (slot == 48) {
            if (previousPage) {
                pageManager.setPlayerPage(player, page - 1);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
                player.openInventory(getCategoryPage(player, category, page - 1));
            } else {
                System.out.println("no");
                player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
            }
            System.out.println("afte " + pageManager.getPlayerPage(player));
            return;
        }

        if (slot == 50) {
            if (nextPage) {
                pageManager.setPlayerPage(player, page + 1);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
                player.openInventory(getCategoryPage(player, category, page + 1));
            } else {
                System.out.println("no");
                player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
            }
            System.out.println("afte " + pageManager.getPlayerPage(player));
            return;
        }
    }

    private Inventory getCategoryPage(Player player, Category category, int page) {

        System.out.println("PAGE> " + pageManager.getPlayerPage(player));
        String title = configUtils.getLang("category.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&5&7&2");
        Inventory inv = Bukkit.createInventory(null, 9 * 6, title
                .replace("{page}", Integer.toString(page + 1))
                .replace("{name}", category.getName()));

        List<MarketItem> marketItems = category.getMarketItems();
        boolean previousPage = page > 0;
        boolean nextPage = false;

        System.out.println((45 * page));
        System.out.println(marketItems.size());

        for (int i = (45 * page); i < marketItems.size(); i++) {
            if (i == (45 * (page + 1))) {
                nextPage = true;
                break;
            }
            MarketItem mi = marketItems.get(i);

            inv.setItem(i - (45 * page), mi.getIcon(player));
        }



        ItemStack refresh = category.getIcon();
        refresh.setAmount(1);
        ItemMeta meta = refresh.getItemMeta();
        meta.setDisplayName(configUtils.getLang("category.refresh.name"));
        meta.setLore(configUtils.getFormattedList("category.refresh.lore"));
        refresh.setItemMeta(meta);


        inv.setItem(45, MiscUtils.getBackItem());
        if (previousPage) inv.setItem(48, MiscUtils.getPageControlItem("back"));
        inv.setItem(49, refresh);
        if (nextPage) inv.setItem(50, MiscUtils.getPageControlItem("next"));
        inv.setItem(53, MiscUtils.getCollectItem(player));

        return inv;
    }
}
