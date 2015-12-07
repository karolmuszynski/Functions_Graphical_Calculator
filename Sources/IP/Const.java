package individual;

import javafx.scene.paint.Color;

public class Const {
    public static final double brakeLineConst = 0.9;

    public static final String solidLine = "solid";
    public static final String dottedLine = "dotted";
    public static final String dashedLine = "dashed";

    public static final Color[] colors =
            {Color.ROYALBLUE, Color.LIMEGREEN, Color.DEEPPINK, Color.AQUAMARINE, Color.GOLD, Color.RED,
                    Color.GREEN, Color.ORANGE, Color.HOTPINK, Color.GREY, Color.MEDIUMORCHID, Color.LIGHTCYAN,
                    Color.TOMATO, Color.BLACK};

    public static int count = 0;

    public static Color getNextColor() {
        if(count == colors.length - 1) count = 0;
        return colors[count++];
    }
}
