package taboolib.common.util

object ReferLocation {

    /**
     * 源位置参照制坐标
     *
     * 偏移量 offset
     * -90 源位置的左边
     * 90  源位置的右边
     * 180 源位置的后面
     * 想要定于前面，可使用负数乘法
     *
     * @param sourceLoc 源位置
     * @param offset 偏移量
     * @param multiply 乘 越大越远之类
     * @param height 高度
     * @return
     */
    fun returnLoc(sourceLoc: Location, offset: Float, multiply: Double, height: Double): Location {
        val referLoc = sourceLoc.clone()
        referLoc.yaw = sourceLoc.yaw + offset
        val vectorAdd = referLoc.direction.normalize().multiply(multiply)
        val getLoc = referLoc.add(vectorAdd)
        getLoc.add(0.0, height, 0.0)
        return getLoc
    }

}