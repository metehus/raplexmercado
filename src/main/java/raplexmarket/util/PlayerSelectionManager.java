package raplexmarket.util;

import com.avaje.ebean.Page;
import org.bukkit.entity.Player;
import raplexmarket.Category;
import raplexmarket.MarketItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerSelectionManager {
    private HashMap<Player, Integer> userPages = new HashMap<>();
    private HashMap<Player, Category> userCategorys = new HashMap<>();
    private HashMap<Player, MarketItem> userConfirm = new HashMap<>();
    private HashMap<Player, Long> userPrices = new HashMap<>();
    private List<Player> closingGUI = new ArrayList<>();

    public PlayerSelectionManager() {
    }

    public void addClosingGUI(Player player) {
        closingGUI.add(player);
    }

    public boolean isClosingGUI(Player player) {
        return closingGUI.contains(player);
    }

    public void removeClosingGUI(Player player) {
        closingGUI.remove(player);
    }

    public int getPlayerPage(Player player) {
        return userPages.get(player);
    }

    public void setPlayerPage(Player player, int page) {
        userPages.put(player, page);
    }

    public Category getPlayerCategory(Player player) {
        return userCategorys.get(player);
    }

    public void setPlayerCategory(Player player, Category category) {
        userCategorys.put(player, category);
    }

    public void setPlayerConfirm(Player player, MarketItem marketItem) {
        userConfirm.put(player, marketItem);
    }

    public MarketItem getPlayerConfirm(Player player) {
        return userConfirm.get(player);
    }

    public void setPlayerPrice(Player player, long price) {
        userPrices.put(player, price);
    }

    public long getPlayerPrice(Player player) {
        return userPrices.get(player);
    }

    public void clearPlayer(Player player) {
        userPages.remove(player);
        userCategorys.remove(player);
        userConfirm.remove(player);
        userPrices.remove(player);
    }
}
