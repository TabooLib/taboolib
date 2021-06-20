# MenuBuilder
造菜单的低级轮子，节省开发时间的

## 示范
简单的一批，根本不用多讲，监听器都帮你省了。
```java
player.openInventory(new MenuBuilder(getPlugin())
        // 名称
        .title("测试界面")
        // 行数
        .rows(1)
        // 物品
        .items("####@####")
        .put('#', new ItemBuilder(Material.STAINED_GLASS_PANE).damage(15).build())
        .put('@', new ItemBuilder(Material.DIAMOND).name("&b点击获取钻石").colored().build())
        // 锁定菜单，使菜单不可以取出物品
        .lockHand()
        // 点击事件
        .click(clickEvent -> {
            if (clickEvent.getSlot() == '@') {
                clickEvent.getClicker().getInventory().addItem(new ItemStack(Material.DIAMOND));
                clickEvent.getClicker().sendMessage("§b获取钻石!");
            }
        })
        .build());
```
### 效果
![](https://i.loli.net/2021/06/17/DTg5L8HEPmvt1SR.png)