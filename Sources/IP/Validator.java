package individual;

import javafx.scene.control.*;
import javafx.scene.control.TextField;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.util.ArrayList;

public class Validator {
    static int idCount = 0;
    static ScriptEngineManager factory = new ScriptEngineManager();
    static ScriptEngine engine = factory.getEngineByName("JavaScript");

    public static String    checkAndReplaceSpecialSymbols(String s) {
        s = s.replaceAll("pi","3.14");
        s = s.replaceAll("e","2.71");
        return s;
    }

    public static boolean containsTernaryOperation(String formula) {
        return formula.contains("?");
    }

    public static boolean validateIntervals(Intervals intervals, TextField codomain) {
        return  intervals.isDomainSet && (codomain.getText().isEmpty() || intervals.isCodomainSet);
    }

    public static boolean validateFofEmptyFields(TextField domain, TextField function) {
        return !domain.getText().isEmpty() && !function.getText().isEmpty();
    }

    public static boolean ifAllConditionsTrue(ArrayList<String> conditions, double x) throws ScriptException {
        if(conditions.isEmpty()) return true;
        engine.put("x",x);
        for(String condition : conditions){
            boolean conditionBool = (Boolean) engine.eval(condition);
            if(!conditionBool) return false;
        }
        return true;
    }

    public static int getNextId() {
        return idCount++;
    }
}
