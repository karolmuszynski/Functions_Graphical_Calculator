package individual;

import javafx.scene.paint.Color;

public class Function {
    public int id;
    public String name;
    public String formula;
    public Color lineColor;
    public String lineStyle;
    public String lineThickness;

    public Function(String formula, Color lineColor, String lineStyle, String lineThickness) {
        this.id = Validator.getNextId();
        this.formula = formula;
        this.lineColor = lineColor;
        this.lineStyle = lineStyle;
        this.lineThickness = lineThickness;
    }

    public String getCss() {
        String css = new String();
        String cssLineStyle = new String();
        String s = lineStyle;
        if (s.equals(Const.solidLine)) {
            cssLineStyle = "-fx-stroke-dash-array: null;";
        } else if (s.equals(Const.dashedLine)) {
            cssLineStyle = "-fx-stroke-dash-array: 6.0 6.0;";
        } else if (s.equals(Const.dottedLine)) {
            cssLineStyle = "-fx-stroke-dash-array: 0.1 5.0; ";
        }
        String cssColor = "-fx-stroke: " + "#" + Integer.toHexString(lineColor.hashCode()) + ";";
        String cssThickness = "-fx-stroke-width: " + lineThickness + "px;";
        css = cssLineStyle + "\n" + cssColor + "\n" + cssThickness;
        return css;
    }
}
