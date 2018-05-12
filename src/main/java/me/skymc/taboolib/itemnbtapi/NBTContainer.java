package me.skymc.taboolib.itemnbtapi;

public class NBTContainer extends NBTCompound {

    private Object nbt;

    public NBTContainer() {
        super(null, null);
        nbt = NBTReflectionUtil.getNewNBTTag();
    }

    protected NBTContainer(Object nbt) {
        super(null, null);
        this.nbt = nbt;
    }

    public NBTContainer(String nbtString) throws IllegalArgumentException {
        super(null, null);
        try {
            nbt = NBTReflectionUtil.parseNBT(nbtString);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("Malformed Json: " + ex.getMessage());
        }
    }

    @Override
    protected Object getCompound() {
        return nbt;
    }

    @Override
    protected void setCompound(Object tag) {
        nbt = tag;
    }

}
