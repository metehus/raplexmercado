package raplexmarket.listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import raplexmarket.util.*;

import java.util.*;

public class InventoryClick implements Listener {
    private RaplexMarket plugin;
    private ConfigUtils configUtils;
    private FileConfiguration config;
    private PlayerSelectionManager playerSelectionManager;

    public InventoryClick(RaplexMarket plugin) {
        this.plugin = plugin;
        this.configUtils = plugin.getConfigUtils();
        this.config = plugin.getConfig();
        this.playerSelectionManager = plugin.getPlayerSelectionManager();
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String chestsell = configUtils.getLang("sell.chest.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&c");

        Inventory inv = event.getInventory();
        final Player player = (Player) event.getWhoClicked();
        boolean pageturn = inv.getName().endsWith(RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&5&7&2"));
        if (!inv.getName().contains(RaplexMarket.guiC)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() == Material.AIR && !pageturn) return;
        if (event.getClickedInventory() == player.getInventory()) {
            event.setCancelled(true);
            return;
        }
        System.out.println(event.getClickedInventory() == player.getInventory());
        String cattitle = configUtils.getLang("view.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&3");
        String ctitle = configUtils.getLang("confirm.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&6");
        String collect = configUtils.getLang("collect.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&e");
        if (inv.getName().equals(cattitle)) {
            event.setCancelled(true);
            if (!event.getClick().isLeftClick())
                player.openInventory(inv);
            handleCategorySelect(player, event.getSlot(), event.getCurrentItem());
            return;
        }
        if (pageturn) {
            event.setCancelled(true);
            if (!event.getClick().isLeftClick())
                player.openInventory(inv);
            handleCategoryClick(player, event.getSlot(), event.getCurrentItem());
            return;
        }
        if (inv.getName().equals(ctitle)) {
            event.setCancelled(true);
            handleConfirmClick(player, event.getSlot(), event.getCurrentItem());
            return;
        }
        if (inv.getName().equals(collect)) {
            event.setCancelled(true);
            handleCollectClick(player, event.getSlot(), event.getCurrentItem());
            return;
        }
        if (inv.getName().equals(chestsell)) {
            return;
//            if (event.getClickedInventory() != player.getInventory()) {
//                if (event.getSlot() >= 27 && event.getSlot() <= 35) {
//                    String nam = event.getCurrentItem().getItemMeta().getDisplayName();
//                    if (nam.equals(configUtils.getLang("sell.chest.chest.name"))) {
//                        event.setCancelled(true);
//                        return;
//                    }
//                    List<ItemStack> items = new ArrayList<>();
//                    for (ItemStack item : event.getInventory().getContents()) {
//                        if (item == null) continue;
//                        if (item.getType() == Material.AIR) continue;
//                        if (item.hasItemMeta())
//                            if (item.getItemMeta().hasDisplayName()) {
//                                String in = item.getItemMeta().getDisplayName();
//                                System.out.println(in);
//                                if (in.equals(configUtils.getLang("sell.chest.cancel.name"))
//                                        || in.equals(configUtils.getLang("sell.chest.confirm.name"))
//                                        || in.equals(configUtils.getLang("sell.chest.chest.name"))) continue;
//                            }
//                        items.add(item);
//                    }
//                    player.sendMessage(nam);
//                    if (nam.equals(configUtils.getLang("sell.chest.cancel.name"))) {
//                        player.closeInventory();
//                        event.setCancelled(true);
//                        playerSelectionManager.addClosingGUI(player);
//                        for (ItemStack item : items) {
//                            player.getLocation().getWorld().dropItem(player.getLocation(), item);
//                        }
//                        return;
//                    }
//                    if (nam.equals(configUtils.getLang("sell.chest.confirm.name"))) {
//
//                        if (items.size() == 0) {
//                            event.setCancelled(true);
//                            player.closeInventory();
//                        }
//
//                        String ccat = config.getString("category-chest");
//                        Category chestCategory = plugin.getCategories().get(ccat);
//
//                        long price = playerSelectionManager.getPlayerPrice(player);
//

//                        MarketItem mi = new MarketItem(UUID.fromString("95edd0fa-8549-3bd5-ae9b-d62c76a5d7e2"), price, new Date().getTime(), false, chestCategory, items, UUID.randomUUID());
//                        chestCategory.addMarketItem(mi, true);
//                        event.setCancelled(true);
//                        player.closeInventory();
//
//                        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
//                        player.sendMessage(configUtils.getLang("sell.added"));
//                        if (config.getBoolean("broadcast-sell.chat.enable")) {
//                            String msg = configUtils.getLang("broadcast-sell.chat.chest")
//                                    .replace("{cat}", chestCategory.getName())
//                                    .replace("{price}", Long.toString(price))
//                                    .replace("{amount}", Integer.toString(mi.getAmount()))
//                                    .replace("{player}", player.getName());
//                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
//                                if (config.getBoolean("broadcast-sell.chat.clickable")) {
//                                    TextComponent tc = new TextComponent(msg);
//                                    tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(configUtils.getLang("broadcast-sell.chat.hold-msg")).create()));
//                                    tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mercado ver " + mi.getItemId()));
//                                    player.spigot().sendMessage(tc);
//                                } else {
//                                    p.sendMessage(msg);
//                                }
//                            }
//                        }
//                        if (config.getBoolean("broadcast-sell.action-bar.enable")) {
//                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
//                                String msg = configUtils.getLang("broadcast-sell.action-bar.chest")
//                                        .replace("{cat}", chestCategory.getName())
//                                        .replace("{price}", Long.toString(price))
//                                        .replace("{amount}", Integer.toString(mi.getAmount()))
//                                        .replace("{player}", player.getName());
//                                ActionBar.sendActionBarMessage(player, msg);
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    public void handleCategorySelect(Player player, int slot, ItemStack item) {
        HashMap<Integer, Category> catslots = new HashMap<>();

        List<Category> categories = new ArrayList<>();
        categories.addAll(plugin.getCategories().values());

        if (slot == config.getInt("view.collect-slot")) {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            MarketCommand cmd = new MarketCommand(plugin);
            player.openInventory(cmd.collectInventory(player));
            return;
        }

        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            catslots.put(c.getSlot(), c);
        }

        if (catslots.get(slot) != null) {
            Category category = catslots.get(slot);
            if (!item.getItemMeta().getDisplayName().equals(category.getDisplayName())) return;

            playerSelectionManager.setPlayerCategory(player, category);
            playerSelectionManager.setPlayerPage(player, 0);
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            player.openInventory(getCategoryPage(player, category, 0));
        }
    }

    public void handleCategoryClick(Player player, int slot, ItemStack item) {
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            playerSelectionManager.clearPlayer(player);
            MarketCommand mcmd = new MarketCommand(plugin);
            player.openInventory(mcmd.mainInventory(player));
            return;
        }
        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            player.openInventory(getCategoryPage(player, playerSelectionManager.getPlayerCategory(player), playerSelectionManager.getPlayerPage(player)));
            return;
        }
        int page = playerSelectionManager.getPlayerPage(player);
        boolean previousPage = page > 0;
        boolean nextPage = false;

        Category category = playerSelectionManager.getPlayerCategory(player);
        List<MarketItem> marketItems = category.getMarketItems(false);
        for (int i = (45 * page); i < marketItems.size(); i++) {
            if (i == (45 * (page + 1))) {
                nextPage = true;
                break;
            }
        }
        if (slot == 48) {
            if (previousPage) {
                playerSelectionManager.setPlayerPage(player, page - 1);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
                player.openInventory(getCategoryPage(player, category, page - 1));
            } else {
                player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
            }
            return;
        }

        if (slot == 50) {
            if (nextPage) {
                playerSelectionManager.setPlayerPage(player, page + 1);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
                player.openInventory(getCategoryPage(player, category, page + 1));
            } else {
                player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
            }
            return;
        }

        if (slot == 53) {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            MarketCommand cmd = new MarketCommand(plugin);
            player.openInventory(cmd.collectInventory(player));
            return;
        }

        if (item == null) return;
        if (item.getType() == Material.AIR) return;

        MarketItem clickedItem = plugin.getMarketItem(UUID.fromString(NBTUtils.getTags(item).getString("itemid")));
        if (clickedItem == null) {
            playerSelectionManager.setPlayerCategory(player, category);
            playerSelectionManager.setPlayerPage(player, 0);
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
            player.openInventory(getCategoryPage(player, category, 0));
        } else {
            if (!clickedItem.getIcon(player).equals(item)) {
                playerSelectionManager.setPlayerCategory(player, category);
                playerSelectionManager.setPlayerPage(player, 0);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
                player.openInventory(getCategoryPage(player, category, 0));
            }
            if (clickedItem.getOwner().getUniqueId().equals(player.getUniqueId())) {
                clickedItem.collect(player);
                player.openInventory(getCategoryPage(player, playerSelectionManager.getPlayerCategory(player), playerSelectionManager.getPlayerPage(player)));
                return;
            }
            plugin.getPlayerSelectionManager().setPlayerConfirm(player, clickedItem);
            MarketCommand cmd = new MarketCommand(plugin);
            player.openInventory(cmd.confirmItem(player));

        }

    }

