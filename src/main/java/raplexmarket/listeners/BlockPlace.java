package raplexmarket.listeners;

import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import raplexmarket.MarketChest;
import raplexmarket.RaplexMarket;
import raplexmarket.util.ConfigUtils;
import raplexmarket.util.PlayerSelectionManager;

import java.util.UUID;

public class BlockPlace implements Listener {
    private RaplexMarket plugin;
    private ConfigUtils configUtils;
    private FileConfiguration config;

    public BlockPlace(RaplexMarket plugin) {
        this.plugin = plugin;
        this.configUtils = plugin.getConfigUtils();
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        net.minecraft.server.v1_8_R1.ItemStack itemnms = CraftItemStack.asNMSCopy(event.getItemInHand());
        NBTTagCompound tag;
        if (itemnms.hasTag()) tag = itemnms.getTag();
        else tag = new NBTTagCompound();
        String id = tag.getString("id");
        if (id == null) return;
        UUID uuid = UUID.fromString(id);

        MarketChest marketChest = plugin.getChests().get(uuid);

        if (marketChest == null) return;

        Chest chest = (Chest) event.getBlock().getState();

        marketChest.getItems().forEach(i -> {
            chest.getBlockInventory().addItem(i);
        });

        plugin.removeChest(uuid);
    }
}
