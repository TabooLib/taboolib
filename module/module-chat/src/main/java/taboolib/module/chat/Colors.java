package taboolib.module.chat;

import net.md_5.bungee.api.ChatColor;

import java.util.Optional;

/**
 * @author sky
 * @since 2021/1/18 2:02 下午
 */
public enum Colors {

    LIGHT_PINK("浅粉红", "FFB6C1"),

    PINK("粉红", "FFC0CB"),

    CRIMSON("猩红", "DC143C"),

    LAVENDER_BLUSH("脸红的淡紫", "FFF0F5"),

    PALE_VIOLET_RED("苍白的紫罗兰红", "DB7093"),

    HOT_PINK("热情的粉红", "FF69B4"),

    DEEP_PINK("深粉", "FF1493"),

    MEDIUM_VIOLET_RED("适中的紫罗兰红", "C71585"),

    ORCHID("兰花的紫", "DA70D6"),

    THISTLE("蓟", "D8BFD8"),

    LUM("李子", "DDA0DD"),

    VIOLET("紫罗兰", "EE82EE"),

    MAGENTA("洋红", "FF00FF"),

    FUCHSIA("灯笼海棠", "FF00FF"),

    DARK_MAGENTA("深洋红", "8B008B"),

    PURPLE("紫", "800080"),

    MEDIUM_ORCHID("适中的兰花紫", "BA55D3"),

    DARK_VOILET("深紫罗兰", "9400D3"),

    DARK_ORCHID("深兰花紫", "9932CC"),

    INDIGO("靛青", "4B0082"),

    BLUE_VIOLET("深紫罗兰的蓝", "8A2BE2"),

    MEDIUM_PURPLE("适中的紫", "9370DB"),

    MEDIUM_SLATE_BLUE("适中的板岩暗蓝灰", "7B68EE"),

    SLATE_BLUE("板岩暗蓝灰", "6A5ACD"),

    DARK_SLATE_BLUE("深岩暗蓝灰", "483D8B"),

    LAVENDER("熏衣草花的淡紫", "E6E6FA"),

    GHOST_WHITE("幽灵的白", "F8F8FF"),

    BLUE("纯蓝", "0000FF"),

    MEDIUM_BLUE("适中的蓝", "0000CD"),

    MIDNIGHT_BLUE("午夜的蓝", "191970"),

    DARK_BLUE("深蓝", "00008B"),

    NAVY("海军蓝", "000080"),

    ROYAL_BLUE("皇军蓝", "4169E1"),

    CORNFLOWER_BLUE("矢车菊的蓝", "6495ED"),

    LIGHT_STEEL_BLUE("淡钢蓝", "B0C4DE"),

    LIGHT_SLATE_GRAY("浅石板灰", "778899"),

    SLATE_GRAY("石板灰", "708090"),

    DODER_BLUE("道奇蓝", "1E90FF"),

    ALICE_BLUE("爱丽丝蓝", "F0F8FF"),

    STEEL_BLUE("钢蓝", "4682B4"),

    LIGHT_SKY_BLUE("淡蓝", "87CEFA"),

    SKY_BLUE("天蓝", "87CEEB"),

    DEEP_SKY_BLUE("深天蓝", "00BFFF"),

    LIGHT_B_LUE("淡蓝", "ADD8E6"),

    POW_DER_BLUE("火药蓝", "B0E0E6"),

    CADET_BLUE("军校蓝", "5F9EA0"),

    AZURE("蔚蓝", "F0FFFF"),

    LIGHT_CYAN("淡青", "E1FFFF"),

    PALE_TURQUOISE("苍白的绿宝石", "AFEEEE"),

    CYAN("青", "00FFFF"),

    AQUA("水绿", "00FFFF"),

    DARK_TURQUOISE("深绿宝石", "00CED1"),

    DARK_SLATE_GRAY("深石板灰", "2F4F4F"),

    DARK_CYAN("深青", "008B8B"),

    TEAL("水鸭", "008080"),

    MEDIUM_TURQUOISE("适中的绿宝石", "48D1CC"),

    LIGHT_SEA_GREEN("浅海洋绿", "20B2AA"),

    TURQUOISE("绿宝石", "40E0D0"),

    AUQAMARIN("碧绿", "7FFFAA"),

    MEDIUM_AQUAMARINE("适中的碧绿", "00FA9A"),

    MEDIUM_SPRING_GREEN("适中的春天的绿", "F5FFFA"),

    MINT_CREAM("薄荷奶油", "00FF7F"),

    SPRING_GREEN("春天的绿", "3CB371"),

    SEA_GREEN("海洋绿", "2E8B57"),

    HONEYDEW("蜂蜜", "F0FFF0"),

    LIGHT_GREEN("淡绿", "90EE90"),

