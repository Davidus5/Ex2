// Add your documentation below:

import java.text.Format;

public class SCell implements Cell {
    private String line;
    private int type;

    // Add your code here
    public SCell(String s) {
        // Add your code here
        setData(s);
    }

    public SCell(Cell cell) {
        // Add your code here
        setData(cell);
    }

    public boolean isNumber(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean isText(String text) {
        return !text.startsWith("=") && !isNumber(text);
    }

    public boolean isForm(String text) {
        return text.startsWith("=");
    }

    public Double computeForm(String form) {
        try {
            if (form.startsWith("=")) {
                form = form.substring(1); // Remove '='
            }
            // Basic expression evaluation
            return evaluateExpression(form);
        } catch (Exception e) {
            return null; // Invalid formula
        }
    }

    private Double evaluateExpression(String expression) {
        // TODO: Implement a proper formula evaluator (considering precedence and parentheses)
        // Placeholder for simple evaluation
        return 0.0;
    }

    public String getContent() {
        return line;
    }

    @Override
    public int getOrder() {
        // Add your code here

        return 0;
        // ///////////////////
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        // Add your code here
        // TEXT=1, NUMBER=2, FORM=3, ERR_FORM_FORMAT=-2, ERR_CYCLE_FORM=-1, ERR=-1;
        line = s;

        if(isNumber(s)) {
            type = Ex2Utils.NUMBER;
        }
        else if(isText(s)) {
            type = Ex2Utils.TEXT;
        }
        else if(isForm(s)) {
            type = Ex2Utils.FORM;

        }
        else {
            type = Ex2Utils.ERR;
            line = Ex2Utils.ERR_FORM;
        }
    }

    private int checkForm(String s){

        char[] arr = s.toCharArray();
        char prev = arr[1];
        for (int i = 2; i < arr.length; i++) {
            for (int j = 0; j < Ex2Utils.M_OPS.length; j++) {
                if
            }
        }

        return Ex2Utils.FORM;
    }

    public void setData(Cell s) {
        // Add your code here
        line = s.getData();
        type = s.getType();

        /////////////////////
    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        // Add your code here

    }
}
