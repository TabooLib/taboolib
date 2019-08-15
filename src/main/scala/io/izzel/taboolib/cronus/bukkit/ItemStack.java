package io.izzel.taboolib.cronus.bukkit;

import io.izzel.taboolib.util.item.Items;
import org.bukkit.entity.Player;

/**
 * @Author 坏黑
 * @Since 2019-05-23 22:45
 */
public class ItemStack {

    private String type;
    private String name;
    private String lore;
    private int damage;
    private int amount;

    public ItemStack(String type, String name, String lore, int damage, int amount) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.damage = damage;
        this.amount = amount;
    }

    public boolean isType(org.bukkit.inventory.ItemStack itemStack) {
        return type == null || itemStack.getType().name().equalsIgnoreCase(type);
    }

    public boolean isName(org.bukkit.inventory.ItemStack itemStack) {
        return name == null || Items.getName(itemStack).contains(name);
    }

    public boolean isLore(org.bukkit.inventory.ItemStack itemStack) {
        return lore == null || Items.hasLore(itemStack, lore);
    }

    public boolean isDamage(org.bukkit.inventory.ItemStack itemStack) {
        return damage == -1 || itemStack.getDurability() == damage;
    }

    public boolean isAmount(org.bukkit.inventory.ItemStack itemStack) {
        return itemStack.getAmount() >= amount;
    }

    public boolean isItem(org.bukkit.inventory.ItemStack itemStack) {
        return isType(itemStack) && isName(itemStack) && isLore(itemStack) && isDamage(itemStack) && isAmount(itemStack);
    }

    public boolean hasItem(Player player) {
        return Items.hasItem(player.getInventory(), this::isItem, amount);
    }

    public boolean takeItem(Player player) {
        return Items.takeItem(player.getInventory(), this::isItem, amount);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getLore() {
        return lore;
    }

    public int getAmount() {
        return amount;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return "ItemStack{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", lore='" + lore + '\'' +
                ", damage=" + damage +
                ", amount=" + amount +
                '}';
    }
}
