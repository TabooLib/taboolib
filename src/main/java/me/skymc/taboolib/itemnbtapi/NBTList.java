package me.skymc.taboolib.itemnbtapi;

import java.lang.reflect.Method;

import me.skymc.taboolib.itemnbtapi.utils.MethodNames;
import me.skymc.taboolib.message.MsgUtils;

public class NBTList {

    private String listName;
    private NBTCompound parent;
    private NBTType type;
    private Object listObject;

    protected NBTList(NBTCompound owner, String name, NBTType type, Object list) {
        parent = owner;
        listName = name;
        this.type = type;
        this.listObject = list;
        if (!(type == NBTType.NBTTagString || type == NBTType.NBTTagCompound)) {
            System.err.println("List types != String/Compound are currently not implemented!");
        }
    }

    protected void save() {
        parent.set(listName, listObject);
    }

    public NBTListCompound addCompound() {
        if (type != NBTType.NBTTagCompound) {
            new Throwable("Using Compound method on a non Compound list!").printStackTrace();
            return null;
        }
        try {
            Method method = listObject.getClass().getMethod("add", NBTReflectionUtil.getNBTBase());
            Object compound = NBTReflectionUtil.getNBTTagCompound().newInstance();
            method.invoke(listObject, compound);
            return new NBTListCompound(this, compound);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public NBTListCompound getCompound(int id) {
        if (type != NBTType.NBTTagCompound) {
            new Throwable("Using Compound method on a non Compound list!").printStackTrace();
            return null;
        }
        try {
            Method method = listObject.getClass().getMethod("get", int.class);
            Object compound = method.invoke(listObject, id);
            return new NBTListCompound(this, compound);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public String getString(int i) {
        if (type != NBTType.NBTTagString) {
            new Throwable("Using String method on a non String list!").printStackTrace();
            return null;
        }
        try {
            Method method = listObject.getClass().getMethod("getString", int.class);
            return (String) method.invoke(listObject, i);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void addString(String s) {
        if (type != NBTType.NBTTagString) {
            new Throwable("Using String method on a non String list!").printStackTrace();
            return;
        }
        try {
            Method method = listObject.getClass().getMethod("add", NBTReflectionUtil.getNBTBase());
            method.invoke(listObject, NBTReflectionUtil.getNBTTagString().getConstructor(String.class).newInstance(s));
            save();
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void setString(int i, String s) {
        if (type != NBTType.NBTTagString) {
            new Throwable("Using String method on a non String list!").printStackTrace();
            return;
        }
        try {
            Method method = listObject.getClass().getMethod("a", int.class, NBTReflectionUtil.getNBTBase());
            method.invoke(listObject, i, NBTReflectionUtil.getNBTTagString().getConstructor(String.class).newInstance(s));
            save();
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public void remove(int i) {
        try {
            Method method = listObject.getClass().getMethod(MethodNames.getRemoveMethodName(), int.class);
            method.invoke(listObject, i);
            save();
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public int size() {
        try {
            Method method = listObject.getClass().getMethod("size");
            return (int) method.invoke(listObject);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return -1;
    }

    public NBTType getType() {
        return type;
    }

}
