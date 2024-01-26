package taboolib.platform.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TabooLib
 * taboolib.platform.util.Slots
 *
 * @author 坏黑
 * @since 2022/8/6 21:01
 */
public class Slots {

    public static final List<Integer> CENTER = of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );

    public static final List<Integer> BORDER = of(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17,
            18, 26,
            27, 35,
            36, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
    );

    public static final List<Integer> LINE_1 = of(0, 1, 2, 3, 4, 5, 6, 7, 8);

    public static final List<Integer> LINE_2 = of(9, 10, 11, 12, 13, 14, 15, 16, 17);

    public static final List<Integer> LINE_3 = of(18, 19, 20, 21, 22, 23, 24, 25, 26);

    public static final List<Integer> LINE_4 = of(27, 28, 29, 30, 31, 32, 33, 34, 35);

    public static final List<Integer> LINE_5 = of(36, 37, 38, 39, 40, 41, 42, 43, 44);

    public static final List<Integer> LINE_6 = of(45, 46, 47, 48, 49, 50, 51, 52, 53);

    public static final Integer LINE_1_LEFT = 0;

    public static final Integer LINE_1_MIDDLE = 4;

    public static final Integer LINE_1_RIGHT = 8;

    public static final Integer LINE_2_LEFT = 9;

    public static final Integer LINE_2_MIDDLE = 13;

    public static final Integer LINE_2_RIGHT = 17;

    public static final Integer LINE_3_LEFT = 18;

    public static final Integer LINE_3_MIDDLE = 22;

    public static final Integer LINE_3_RIGHT = 26;

    public static final Integer LINE_4_LEFT = 27;

    public static final Integer LINE_4_MIDDLE = 31;

    public static final Integer LINE_4_RIGHT = 35;

    public static final Integer LINE_5_LEFT = 36;

    public static final Integer LINE_5_MIDDLE = 40;

    public static final Integer LINE_5_RIGHT = 44;

    public static final Integer LINE_6_LEFT = 45;

    public static final Integer LINE_6_MIDDLE = 49;

    public static final Integer LINE_6_RIGHT = 53;

    private static List<Integer> of(int... slots) {
        return Arrays.stream(slots).boxed().collect(Collectors.toList());
    }
}
