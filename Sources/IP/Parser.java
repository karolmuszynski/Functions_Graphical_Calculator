package individual;
import javafx.geometry.Point2D;
import java.util.StringTokenizer;

public class Parser {

    public static Point2D parseInterval(String intervalString) throws Exception {
        intervalString = Validator.checkAndReplaceSpecialSymbols(intervalString);
        String delim = "<(,)>";
        StringTokenizer tok = new StringTokenizer(intervalString, delim, false);
        String startString = tok.nextToken();
        String endString = tok.nextToken();
        double start = Double.parseDouble(startString);
        double end = Double.parseDouble(endString);
        if (start >= end)
            throw new Exception();
        return new Point2D(start,end);
    }

    public static String getTernaryLeft(String formula) throws Exception {
        return getTernaryPart(formula, 1);
    }

    public static String getTernaryRight(String formula) throws Exception {
        return getTernaryPart(formula, 2);
    }

    public static String getTernaryCondition(String formula) throws Exception {
        return getTernaryPart(formula, 0);
    }

    /*private static String getTernaryPart(int number, String formula) throws Exception {
        int i = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(formula, ":?()", false);
        while (stringTokenizer.hasMoreTokens()) {
            if(i == number)  return stringTokenizer.nextToken();
            stringTokenizer.nextToken();
            i++;
        }
        throw new Exception();
    }*/

    public static String getInverseCondition(String condition) {
        StringBuilder inverse = new StringBuilder(condition);
        inverse.insert(0,"!(");
        inverse.append(")");
        return inverse.toString();
    }

    private static String getTernaryPart(String formula, int number) throws Exception {
        formula = formula.trim();
        int formulaLength = formula.length();
        if(formula.charAt(0) == '(' && formula.charAt(formulaLength - 1) == ')')
            formula = formula.substring(1,formulaLength - 1);
        StringBuilder output = new StringBuilder();
        int bracketsCount = 0;
        for(char c: formula.toCharArray()) {
            if(c == '(') bracketsCount++;
            if(c == ')') bracketsCount--;

            if(c == '?' && bracketsCount == 0) {
                if(number == 0) return output.toString();
                else {
                    output.setLength(0);
                    continue;
                }
            }

            if(c == ':' && bracketsCount == 0) {
                if(number == 1) return output.toString();
                else {
                    output.setLength(0);
                    continue;
                }
            }
            output.append(c);
        }
        if(number == 2) return output.toString();
        throw new Exception("parse ternary operation faild");
    }
}
