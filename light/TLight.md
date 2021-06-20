# TLight
快速创建光源

## 使用
```java
// 创建一个强度为15， 光照类型为方块的光源
TLight.create(block, Type.BLOCK, 15);

// 删除此光源
TLight.delete(block, Type.BLOCK);
```

!> 使用TLight#createLight()创建光源是不会被更新的，需要手动更新。