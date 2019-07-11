package raplexmarket.commands;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import raplexmarket.Category;
import raplexmarket.MarketItem;
import raplexmarket.MarketItemType;
import raplexmarket.RaplexMarket;
import raplexmarket.util.*;

import javax.swing.plaf.ActionMapUIResource;
import java.util.*;

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
            if (args.length == 1) {
                String sub = args[0];
                if (sub.equals("debug")) {
                    Random random = new Random();
                    plugin.getMarketItemsList().get(random.nextInt(plugin.getMarketItemsList().size())).buy(Bukkit.getPlayer("Maath__"));
                    sender.sendMessage("removed");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.DARK_RED + "Command avaliable only for players.");
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(configUtils.getFormattedArray("help"));
            return true;
        }
        else if (args.length == 1) {
            String sub = args[0];
            if (sub.equals("ver") || sub.equals("view")) {
                player.openInventory(mainInventory(player));
                return true;
            }
            if (sub.equals("coletar") || sub.equals("collect")) {
                player.openInventory(collectInventory(player));
                return true;
            }
            if (sub.equals("debug") && player.isOp()) {
                plugin.getMarketItemsList().forEach(m -> {
                    player.sendMessage(m.getItemId().toString().split("-")[0] + " - " + m.isExpired() + " - " + m.getOwner().getName());
                });
                return true;
            }
            if (sub.equals("vender") || sub.equals("sell")) {
                player.sendMessage(configUtils.getLang("sell.no-price"));
                player.playSound(player.getLocation(), errorSound, 1, 1);
                return true;
            }
            player.sendMessage(configUtils.getFormattedArray("help"));
            player.playSound(player.getLocation(), errorSound, 1, 1);
            return true;

        }
        else if (args.length == 2) {
            String sub = args[0];
            String sub2 = args[1];
            if (sub.equals("vender") || sub.equals("sell")) {
                int limit = MiscUtils.getPlayerLimitOnPermission(player, "raplex.mercado.limit.", config.getInt("max-sell-limit"));
                if (limit <= 0) {
                    player.sendMessage(configUtils.getLang("sell.no-perm"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }
                if (MiscUtils.getPlayerSellingItems(player).size() >= limit) {
                    player.sendMessage(configUtils.getLang("sell.limit"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }
                try {
                    long price = new Long(sub2);
                } catch (Exception e) {
                    player.sendMessage(configUtils.getLang("sell.invalid"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }
                long price = new Long(sub2);
                if (price < 1 || price > 9000000000000000000L) {
                    player.sendMessage(configUtils.getLang("sell.invalid"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }
                if ((MiscUtils.getPlayerCollectables(player) + MiscUtils.getPlayerSellingItems(player).size()) >= 36) {
                    player.sendMessage(configUtils.getLang("sell.limit-expired"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }

                ItemStack item = player.getItemInHand();
                if (item == null || item.getType() == Material.AIR) {
                    player.sendMessage(configUtils.getLang("sell.no-item"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }
                Category category = null;
                for (Category cat : plugin.getCategories().values()) {
                    if (cat.getItems().contains(item.getType())) category = cat;
                }

                if (category == null) {
                    player.sendMessage(configUtils.getLang("sell.invalid-item"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }
                List<ItemStack> items = new ArrayList<>();
                items.add(item);

                MarketItem mi = new MarketItem(player.getUniqueId()

                        , price, new Date().getTime(), false, category, items, UUID.randomUUID());
                category.addMarketItem(mi, true);
                player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                player.sendMessage(configUtils.getLang("sell.added"));
                mi.createTask();
                if (config.getBoolean("broadcast-sell.chat.enable")) {
                    String msg = configUtils.getLang("broadcast-sell.chat.item")
                            .replace("{cat}", category.getName())
                            .replace("{price}", Long.toString(price))
                            .replace("{amount}", Integer.toString(item.getAmount()))
                            .replace("{player}", player.getName());
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (config.getBoolean("broadcast-sell.chat.clickable")) {
                            TextComponent tc = new TextComponent(msg);
                            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(configUtils.getLang("broadcast-sell.chat.hold-msg")).create()));
                            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mercado ver " + mi.getItemId()));
                            p.spigot().sendMessage(tc);
                        } else {
                            p.sendMessage(msg);
                        }
                    }
                }
                if (config.getBoolean("broadcast-sell.action-bar.enable")) {
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        String msg = configUtils.getLang("broadcast-sell.action-bar.item")
                                .replace("{cat}", category.getName())
                                .replace("{price}", Long.toString(price))
                                .replace("{amount}", Integer.toString(item.getAmount()))
                                .replace("{player}", player.getName());
                        ActionBar.sendActionBarMessage(p, msg);
                    }
                }

                return true;
            } else if (sub.equals("ver") || sub.equals("view")) {
                try {
                    UUID itemId = UUID.fromString(sub2);
                    MarketItem item = plugin.getMarketItem(itemId);
                    if (item.getOwner().getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(configUtils.getLang("view.cant-see-own"));
                        player.playSound(player.getLocation(), errorSound, 1, 1);
                        return true;
                    }
                    if (item.isExpired()) {
                        player.sendMessage(configUtils.getLang("view.expired"));
                        player.playSound(player.getLocation(), errorSound, 1, 1);
                        return true;
                    }
                    plugin.getPlayerSelectionManager().setPlayerConfirm(player, item);
                    if (item.getMarketType() == MarketItemType.SINGLE_ITEM)
                        player.openInventory(confirmItem(player));
                    else
                        player.openInventory(confirmChest(player));
                    return true;
                } catch (Exception e) {
                    player.sendMessage(configUtils.getLang("view.invalid-id"));
                    player.playSound(player.getLocation(), errorSound, 1, 1);
                    return true;
                }
            }
            player.sendMessage(configUtils.getFormattedArray("help"));
            player.playSound(player.getLocation(), errorSound, 1, 1);
            return true;
        }
        else {
            String sub = args[0];
            String sub2 = args[1];
            String sub3 = args[2];

//            if (sub.equals("vender") || sub.equals("sell")) {
//                if (sub3.equals("chest") || sub3.equals("bau")) {
//                    try {
//                        long price = new Long(sub2);
//                    } catch (Exception e) {
//                        player.sendMessage(configUtils.getLang("sell.invalid"));
//                        player.playSound(player.getLocation(), errorSound, 1, 1);
//                        return true;
//                    }
//                    long price = new Long(sub2);
//                    if (price < 1 || price > 9000000000000000000L) {
//                        player.sendMessage(configUtils.getLang("sell.invalid"));
//                        player.playSound(player.getLocation(), errorSound, 1, 1);
//                        return true;
//                    }
//                    openChest(player, price);
//
//                }
//
//
//
//                return true;
//            }


        }

        player.sendMessage(configUtils.getFormattedArray("help"));
        player.playSound(player.getLocation(), errorSound, 1, 1);
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

    public Inventory confirmItem(Player player) {
        String title = configUtils.getLang("confirm.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&6");
        Inventory inv = Bukkit.createInventory(null, 9 * plugin.getConfig().getInt("confirm.rows"), title);

        MarketItem item = plugin.getPlayerSelectionManager().getPlayerConfirm(player);

        ItemStack confirmItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
        ItemMeta cm = confirmItem.getItemMeta();
        cm.setLore(configUtils.getFormattedList("confirm.confirm.lore"));
        cm.setDisplayName(configUtils.getLang("confirm.confirm.name"));
        confirmItem.setItemMeta(cm);

        ItemStack cancelItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta cam = cancelItem.getItemMeta();
        cam.setLore(configUtils.getFormattedList("confirm.cancel.lore"));
        cam.setDisplayName(configUtils.getLang("confirm.cancel.name"));
        cancelItem.setItemMeta(cam);

        config.getIntegerList("confirm.confirm-slots").forEach(i -> {
            inv.setItem(i, confirmItem);
        });

        config.getIntegerList("confirm.cancel-slots").forEach(i -> {
            inv.setItem(i, cancelItem);
        });

        inv.setItem(config.getInt("confirm.preview-slot", 13), item.getIcon(player));

        return inv;
    }

    public Inventory confirmChest(Player player) {

        MarketItem item = plugin.getPlayerSelectionManager().getPlayerConfirm(player);

        String title = configUtils.getLang("confirm.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&6");
        Inventory inv = Bukkit.createInventory(null, 9 * 4, title);

        ItemStack confirmItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
        ItemMeta cm = confirmItem.getItemMeta();
        cm.setDisplayName(configUtils.getLang("confirm.confirm.name"));
        cm.setLore(configUtils.getFormattedList("confirm.confirm.lore"));

        ItemStack cancelItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta cam = cancelItem.getItemMeta();
        cam.setDisplayName(configUtils.getLang("confirm.cancel.name"));
        cam.setLore(configUtils.getFormattedList("confirm.cancel.lore"));


        confirmItem.setItemMeta(cm);
        cancelItem.setItemMeta(cam);

        for (int i = 0; i < item.getItems().size(); i++) {
            inv.setItem(i, item.getItems().get(i));
        }


        inv.setItem(27, cancelItem);
        inv.setItem(28, cancelItem);
        inv.setItem(29, cancelItem);
        inv.setItem(30, cancelItem);

        inv.setItem(31, item.getIcon(player));

        inv.setItem(32, confirmItem);
        inv.setItem(15, confirmItem);
        inv.setItem(16, confirmItem);
        inv.setItem(35, confirmItem);

        return inv;
    }

    public void openChest(Player player, long price) {
        String title = configUtils.getLang("sell.chest.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&c");
        Inventory inv = Bukkit.createInventory(null, 9 * 4, title);

        ItemStack confirmItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
        ItemMeta cm = confirmItem.getItemMeta();
        cm.setLore(configUtils.getFormattedList("sell.chest.confirm.lore"));
        cm.setDisplayName(configUtils.getLang("sell.chest.confirm.name"));
        confirmItem.setItemMeta(cm);

        ItemStack cancelItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta cam = cancelItem.getItemMeta();
        cam.setLore(configUtils.getFormattedList("sell.chest.cancel.lore"));
        cam.setDisplayName(configUtils.getLang("sell.chest.cancel.name"));
        cancelItem.setItemMeta(cam);

        ItemStack chest = new ItemStack(Material.CHEST, 1);
        ItemMeta meta = chest.getItemMeta();
        List<String> lore = new ArrayList<>();
        configUtils.getFormattedList("sell.chest.chest.lore").forEach(l -> {
            lore.add(l.replace("{price}", Long.toString(price)));
        });
        meta.setLore(lore);
        meta.setDisplayName(configUtils.getLang("sell.chest.chest.name"));
        chest.setItemMeta(meta);

        inv.setItem(27, cancelItem);
        inv.setItem(28, cancelItem);
        inv.setItem(29, cancelItem);
        inv.setItem(30, cancelItem);

        inv.setItem(31, chest);

        inv.setItem(32, confirmItem);
        inv.setItem(33, confirmItem);
        inv.setItem(34, confirmItem);
        inv.setItem(35, confirmItem);

        plugin.getPlayerSelectionManager().setPlayerPrice(player, price);

        player.openInventory(inv);
    }

    public Inventory collectInventory(Player player) {
        String title = configUtils.getLang("collect.title") + RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&e");
        Inventory inv = Bukkit.createInventory(null, 9 * 4, title);

        for (int i = 0; i < MiscUtils.getPlayerExpiredItens(player).size(); i++) {
            MarketItem item = MiscUtils.getPlayerExpiredItens(player).get(i);
            inv.setItem(i, item.getIcon(player));
        }

        return inv;
    }
}