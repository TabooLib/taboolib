# NBT
快速操作物品NBT。
> 参见: [NMS.java](nms/NMS.md)

## 使用
你需要借助[NMS.java](nms/NMS.md)来操作物品NBT。

### 获取物品NBT
使用NMS#loadNBT()
```java
NBTCompound compound = NMS.handle().loadNBT(item);
```

### 设置物品NBT
`NBTCompound`实现了`Map<java.lang.String,NBTBase>`，所以你可以像操作`Map`一样操作`NBTCompound`。
`NBTBase`就是`NBTTag`及其子类的包装。
```java
NBTCompound compound = NMS.handle().loadNBT(item);
compound.put("TabooLib", new NBTBase("TabooLib"));
```

## 实例: 将物品设置为"不可破坏"
```java
public class TestNBT {
    @TInject
    static CommandBuilder command = CommandBuilder.create("testnbt", TestPlugin.getInstance().getPlugin())
            .execute((commandSender, strings) -> {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    NBTCompound compound = NMS.handle().loadNBT(itemStack);
                    // 设置NBT数据
                    compound.put("Unbreakable", new NBTBase(1));
                    // 将NBT数据保存到物品里
                    ItemStack item = NMS.handle().saveNBT(itemStack, compound);
                    player.getInventory().addItem(item);
                }
            });
}
```

### 效果
![](https://i.loli.net/2021/06/20/rUlcfnbsvdp8aIC.png)