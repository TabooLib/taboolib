# MenuBuilder
造菜单的低级轮子，节省开发时间的

## 示范
简单的一批，根本不用多讲，监听器都帮你省了。
```java
player.openInventory(new MenuBuilder()
    // 名称
    .name("测试界面")
    // 行数
    .rows(1)
    // 物品（省略触发器）
    .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, 15).build(), 0, 1, 2, 3, 5, 6, 7, 8)
    // 物品
    .item(new ItemBuilder(Material.DIAMOND).name("&b点击获取钻石").colored().build(), event -> {
        event.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND));
        event.getPlayer().sendMessage("§b获取钻石!");
    }, 4).build());
```
