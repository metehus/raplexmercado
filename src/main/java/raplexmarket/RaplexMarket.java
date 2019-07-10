package raplexmarket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import raplexmarket.listeners.InventoryClick;
import raplexmarket.util.ConfigUtils;
import raplexmarket.util.ItemSerialization;
import raplexmarket.util.PageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RaplexMarket extends JavaPlugin {

    private static RaplexMarket instance;
    private Economy economy;
    private File database;
    private List<MarketItem> marketItems = new ArrayList<>();
    private HashMap<String, Category> categories = new HashMap<>();
    private ConfigUtils configUtils;
    private PageManager pageManager = new PageManager();
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

        getCommand("market").setExecutor(new MarketCommand(this));
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
                    UUID id = UUID.fromString((String) bid.get("from"));
                    long price = (long) bid.get("price");
                    long date = (long) bid.get("started");
                    List<ItemStack> itemStacks = new ArrayList<>();
                    JSONArray items = (JSONArray) bid.get("items");
                    items.forEach(i -> {
                        itemStacks.add(ItemSerialization.deserializeItem((JSONObject) i));
                    });
                    Category cat = categories.get((String) bid.get("category"));

                    MarketItem mi = new MarketItem(id, ((Long) price).intValue(), date, cat, itemStacks);
                    cat.addMarketItem(mi);
                    marketItems.add(mi);
                } catch (Exception e) {
                    getLogger().severe("Market database error: " + e);
                }
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

    public List<MarketItem> getMarketItems() {
        return marketItems;
    }

    public PageManager getPageManager() {
        return pageManager;
    }

    public HashMap<String, Category> getCategories() {
        return categories;
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