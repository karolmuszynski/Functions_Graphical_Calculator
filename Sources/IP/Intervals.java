package individual;

import javafx.geometry.Point2D;

import java.awt.*;
import java.util.StringTokenizer;

public class Intervals {

    boolean isDomainSet;
    public double domainStart;
    public double domainEnd;

    boolean isCodomainSet;
    public double codomainStart;
    public double codomainEnd;

    void setDomain(String domainString) throws Exception {
        try {
            Point2D point = Parser.parseInterval(domainString);
            domainStart = point.getX();
            domainEnd = point.getY();
        } catch (Exception ex) {
            isDomainSet = false;
            throw ex;
        }
        isDomainSet = true;
    }

    void setCodomain(String codomainString) throws Exception {
        try {
            Point2D point = Parser.parseInterval(codomainString);
            codomainStart = point.getX();
            codomainEnd = point.getY();
        } catch (Exception ex) {
            isCodomainSet = false;
            throw ex;
        }
        isCodomainSet = true;
    }
}
