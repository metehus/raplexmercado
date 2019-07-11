package raplexmarket;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private String displayName;
    private String icon;
    private int slot;
    private boolean enchant;
    private List<Integer> items;
    private List<String> lore;
    private List<MarketItem> marketItems = new ArrayList<>();

    public Category(String name, String displayName, String icon, int slot, boolean enchant, List<Integer> items, List<String> lore) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
        this.enchant = enchant;
        this.lore = lore;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean isEnchant() {
        return enchant;
    }

    public int getSlot() {
        return slot;
    }

    public List<MarketItem> getMarketItems() {
        return marketItems;
    }

    public List<MarketItem> getMarketItems(boolean showExpired) {
        List<MarketItem> mi = new ArrayList<>();
        for (MarketItem marketItem : marketItems) {
            if (!marketItem.isExpired()) mi.add(marketItem);
        }
        return showExpired ? marketItems : mi;
    }

    public void addMarketItem(MarketItem marketItem, boolean addMain) {
        marketItems.add(marketItem);
        if (addMain) RaplexMarket.getInstance().addMarketItem(marketItem.getItemId(), marketItem);
    }

    public void removeMarketItem(MarketItem marketItem) {
        marketItems.remove(marketItem);
    }

    public void addMarketItem(MarketItem marketItem) {
        addMarketItem(marketItem, false);
    }

    public Material getIconMaterial() {
        if (icon.contains(":"))
            return Material.getMaterial(Integer.parseInt(icon.split(":")[0]));
        else
            return Material.getMaterial(Integer.parseInt(icon));
    }

    public short getIconItemDamage() {
        if (icon.contains(":"))
            return Short.parseShort(icon.split(":")[1]);
        else
            return 0;
    }

    public ItemStack getIcon() {
        boolean showNumber = RaplexMarket.getInstance().getConfig().getBoolean("view.show-numbers");
        int am = getMarketItems(false).size() >= 64 ? 64 : getMarketItems(false).size();
        ItemStack icon = new ItemStack(getIconMaterial(), showNumber ? am : 1, getIconItemDamage());
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(getDisplayName());
        List<String> lore = new ArrayList<>();
        getLore().forEach(l -> {
            lore.add(l.replace("{amount}", Integer.toString(getMarketItems(false).size())));
        });
        meta.setLore(lore);
        if (isEnchant()) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        icon.setItemMeta(meta);

        return icon;
    }

    public List<Material> getItems() {
        List<Material> mats = new ArrayList<>();
        items.forEach(i -> {
            mats.add(Material.getMaterial(i));
        });
        return mats;
    }
    public List<Integer> getItemsIds() {
        return items;
    }
}
