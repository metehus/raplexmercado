package raplexmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import raplexmarket.commands.MarketCommand;
import raplexmarket.listeners.BlockPlace;
import raplexmarket.listeners.InventoryClick;
import raplexmarket.listeners.InventoryClose;
import raplexmarket.util.ConfigUtils;
import raplexmarket.util.ItemSerialization;
import raplexmarket.util.PlayerSelectionManager;

import java.io.*;
import java.util.*;

public class RaplexMarket extends JavaPlugin {

    private static RaplexMarket instance;
    private Economy economy;
    private File database;
    private HashMap<UUID, MarketItem> marketItems = new HashMap<>();
    private HashMap<UUID, MarketChest> chests = new HashMap<>();
    private HashMap<String, Category> categories = new HashMap<>();
    private ConfigUtils configUtils;
    private PlayerSelectionManager playerSelectionManager = new PlayerSelectionManager();
    public static final String guiC = ChatColor.translateAlternateColorCodes('&', "&d&1");


    @Override
    public void onEnable() {
        instance = this;
        configUtils = new ConfigUtils(this);

        if (!setupEconomy()) {
            getLogger().severe(ChatColor.DARK_RED + "Failed to setup economy.");
            getServer().getPluginManager().disablePlugin(this);
        }
        if (!setupCategories()) {
            getLogger().severe(ChatColor.DARK_RED + "Failed to setup categories.");
        }
        if (!setupDatabase()) {
            getLogger().severe(ChatColor.DARK_RED + "Failed to setup database.");
        }

        Bukkit.getPluginManager().registerEvents(new InventoryClick(this), this);
        //Bukkit.getPluginManager().registerEvents(new BlockPlace(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClose(this), this);

        getCommand("market").setExecutor(new MarketCommand(this));
    }

    @Override
    public void onDisable() {
        JSONObject file = new JSONObject();
        JSONArray bids = new JSONArray();
        JSONArray chestsArray = new JSONArray();
        for (MarketItem item : marketItems.values()) {
            item.setExpired(true);
            JSONObject itemObj = new JSONObject();
            itemObj.put("id", item.getItemId().toString());
            itemObj.put("from", item.getOwner().getUniqueId().toString());
            itemObj.put("price", item.getPrice());
            itemObj.put("started", item.getStarted().getTime());
            itemObj.put("expired", item.isExpired());
            itemObj.put("category", item.getCategory().getName());
            JSONArray items = new JSONArray();
            for (ItemStack i : item.getItems()) {
                items.add(ItemSerialization.serializeItem(i));
            }
            itemObj.put("items", items);
            bids.add(itemObj);
        }

//        getChests().forEach(((uuid, c) -> {
//            JSONObject chest = new JSONObject();
//            chest.put("id", uuid.toString());
//            JSONArray items = new JSONArray();
//            for (ItemStack i : c.getItems()) {
//                items.add(ItemSerialization.serializeItem(i));
//            }
//            chest.put("items", items);
//            chestsArray.add(chest);
//        }));

        file.put("bids", bids);
        file.put("chests", chestsArray);
        try {
            FileWriter fw = new FileWriter(database);
            fw.write(file.toJSONString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            getLogger().severe(ChatColor.DARK_RED + "Failed to save database");
            e.printStackTrace();
        }
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }

        return this.economy != null;
    }

    public boolean setupDatabase() {
        database = new File(getDataFolder(), "database.json");

        try {
            if (!database.exists()) saveResource(database.getName(), false);

            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(new FileReader(database));
            JSONObject jsonObject = (JSONObject) parsed;
            JSONArray bids = (JSONArray) jsonObject.get("bids");

            bids.forEach(b -> {
                try {
                    JSONObject bid = (JSONObject) b;
                    UUID id = UUID.fromString((String) bid.get("id"));
                    UUID player = UUID.fromString((String) bid.get("from"));
                    long price = (long) bid.get("price");
                    long date = (long) bid.get("started");
                    boolean expired = (boolean) bid.get("expired");
                    List<ItemStack> itemStacks = new ArrayList<>();
                    JSONArray items = (JSONArray) bid.get("items");
                    items.forEach(i -> {
                        itemStacks.add(ItemSerialization.deserializeItem((String) i));
                    });
                    Category cat = categories.get((String) bid.get("category"));

                    if (getConfig().getBoolean("debug-log"))
                        getLogger().info("Setted item " + id);

                    MarketItem mi = new MarketItem(player, ((Long) price).intValue(), date, expired, cat, itemStacks, id);
                    cat.addMarketItem(mi);
                    marketItems.put(id, mi);
                } catch (Exception e) {
                    getLogger().severe("Market database error: " + e);
                }
            });

            JSONArray chestsOB = (JSONArray) jsonObject.get("chests");

            chestsOB.forEach(c -> {
                JSONObject ches = (JSONObject) c;
                UUID id = UUID.fromString((String) ches.get("id"));
                List<ItemStack> itemStacks = new ArrayList<>();
                JSONArray items = (JSONArray) ches.get("items");
                items.forEach(i -> {
                    itemStacks.add(ItemSerialization.deserializeItem((String) i));
                });

                MarketChest chest = new MarketChest(id, itemStacks);
                addChest(chest);
            });

            getLogger().info("Database items successfully setted up.");

            return true;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setupCategories() {
        try {
            ConfigurationSection cats = getConfig().getConfigurationSection("categories");
            for (String cat : cats.getKeys(false)) {
                try {
                    String path = "categories." + cat + ".";
                    int slot = getConfig().getInt(path + "slot");
                    String icon = getConfig().getString(path + "icon");
                    String name = getConfigUtils().getLang(path + "name");
                    List<Integer> items = getConfig().getIntegerList(path + "items");
                    boolean enchant = getConfig().getBoolean(path + "enchant");
                    List<String> lore = getConfigUtils().getFormattedList(path + "lore");

                    categories.put(cat, new Category(cat, name, icon, slot, enchant, items, lore));
                } catch (Exception e) {
                    getLogger().severe("Error registering category " + cat);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public File getDatabaseFile() {
        return database;
    }

    public HashMap<UUID, MarketItem> getMarketItems() {
        return marketItems;
    }

    public List<MarketItem> getMarketItemsList() {
        List<MarketItem> list = new ArrayList(getMarketItems().values());
        return list;
    }

    public MarketItem getMarketItem(UUID id) {
        return marketItems.get(id);
    }

    public void addMarketItem(UUID id, MarketItem marketItem) {
        marketItems.put(id, marketItem);
    }

    public void removeMarketItem(MarketItem marketItem) {
        marketItems.remove(marketItem.getItemId());
    }

    public PlayerSelectionManager getPlayerSelectionManager() {
        return playerSelectionManager;
    }

    public HashMap<String, Category> getCategories() {
        return categories;
    }

    public HashMap<UUID, MarketChest> getChests() {
        return chests;
    }

    public void addChest(MarketChest chest) {
        chests.put(chest.getUUID(), chest);
    }

    public void removeChest(UUID uuid) {
        chests.remove(uuid);
    }

    public static RaplexMarket getInstance() {
        return instance;
    }

    public ConfigUtils getConfigUtils() {
        return configUtils;
    }

    public Economy getEconomy() {
        return economy;
    }
}