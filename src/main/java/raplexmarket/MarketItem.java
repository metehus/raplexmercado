package raplexmarket;

import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import raplexmarket.commands.MarketCommand;
import raplexmarket.listeners.InventoryClick;
import raplexmarket.util.*;
import sun.security.krb5.Config;

import java.util.*;

public class MarketItem {
    private UUID itemId;
    private OfflinePlayer owner;
    private long price;
    private Date started;
    private boolean expired;
    private Category category;
    private List<ItemStack> items;
    private ConfigUtils config = RaplexMarket.getInstance().getConfigUtils();
    private FileConfiguration cfg = RaplexMarket.getInstance().getConfig();
    private BukkitTask task;

    public MarketItem(UUID uuid, long price, long started, boolean expired, Category category, List<ItemStack> items, UUID itemId) {
        this.itemId = itemId;
        this.owner = Bukkit.getOfflinePlayer(uuid);
        this.price = price;
        this.started = new Date(started);
        this.expired = expired;
        this.category = category;
        this.items = items;
    }

    public UUID getItemId() {
        return itemId;
    }

    public Date getStarted() {
        return started;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public long getPrice() {
        return price;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public Category getCategory() {
        return category;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public int getAmount(){
        int i = 0;
        for (ItemStack item : getItems()) {
            i += item.getAmount();
        }
        return i;
    }

    public MarketItemType getMarketType() {
        return getItems().size() > 1 ? MarketItemType.CHEST : MarketItemType.SINGLE_ITEM;
    }

    public ItemStack getIcon(Player buyer) {
        String type = buyer.getUniqueId().equals(owner.getUniqueId()) ? "collect" : "buy";
        String canBuyColor = EconomyUtils.canBuy(buyer, (double) getPrice()) ? "can-buy" : "cant-buy";
        String time = DurationFormatUtils.formatDuration(getLeftTime(), config.getLang("item.time"));
        if (getItems().size() == 1) {
            ItemStack item = new ItemStack(getItems().get(0));
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (meta.hasLore())
                meta.getLore().forEach(l -> {
                    lore.add(l);
                });
            config.getFormattedList("item.item." + type + "-lore").forEach(m -> {
                lore.add(m
                        .replace("{player}", owner.getName())
                        .replace("{price}", Long.toString(getPrice()))
                        .replace("{time}", time)
                        .replace("&p", config.getLang("item." + canBuyColor))
                        .replace("{amount}", Integer.toString(getAmount())));
            });
            meta.setLore(lore);
            item.setItemMeta(meta);
            return NBTUtils.setString(item, "itemid", getItemId().toString());
        } else {
            ItemStack chest = new ItemStack(Material.CHEST);
            ItemMeta meta = chest.getItemMeta();
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setDisplayName(config.getLang("item.chest.name"));
            List<String> lore = new ArrayList<>();
            config.getFormattedList("item.chest." + type + "-lore").forEach(m -> {
                lore.add(m
                        .replace("{player}", owner.getName())
                        .replace("{price}", Long.toString(getPrice()))
                        .replace("&p", config.getLang("item." + canBuyColor))
                        .replace("{amount}", Integer.toString(getAmount())));
            });
            meta.setLore(lore);
            chest.setItemMeta(meta);
            return NBTUtils.setString(chest, "itemid", getItemId().toString());
        }
    }

    public void buy(Player player) {
        if (expired) {
            player.sendMessage(RaplexMarket.getInstance().getConfigUtils().getLang("buy.expired"));
            player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
            return;
        }
        if (!EconomyUtils.canBuy(player, price)) {
            player.sendMessage(RaplexMarket.getInstance().getConfigUtils().getLang("buy.money"));
            player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
            return;
        }
        EconomyResponse res = RaplexMarket.getInstance().getEconomy().withdrawPlayer(player, price);
        if (res.transactionSuccess()) {
            EconomyResponse res2 = RaplexMarket.getInstance().getEconomy().depositPlayer(owner, price);
            if (res2.transactionSuccess()) {
                player.sendMessage(RaplexMarket.getInstance().getConfigUtils().getLang("buy.success"));
                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                if (cfg.getBoolean("buy.broadcast.seller.enable"))
                    if (owner.isOnline())
                        owner.getPlayer().sendMessage(config.getLang("buy.broadcast.seller.msg")
                                .replace("{seller}", owner.getName())
                                .replace("{price}", Long.toString(price))
                                .replace("{player}", player.getName()));
                if (cfg.getBoolean("buy.broadcast.everyone.enable")) {
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (p.getUniqueId().equals(owner.getUniqueId()) && cfg.getBoolean("buy.broadcast.seller.exclusive")) continue;
                        p.sendMessage(config.getLang("buy.broadcast.everyone.msg")
                                .replace("{seller}", owner.getName())
                                .replace("{price}", Long.toString(price))
                                .replace("{player}", player.getName()));
                    }
                }
                boolean freeSpace = false;
                for (ItemStack i : player.getInventory()) {
                    if (i == null || i.getType() == Material.AIR)
                        freeSpace = true;
                }
                if (freeSpace)
                    player.getInventory().addItem(giveableItem());
                else {
                    player.sendMessage(RaplexMarket.getInstance().getConfigUtils().getLang("buy.space"));
                    player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
                    player.getLocation().getWorld().dropItem(player.getLocation(), giveableItem());
                }
                remove();
                reloadPlayers();
                return;
            }

        }
    }

    public ItemStack giveableItem() {
        if (getMarketType() == MarketItemType.SINGLE_ITEM) return items.get(0);
        List<String> lore = new ArrayList<>();
        config.getFormattedList("item.chest.chest-item.lore").forEach(l -> {
            lore.add(l
                    .replace("{amount}", Integer.toString(getAmount()))
                    .replace("{player}", owner.getName()));
        });
        ItemStack item = ItemCreator.add(Material.CHEST, config.getLang("item.chest.chest-item.name"), 1, lore.toArray(new String[lore.size()]));

        net.minecraft.server.v1_8_R1.ItemStack itemnms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (itemnms.hasTag()) tag = itemnms.getTag();
        else tag = new NBTTagCompound();

        MarketChest marketChest = new MarketChest(UUID.randomUUID(), getItems());

        tag.setString("id", marketChest.getUUID().toString());
        itemnms.setTag(tag);

        RaplexMarket.getInstance().addChest(marketChest);

        return CraftItemStack.asBukkitCopy(itemnms);
    }

    public void collect(Player player) {
        int freeSpace = 0;
        for (ItemStack i : player.getInventory()) {
            if (i == null || i.getType() == Material.AIR)
                freeSpace++;
        }
        if (freeSpace >= getItems().size()) {
            for (ItemStack itemStack : getItems()) {
                player.getInventory().addItem(itemStack);
            }
            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
            reloadPlayers();
            remove();
            if (MiscUtils.getPlayerExpiredItens(player).size() == 0) {
                player.closeInventory();
            } else {
                MarketCommand cmd = new MarketCommand(RaplexMarket.getInstance());
                player.openInventory(cmd.collectInventory(player));
            }
        } else {
            player.sendMessage(RaplexMarket.getInstance().getConfigUtils().getLang("collect.no-space"));
            player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1, 1);
            return;
        }
    }

    public void createTask() {
        RaplexMarket plugin = RaplexMarket.getInstance();
        BukkitTask task = new MarketTask(plugin, this).runTaskLater(plugin, 20 * (plugin.getConfig().getInt("expire-time") * 60));
        this.task = task;
    }

    public void remove() {
        if (task != null)
            task.cancel();
        category.removeMarketItem(this);
        RaplexMarket.getInstance().removeMarketItem(this);
    }

    public void reloadPlayers() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            String inv = player.getOpenInventory().getTitle();
            boolean pageturn = inv.endsWith(RaplexMarket.guiC + ChatColor.translateAlternateColorCodes('&', "&5&7&2"));
            PlayerSelectionManager playerSelectionManager = RaplexMarket.getInstance().getPlayerSelectionManager();
            if (pageturn) {
                InventoryClick ic = new InventoryClick(RaplexMarket.getInstance());
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1);
                player.openInventory(ic.getCategoryPage(player, playerSelectionManager.getPlayerCategory(player), playerSelectionManager.getPlayerPage(player)));
            }
        }
    }

    public long getLeftTime() {
        Date now = new Date();
        return getEndTime().getTime() - now.getTime();
    }

    public Date getEndTime() {
        return DateUtils.addMinutes(started, RaplexMarket.getInstance().getConfig().getInt("expire-time"));
    }
}
