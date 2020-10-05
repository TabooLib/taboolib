package io.izzel.taboolib.cronus.bukkit;

import com.google.common.collect.Lists;
import io.izzel.taboolib.util.item.Items;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-05-23 22:45
 */
public class ItemStack {

    private final List<String> type;
    private final String name;
    private final String lore;
    private final int damage;
    private final int amount;

    public ItemStack(String type, String name, String lore, int damage, int amount) {
        this.type = type == null ? null : Lists.newArrayList(type.split("\\|"));
        this.name = name;
        this.lore = lore;
        this.damage = damage;
        this.amount = amount;
    }

    public boolean isType(org.bukkit.inventory.ItemStack itemStack) {
        return type == null || type.stream().anyMatch(e -> e.equalsIgnoreCase(itemStack.getType().name()));
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

    public boolean isSimilar(org.bukkit.inventory.ItemStack itemStack) {
        return isType(itemStack) && isName(itemStack) && isLore(itemStack) && isDamage(itemStack);
    }

    public boolean hasItem(Player player) {
        return Items.hasItem(player.getInventory(), this::isSimilar, amount);
    }

    public boolean takeItem(Player player) {
        return Items.takeItem(player.getInventory(), this::isSimilar, amount);
    }

    public List<String> getType() {
        return Lists.newArrayList(type);
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
