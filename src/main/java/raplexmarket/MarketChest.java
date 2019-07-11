package raplexmarket;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class MarketChest {
    private UUID id;
    private List<ItemStack> items;

    public MarketChest(UUID uuid, List<ItemStack> items) {
        this.id = uuid;
        this.items = items;
    }

    public void addItem(ItemStack item) {
        items.add(item);
    }

    public UUID getUUID() {
        return id;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