    public void handleConfirmClick(Player player, int slot, ItemStack item) {
        MarketItem mi = playerSelectionManager.getPlayerConfirm(player);

        for (Integer st : config.getIntegerList("confirm.cancel-slots")) {
            if (slot != st) continue;
            if (item.getType().equals(Material.STAINED_GLASS_PANE) && item.getDurability() == 14) {
                playerSelectionManager.clearPlayer(player);
                player.closeInventory();
                return;
            }
        }
        for (Integer st : config.getIntegerList("confirm.confirm-slots")) {
            if (slot != st) continue;
            if (item.getType().equals(Material.STAINED_GLASS_PANE) && item.getDurability() == 5) {
                playerSelectionManager.clearPlayer(player);
                if (mi == null) {
                    player.sendMessage(configUtils.getLang("buy.already"));
                    player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
                    return;
                }
                mi.buy(player);
                player.closeInventory();
                return;
            }
        }
    }


    public Inventory getCategoryPage(Player player, Category category, int page) {

        String title = configUtils.getLang("category.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&5&7&2");
        Inventory inv = Bukkit.createInventory(null, 9 * 6, title
                .replace("{page}", Integer.toString(page + 1))
                .replace("{name}", category.getName()));

        List<MarketItem> marketItems = category.getMarketItems(false);
        boolean previousPage = page > 0;
        boolean nextPage = false;

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

    private void handleCollectClick(Player player, int slot, ItemStack item) {
        MarketItem collect = MiscUtils.getPlayerExpiredItens(player).get(slot);
        if (collect == null) return;
        if (!item.equals(collect.getIcon(player))) {
            MarketCommand cmd = new MarketCommand(plugin);
            player.openInventory(cmd.collectInventory(player));
        }

        collect.collect(player);
    }
}
