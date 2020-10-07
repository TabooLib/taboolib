package io.izzel.taboolib.module.nms.nbt;

import io.izzel.taboolib.util.item.Equipments;
import io.izzel.taboolib.util.item.Items;

import java.util.Objects;
import java.util.UUID;

/**
 * 物品 Attribute 属性映射类
 *
 * @Author sky
 * @Since 2019-10-22 11:38
 */
public class NBTAttribute {

    private UUID id;
    private String name;
    private String description;
    private double amount;
    private Equipments slot;
    private NBTOperation operation;

    public NBTAttribute(String name, String description, double amount, NBTOperation operation) {
        this(UUID.randomUUID(), name, description, amount, null, operation);
    }

    public NBTAttribute(String name, String description, double amount, Equipments slot, NBTOperation operation) {
        this(UUID.randomUUID(), name, description, amount, slot, operation);
    }

    public NBTAttribute(UUID id, String name, String description, double amount, NBTOperation operation) {
        this(id, name, description, amount, null, operation);
    }

    public NBTAttribute(UUID id, String name, String description, double amount, Equipments slot, NBTOperation operation) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.slot = slot;
        this.operation = operation;
    }

    public NBTCompound toNBT() {
        NBTCompound nbt = new NBTCompound();
        nbt.put("UUIDMost", new NBTBase(id.getMostSignificantBits()));
        nbt.put("UUIDLeast", new NBTBase(id.getLeastSignificantBits()));
        nbt.put("AttributeName", new NBTBase(name));
        nbt.put("Name", new NBTBase(description));
        nbt.put("Amount", new NBTBase(amount));
        nbt.put("Operation", new NBTBase(operation.ordinal()));
        if (slot != null) {
            nbt.put("Slot", new NBTBase(slot.getNMS()));
        }
        return nbt;
    }

    public static NBTAttribute fromNBT(NBTCompound nbt) {
        NBTAttribute attribute = new NBTAttribute(
                new UUID(nbt.get("UUIDMost").asLong(), nbt.get("UUIDLeast").asLong()),
                nbt.get("AttributeName").asString(),
                nbt.get("Name").asString(),
                nbt.get("Amount").asDouble(),
                NBTOperation.fromIndex(nbt.get("Operation").asInt())
        );
        if (nbt.containsKey("Slot")) {
            attribute.slot(Equipments.fromNMS(nbt.get("Slot").asString()));
        }
        return attribute;
    }

    public static NBTAttribute create() {
        return new NBTAttribute(Items.asAttribute("damage"), "TabooLib Modifiers", 0, NBTOperation.ADD_NUMBER);
    }

    public UUID getId() {
        return id;
    }

    public NBTAttribute id(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public NBTAttribute name(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public NBTAttribute description(String description) {
        this.description = description;
        return this;
    }

    public double getAmount() {
        return amount;
    }

    public NBTAttribute amount(double amount) {
        this.amount = amount;
        return this;
    }

    public Equipments getSlot() {
        return slot;
    }

    public NBTAttribute slot(Equipments slot) {
        this.slot = slot;
        return this;
    }

    public NBTOperation getOperation() {
        return operation;
    }

    public NBTAttribute operation(NBTOperation operation) {
        this.operation = operation;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NBTAttribute)) {
            return false;
        }
        NBTAttribute that = (NBTAttribute) o;
        return Double.compare(that.getAmount(), getAmount()) == 0 &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                getSlot() == that.getSlot() &&
                getOperation() == that.getOperation();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getAmount(), getSlot(), getOperation());
    }

    @Override
    public String toString() {
        return "NBTAttribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", slot=" + slot +
                ", operation=" + operation +
                '}';
    }
}
