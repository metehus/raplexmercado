package raplexmarket;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import raplexmarket.util.ConfigUtils;
import raplexmarket.util.EconomyUtils;
import sun.security.krb5.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MarketItem {
    private OfflinePlayer owner;
    private long price;
    private Date started;
    private Category category;
    private List<ItemStack> items;
    private ConfigUtils config = RaplexMarket.getInstance().getConfigUtils();

    public MarketItem(UUID uuid, long price, long started, Category category, List<ItemStack> items) {
        this.owner = Bukkit.getOfflinePlayer(uuid);
        this.price = price;
        this.started = new Date(started);
        this.category = category;
        this.items = items;
    }

    public Date getStarted() {
        return started;
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

    public ItemStack getIcon(Player buyer) {
        String type = buyer.getUniqueId().equals(owner.getUniqueId()) ? "collect" : "buy";
        String canBuyColor = EconomyUtils.canBuy(buyer, (double) getPrice()) ? "can-buy" : "cant-buy";
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
                        .replace("&p", config.getLang("item." + canBuyColor))
                        .replace("{amount}", Integer.toString(getAmount())));
            });
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
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
            return chest;
        }
    }
}
