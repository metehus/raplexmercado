package raplexmarket.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ItemSerialization {
    public static JSONObject serializeItem(ItemStack item) {
        JSONObject jsonObject = new JSONObject();
        Map<String, Object> map = item.serialize();
        map.forEach((k, v) -> {
            if (k.equals("meta"))
                jsonObject.put(k, item.getItemMeta().serialize());
            else
                jsonObject.put(k, v);
        });

        return jsonObject;
    }

    public static ItemStack deserializeItem(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();

        map.put("type", jsonObject.get("type"));
        if (jsonObject.containsKey("damage"))
            map.put("damage", jsonObject.get("damage"));
        if (jsonObject.containsKey("amount"))
            map.put("amount", ((Long)jsonObject.get("amount")).intValue());
        ItemStack item = ItemStack.deserialize(map);

        if (jsonObject.containsKey("meta")) {
            JSONObject metaObj = (JSONObject) jsonObject.get("meta");
            ItemMeta meta = item.getItemMeta();
            if (metaObj.containsKey("display-name"))
                meta.setDisplayName((String) metaObj.get("display-name"));
            if (metaObj.containsKey("lore"))
                meta.setLore((JSONArray) metaObj.get("lore"));
            if (metaObj.containsKey("enchants")) {
                JSONObject enchants = (JSONObject) metaObj.get("enchants");
                enchants.keySet().forEach(e -> {
                    long level = (long) enchants.get(e);
                    meta.addEnchant(Enchantment.getByName((String) e), ((Long) level).intValue(), true);
                });
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
