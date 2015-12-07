package individual;

import de.congrace.exp4j.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class Controller {

    @FXML
    private TextField domainTextField;
    @FXML
    private TextField codomainTextField;
    @FXML
    private LineChart coordinateLineChart;
    @FXML
    private TextField functionTextField;
    @FXML
    private NumberAxis xNumberAxis;
    @FXML
    private NumberAxis yNumberAxis;
    @FXML
    private ComboBox lineStyleComboBox;
    @FXML
    private ComboBox lineThiknessComboBox;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Pane numericPane;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private Label descriptionLabel;
    @FXML
    private VBox legendVBox;
    @FXML
    private CheckBox modificationModeCheckBox;
    @FXML
    private AnchorPane toSnapshot;

    Stage stage;
    MessageBox msgBox;
    Intervals intervals = new Intervals();
    LinkedList<FunctionViewHelper> functions = new LinkedList<FunctionViewHelper>();
    boolean axisXdescriptionFlag = true;
    boolean editingMode = false;
    int editingFunctionId = 0;
    int fontFactor = 0;
    ObservableList<XYChart.Series> seriesListChart = FXCollections.observableArrayList();
    ArrayList<XYChart.Series> lastSeries = new ArrayList<XYChart.Series>();
    ArrayList<CustomFunction> exp4jCustomFunction = new ArrayList<CustomFunction>();

    public void init(Stage stage) {
        coordinateLineChart.setData(seriesListChart);
        this.stage = stage;
        msgBox = new MessageBox(stage);
        lineStyleComboBox.getItems().addAll(
                Const.dashedLine, Const.solidLine, Const.dottedLine
        );
        lineStyleComboBox.setValue(Const.solidLine);
        lineThiknessComboBox.getItems().addAll(
                1, 2, 3, 4, 5, 8
        );
        lineThiknessComboBox.setValue(1);
        colorPicker.setValue(Const.getNextColor());
        coordinateLineChart.setLegendVisible(false);
        addAllListeners();

        //TODO remove
        /*domainTextField.setText("<-2,2)");
        functionTextField.setText("x > 0 ? (x > 1 ? 1:0) : -1");*/
    }

    private void drawFunctionReq(String formula, ArrayList<String> conditions, String cssStyle) throws Exception {
        if (!Validator.containsTernaryOperation(formula))
            drawFormula(buildExpression(formula), conditions, cssStyle);
        else {
            ArrayList<String> ternaryLeft = new ArrayList<String>(conditions);
            ArrayList<String> ternaryRight = new ArrayList<String>(conditions);
            String condition = Parser.getTernaryCondition(formula);
            ternaryLeft.add(condition);
            ternaryRight.add(Parser.getInverseCondition(condition));
            drawFunctionReq(Parser.getTernaryLeft(formula), ternaryLeft, cssStyle);
            drawFunctionReq(Parser.getTernaryRight(formula), ternaryRight, cssStyle);
        }
    }

    private void drawFormula(Calculable expression, ArrayList<String> conditions, String cssStyle) throws ScriptException {
        ArrayList<XYChart.Series> seriesList = new ArrayList<XYChart.Series>();
        seriesList.add(new XYChart.Series());
        double lastY = 0;
        for (double x = intervals.domainStart; x < intervals.domainEnd; x += 0.009) {
            if (Validator.ifAllConditionsTrue(conditions, x)) {
                if (x != intervals.domainStart) lastY = expression.calculate();
                expression.setVariable("x", x);
                if (Math.abs(lastY - expression.calculate()) > Const.brakeLineConst)
                    seriesList.add(new XYChart.Series());
                seriesList.get(seriesList.size() - 1).getData().add(new XYChart.Data(x, expression.calculate()));
            }
        }

        seriesListChart.addAll(seriesList);
        lastSeries.addAll(seriesList);

        setSeriesStyle(seriesList, cssStyle);
    }

    private void setSeriesStyle(ArrayList<XYChart.Series> seriesList, String cssStyle) {
        for (XYChart.Series series : seriesList) {
            series.getNode().setStyle(cssStyle);
        }
    }

    private Calculable buildExpression(String expression) throws UnparsableExpressionException, UnknownFunctionException {
        return new ExpressionBuilder(expression)
                .withVariableNames("x")
                .withCustomFunctions(exp4jCustomFunction)
                .build();
    }

    private void putIntoFunctionTextField(String text) {
        int caretPosition = functionTextField.getCaretPosition();
        String currentText = functionTextField.getText();
        String result = currentText.substring(0, caretPosition);
        result += text;
        result += currentText.substring(caretPosition, currentText.length());
        functionTextField.setText(result);
        functionTextField.positionCaret(caretPosition + text.length());
    }

    private void setDescriptionVisibility(boolean visibility) {
        descriptionTextField.clear();
        descriptionTextField.setVisible(visibility);
        descriptionLabel.setVisible(visibility);
        if (visibility) descriptionTextField.requestFocus();
    }

    private void setEditableIntervals(boolean editable) {
        codomainTextField.setEditable(editable);
        domainTextField.setEditable(editable);
        codomainTextField.setDisable(!editable);
        domainTextField.setDisable(!editable);
    }

    private FunctionViewHelper getByFunctionId(int id) {
        for (FunctionViewHelper fv : functions) {
            if (fv.function.id == id) return fv;
        }
        return null;
    }

    private void deleteFunction(int id) {
        FunctionViewHelper fv = getByFunctionId(id);
        legendVBox.getChildren().remove(fv.view);
        seriesListChart.removeAll(fv.series);
        setEditingMode(false);
    }

    private void setEditingModeFor(int functionId) {
        editingFunctionId = functionId;
        Function function = getByFunctionId(functionId).function;
        functionTextField.setText(function.formula);
        colorPicker.setValue(function.lineColor);
        lineStyleComboBox.setValue(function.lineStyle);
        lineThiknessComboBox.setValue(function.lineThickness);
        setEditingMode(true);
    }

    private void setEditingMode(boolean bool) {
        editingMode = bool;
        modificationModeCheckBox.setDisable(!bool);
        modificationModeCheckBox.setSelected(bool);
    }

    private HBox addLegendFunctionView(Function function) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(5);
        javafx.scene.shape.Rectangle rectangle = new Rectangle(7, 7);
        rectangle.setFill(null);
        rectangle.setStrokeWidth(2);
        rectangle.setStroke(function.lineColor);
        Label label = new Label(function.formula);
        label.setFont(new Font(12 + fontFactor));
        hBox.getChildren().add(rectangle);
        hBox.getChildren().add(label);
        legendVBox.getChildren().add(hBox);
        final int id = function.id;
        hBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        deleteFunction(id);
                    } else {
                        setEditingModeFor(id);
                    }
                }
            }
        });
        return hBox;
    }

    public static BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2) {

        //do some calculate first
        int offset  = 5;
        int wid = img1.getWidth()+img2.getWidth()+offset;
        int height = Math.max(img1.getHeight(),img2.getHeight())+offset;
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        java.awt.Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(java.awt.Color.WHITE);
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth()+offset, 0);
        g2.dispose();
        return newImage;
    }

    private void changeText(int newFontFactor) {
        //coordinateLineChart.getStyleClass().add("");
        for(FunctionViewHelper viewHelper: functions) {
            Label label = (Label) viewHelper.view.getChildren().get(1);
            label.setFont(new Font(12 + newFontFactor));
        }
        xNumberAxis.setTickLabelFont(new Font(12 + newFontFactor));
        yNumberAxis.setTickLabelFont(new Font(12 + newFontFactor));
        fontFactor = newFontFactor;
    }

    @FXML
    private void setModificationModeCheckBoxChangeHandlar() {
        if (editingMode) setEditingMode(false);
    }

    @FXML
    private void keyboardVisibilityHandler() {

            numericPane.setVisible(true);
            numericPane.setPrefHeight(95);
    }

    @FXML
    private void makeTextBigger() {
        if(fontFactor + 1 <= 5)
            changeText(fontFactor + 1);
    }

    @FXML
    private void makeTextSmaller() {
        if(fontFactor - 1 >= -5)
            changeText(fontFactor - 1);
    }

    @FXML
    private void drawButtonHandler() {
        //TODO remove
        //domainTextFieldOnChange();
        Function function = null;
        if (!Validator.validateFofEmptyFields(domainTextField, functionTextField)) {
            msgBox.show("You have to specify domain and formula.");
            return;
        }
        if (!Validator.validateIntervals(intervals, codomainTextField)) {
            msgBox.show("Specify proper domain and codomain.");
            return;
        }
        try {
            if (!editingMode) {
                function = new Function(functionTextField.getText(), colorPicker.getValue(), (String) lineStyleComboBox.getValue(),
                        lineThiknessComboBox.getValue().toString());
                colorPicker.setValue(Const.getNextColor());
            } else {
                function = getByFunctionId(editingFunctionId).function;
                function.formula = functionTextField.getText();
                seriesListChart.removeAll(getByFunctionId(editingFunctionId).series);
            }
            ArrayList<String> conditions = new ArrayList<String>();
            drawFunctionReq(function.formula, conditions, function.getCss());
            if (!editingMode)
                functions.add(new FunctionViewHelper(function, addLegendFunctionView(function), new ArrayList<XYChart.Series>(lastSeries)));
            else {
                getByFunctionId(editingFunctionId).series = new ArrayList<XYChart.Series>(lastSeries);
                ((Label) getByFunctionId(editingFunctionId).view.getChildren().get(1)).setText(function.formula);
            }
        } catch (UnknownFunctionException ex) {
            ex.printStackTrace();
            msgBox.show("Unknown function.");
        } catch (UnparsableExpressionException ex) {
            ex.printStackTrace();
            msgBox.show("Unparsable expression.");
        } catch (Exception ex) {
            ex.printStackTrace();
            msgBox.show("Unknown error.");
        } finally {
            lastSeries.clear();
        }
    }

    @FXML
    private void clearButtonHandler() {
        setEditableIntervals(true);
        functionTextField.clear();
        domainTextField.clear();
        codomainTextField.clear();
        legendVBox.getChildren().clear();
        coordinateLineChart.getData().clear();
    }

    @FXML
    private void saveButtonHandler() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                WritableImage graphImage = toSnapshot.snapshot(new SnapshotParameters(), null);
                ImageIO.write(SwingFXUtils.fromFXImage(graphImage, null), "png", file);
            } catch (IOException e) {
                msgBox.show("Unable to save image.");
            }
        }
    }

    @FXML
    private void domainTextFieldOnChange() {
        domainTextField.setStyle("");
        try {
            intervals.setDomain(domainTextField.getText());
            xNumberAxis.lowerBoundProperty().set(intervals.domainStart);
            xNumberAxis.upperBoundProperty().set(intervals.domainEnd);
        } catch (Exception e) {
            domainTextField.setStyle("-fx-focus-color: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void codomainTextFieldOnChange() {
        codomainTextField.setStyle("");
        if (!codomainTextField.getText().isEmpty()) {
            try {
                intervals.setCodomain(codomainTextField.getText());

                yNumberAxis.setAutoRanging(false);
                yNumberAxis.setUpperBound(intervals.codomainEnd);
                yNumberAxis.setLowerBound(intervals.codomainStart);
            } catch (Exception e) {
                codomainTextField.setStyle("-fx-focus-color: red;");
                e.printStackTrace();
            }
        } else yNumberAxis.setAutoRanging(true);
    }


    @FXML
    private void onClickAxisX() {
        setDescriptionVisibility(true);
        axisXdescriptionFlag = true;
    }

    @FXML
    private void onClickAxisY() {
        setDescriptionVisibility(true);
        axisXdescriptionFlag = false;
    }

    @FXML
    private void onKeyTypedDescriptionTextField(KeyEvent event) {
        if (KeyCode.ENTER.equals(event.getKeyCode()))
            descriptionTextField.focusedProperty().notify();
    }

    @FXML
    private void nuberic_1() {
        putIntoFunctionTextField("1");
    }

    @FXML
    private void nuberic_2() {
        putIntoFunctionTextField("2");
    }

    @FXML
    private void nuberic_3() {
        putIntoFunctionTextField("3");
    }

    @FXML
    private void nuberic_4() {
        putIntoFunctionTextField("4");
    }

    @FXML
    private void nuberic_5() {
        putIntoFunctionTextField("5");
    }

    @FXML
    private void nuberic_6() {
        putIntoFunctionTextField("6");
    }

    @FXML
    private void nuberic_7() {
        putIntoFunctionTextField("7");
    }

    @FXML
    private void nuberic_8() {
        putIntoFunctionTextField("8");
    }

    @FXML
    private void nuberic_9() {
        putIntoFunctionTextField("9");
    }

    @FXML
    private void nuberic_0() {
        putIntoFunctionTextField("0");
    }

    @FXML
    private void nuberic_div() {
        putIntoFunctionTextField("/");
    }

    @FXML
    private void nuberic_mul() {
        putIntoFunctionTextField("*");
    }

    @FXML
    private void nuberic_plus() {
        putIntoFunctionTextField("+");
    }

    @FXML
    private void nuberic_minus() {
        putIntoFunctionTextField("-");
    }

    @FXML
    private void numeric_pow() {
        putIntoFunctionTextField("^");
    }

    @FXML
    private void numeric_sqrt() {
        putIntoFunctionTextField("sqrt(x)");
    }

    @FXML
    private void numeric_sin() {
        putIntoFunctionTextField("sin(x)");
    }

    @FXML
    private void numeric_cos() {
        putIntoFunctionTextField("cos(x)");
    }

    @FXML
    private void numeric_tg() {
        putIntoFunctionTextField("tan(x)");
    }

    @FXML
    private void numeric_ctg() {
        putIntoFunctionTextField("atan(x)");
    }

    @FXML
    private void numeric_htg() {
        putIntoFunctionTextField("tanh(x)");
    }

    @FXML
    private void numeric_hsin() {
        putIntoFunctionTextField("sinh(x)");
    }

    @FXML
    private void numeric_min() {
        putIntoFunctionTextField("min(,)");
    }

    @FXML
    private void numeric_max() {
        putIntoFunctionTextField("max(,)");
    }

    @FXML
    private void numeric_floor() {
        putIntoFunctionTextField("floor()");
    }

    @FXML
    private void numeric_cell() {
        putIntoFunctionTextField("ceil()");
    }

    @FXML
    private void numeric_log() {
        putIntoFunctionTextField("log()");
    }

    @FXML
    private void numeric_exp() {
        putIntoFunctionTextField("exp()");
    }

    @FXML
    private void numeric_x() {
        putIntoFunctionTextField("x");
    }

    @FXML
    private void numeric_pi() {
        putIntoFunctionTextField("pi");
    }

    @FXML
    private void numeric_e() {
        putIntoFunctionTextField("e");
    }

    public class FunctionViewHelper {
        public ArrayList<XYChart.Series> series;
        public Function function;
        public HBox view;

        FunctionViewHelper(Function function, HBox view, ArrayList<XYChart.Series> series) {
            this.view = view;
            this.function = function;
            this.series = series;
        }
    }

    private void addAllListeners() {
        descriptionTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldProperty, Boolean newProperty) {
                if (!newProperty) {
                    if (axisXdescriptionFlag)
                        xNumberAxis.setLabel(descriptionTextField.getText());
                    else
                        yNumberAxis.setLabel(descriptionTextField.getText());
                    setDescriptionVisibility(false);
                }
            }
        });

        descriptionTextField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, new EventHandler<javafx.scene.input.KeyEvent>() {
            @Override
            public void handle(javafx.scene.input.KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    functionTextField.requestFocus();
            }
        });

        lineThiknessComboBox.valueProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue observableValue, Object s, Object s2) {
                if (editingMode) {
                    FunctionViewHelper fvh = getByFunctionId(editingFunctionId);
                    fvh.function.lineThickness = s2.toString();
                    setSeriesStyle(fvh.series, fvh.function.getCss());
                }
            }
        });

        lineStyleComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                if (editingMode) {
                    FunctionViewHelper fvh = getByFunctionId(editingFunctionId);
                    fvh.function.lineStyle = s2;
                    setSeriesStyle(fvh.series, fvh.function.getCss());
                }
            }
        });

        colorPicker.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observableValue, Color color, Color color2) {
                if (editingMode) {
                    FunctionViewHelper fvh = getByFunctionId(editingFunctionId);
                    fvh.function.lineColor = color2;
                    setSeriesStyle(fvh.series, fvh.function.getCss());
                    Rectangle rectangle = (Rectangle) fvh.view.getChildren().get(0);
                    rectangle.setStroke(color2);
                }
            }
        });

        modificationModeCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                if (!aBoolean2) {
                    setEditingMode(false);
                    colorPicker.setValue(Const.getNextColor());
                    functionTextField.setText("");
                }
            }
        });

        try {
            CustomFunction custom1 = new CustomFunction("max",2) {
                @Override
                public double applyFunction(double[] values) {
                    double max=values[0];
                    for (int i=1;i<this.getArgumentCount();i++) {
                        if (values[i] > max) {
                            max=values[i];
                        }
                    }
                    return max;
                }
            };

            CustomFunction custom2 = new CustomFunction("min",2) {
                @Override
                public double applyFunction(double[] values) {
                    double min=values[0];
                    for (int i=1;i<this.getArgumentCount();i++) {
                        if (values[i] < min) {
                            min=values[i];
                        }
                    }
                    return min;
                }
            };

            exp4jCustomFunction.add(custom1);
            exp4jCustomFunction.add(custom2);
        } catch (InvalidCustomFunctionException e) {
            e.printStackTrace();
        }
    }
}
