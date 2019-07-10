package raplexmarket.util;

import com.avaje.ebean.Page;
import org.bukkit.entity.Player;
import raplexmarket.Category;

import java.util.HashMap;

public class PageManager {
    private HashMap<Player, Integer> userPages = new HashMap<>();
    private HashMap<Player, Category> userCategorys = new HashMap<>();

    public PageManager() {
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

    public void clearPlayer(Player player) {
        userPages.remove(player);
        userCategorys.remove(player);
    }
}