    PALE_GREEN("苍白的绿", "98FB98"),

    DARK_SEA_GREEN("深海洋绿", "8FBC8F"),

    LIME_GREEN("酸橙绿", "32CD32"),

    LIME("酸橙", "00FF00"),

    FOREST_GREEN("森林绿", "228B22"),

    GREEN("纯绿", "008000"),

    DARK_GREEN("深绿", "006400"),

    CHARTREUSE("查特酒绿", "7FFF00"),

    LAWN_GREEN("草坪绿", "7CFC00"),

    GREEN_YELLOW("绿黄", "ADFF2F"),

    OLIVE_DRAB("橄榄土褐", "556B2F"),

    BEIGE("米", "6B8E23"),

    LIGHT_GOLDENROD_YELLOW("浅秋麒麟黄", "FAFAD2"),

    IVORY("象牙", "FFFFF0"),

    LIGHT_YELLOW("浅黄", "FFFFE0"),

    YELLOW("纯黄", "FFFF00"),

    OLIVE("橄榄", "808000"),

    DARK_KHAKI("深卡其布", "BDB76B"),

    LEMON_CHIFFON("柠檬薄纱", "FFFACD"),

    PALE_GODENROD("灰秋麒麟", "EEE8AA"),

    KHAKI("卡其布", "F0E68C"),

    GOLD("金", "FFD700"),

    CORNISLK("玉米", "FFF8DC"),

    GOLD_ENROD("秋麒麟", "DAA520"),

    FLORAL_WHITE("花的白", "FFFAF0"),

    OLD_LACE("老饰带", "FDF5E6"),

    WHEAT("小麦", "F5DEB3"),

    MOCCASIN("鹿皮鞋", "FFE4B5"),

    ORANGE("橙", "FFA500"),

    PAPAYA_WHIP("番木瓜", "FFEFD5"),

    BLANCHED_ALMOND("漂白的杏仁", "FFEBCD"),

    NAVAJO_WHITE("Navajo白", "FFDEAD"),

    ANTIQUE_WHITE("古代的白", "FAEBD7"),

    TAN("晒黑", "D2B48C"),

    BRULY_WOOD("结实的树", "DEB887"),

    BISQUE("BISQUE", "FFE4C4"),

    DARK_ORANGE("深橙", "FF8C00"),

    LINEN("亚麻布", "FAF0E6"),

    PERU("秘鲁", "CD853F"),

    PEACH_PUFF("桃", "FFDAB9"),

    SANDY_BROWN("沙棕", "F4A460"),

    CHOCOLATE("巧克力", "D2691E"),

    SADDLE_BROWN("马鞍棕", "8B4513"),

    SEA_SHELL("海贝壳", "FFF5EE"),

    SIENNA("黄土赭", "A0522D"),

    LIGHT_SALMON("浅鲜肉", "FFA07A"),

    CORAL("珊瑚", "FF7F50"),

    ORANGE_RED("橙红", "FF4500"),

    DARK_SALMON("深鲜肉", "E9967A"),

    TOMATO("番茄", "FF6347"),

    MISTY_ROSE("薄雾玫瑰", "FFE4E1"),

    SALMON("鲜肉", "FA8072"),

    SNOW("雪", "FFFAFA"),

    LIGHT_CORAL("淡珊瑚", "F08080"),

    ROSY_BROWN("玫瑰棕", "BC8F8F"),

    INDIAN_RED("印度红", "CD5C5C"),

    RED("纯红", "FF0000"),

    BROWN("棕", "A52A2A"),

    FIRE_BRICK("耐火砖", "B22222"),

    DARK_RED("深红", "8B0000"),

    MAROON("栗", "800000"),

    WHITE("纯白", "FFFFFF"),

    WHITE_SMOKE("白烟", "F5F5F5"),

    GAINSBORO("Gainsboro", "DCDCDC"),

    LIGHT_GREY("浅灰", "D3D3D3"),

    SILVER("银白", "C0C0C0"),

    DARK_GRAY("深灰", "A9A9A9"),

    GRAY("灰", "808080"),

    DIM_GRAY("暗淡的灰", "696969"),

    BLACK("纯黑", "000000");

    final String display;
    final String hexCode;

    Colors(String display, String hexCode) {
        this.display = display;
        this.hexCode = hexCode;
    }

    public String getDisplay() {
        return display;
    }

    public String getHexCode() {
        return hexCode;
    }

    public ChatColor toChatColor() {
        return ChatColor.of("#" + hexCode);
    }

    public static Optional<Colors> matchKnownColor(String in) {
        for (Colors knownColor : values()) {
            if (knownColor.name().equalsIgnoreCase(in) || knownColor.display.equalsIgnoreCase(in)) {
                return Optional.of(knownColor);
            }
        }
        return Optional.empty();
    }
}