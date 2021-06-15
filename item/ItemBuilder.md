# ItemBuilder
造物品的低级轮子，节省开发时间的

## 示范
简单的一批，根本不用多讲，这个轮子可以设置绝大多数物品数据。 
```java
public ItemStack getItem() {
    // 要这样写才好看
    return new ItemBuilder(Material.STONE)
        .name("名字")
        .lore("描述1", "描述2")
        .build();
}
```

## 创建
```java
// 一个参数的（材质）
new ItemBuilder(Material.STONE);
// 两个参数的（材质，数量）
new ItemBuilder(Material.STONE, 1);
// 三个参数的（材质，数量，损伤值）
new ItemBuilder(Material.STONE, 1, 0);
```

## 方法
| 方法 | 作用 | 目标 |
| --- | --- | --- | 
| material | 更改材质 | ~ |
| amount | 更改数量 | ~ |
| damage | 更改损伤值 | ~ |
| name | 更改名称 | ~ |
| lore | 更改描述 | ~ |
| flags | 添加标记 | ~ |
| enchant | 添加附魔 | ~ |
| color | 添加颜色 | 皮革 |
| banner | 添加图案 | 旗帜 |
| potionData | 更改基础效果 | 药水 |
| potionColor | 更改药水颜色 | 药水 |
| potionEffect | 添加效果 | 药水 |
| effType | 更改生物类型 | 怪物蛋 |
| skullOwner | 更改头颅皮肤 | 头颅 |
| unbreakable | 无法破坏 | ~ |
| colored | 转换名称和描述的颜色 | ~ |
| shiny | 添加附魔并隐藏 | ~ |
