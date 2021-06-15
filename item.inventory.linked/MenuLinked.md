# MenuLinked

高效创建多页菜单

## 创建

```java
public class Icon {
}
```
```java
public class TestMenu extends MenuLinked<Icon> {
    public TestMenu(@NotNull Player player) {
        super(player);
    }

    @Override
    public List<Icon> getElements() {
        
    }

    @Override
    public List<Integer> getSlots() {
        
    }

    @Override
    public void onBuild(@NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull ClickEvent clickEvent, @NotNull Icon icon) {

    }

    @Override
    public @Nullable ItemStack generateItem(@NotNull Player player, @NotNull Icon icon, int i, int i1) {
        
    }
}
```

你可能会感到很诧异，为什么创建一个菜单需要两个类呢，往下看就知道了

## 实例: 玩家选择菜单
```kotlin
class PlayerMenu(player: Player) : MenuLinked<Icon>(player) {
    override fun getElements(): MutableList<Icon> {
        return Bukkit.getOnlinePlayers().map { Icon(it.name) }.toMutableList()
    }

    override fun getSlots(): MutableList<Int> {
        return mutableListOf(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 41, 43, 44)
    }

    override fun getRows(): Int {
        return 6
    }

    override fun onBuild(inv: Inventory) {
        // 添加边框
        for (slot in listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 47, 48, 49, 50, 51, 53)) {
            inv.setItem(slot, ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).name(" ").colored().build())
        }

        // 添加上一页按钮
        if (hasPreviousPage()) {
            inv.setItem(46, ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&f上一页").colored().build())
        } else {
            inv.setItem(46, ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&8上一页").colored().build())
        }
        
        // 添加下一页按钮
        if (hasNextPage()) {
            inv.setItem(52, ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&f下一页").colored().build())
        } else {
            inv.setItem(52, ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&8下一页").colored().build())
        }
    }

    // 点击到元素时
    override fun onClick(clickEvent: ClickEvent, icon: Icon) {
        clickEvent.clicker.sendMessage("你选择了" + icon.name)
    }

    // 生成每个元素到菜单时调用
    override fun generateItem(player: Player, icon: Icon, index: Int, slot: Int): ItemStack? {
        return ItemBuilder(Material.PLAYER_HEAD)
            .skullOwner(icon.name)
            .name(icon.name)
            .lore("", "点击选择!", "")
            .build()
    }

    companion object {
        // 打开菜单
        fun open(player: Player) {
            PlayerMenu(player).open()
        }
    }
}
```

```kotlin
// 其实没有这个Icon, 直接用Player填上泛型都行
class Icon(val name: String)
```
> 注意: 不要忘了覆写getSlots()，不然你的菜单只有一行。