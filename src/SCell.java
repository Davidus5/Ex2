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
            type = checkForm(s);
        }
        else {
            type = Ex2Utils.ERR;
            line = Ex2Utils.ERR_FORM;
        }
    }

    private int checkForm(String s){
        char[] arr = s.toCharArray();
        String ops = "+-*/";
        String abc = "ABCDEFGHIJKLOMNPQRSTUVWXYZ";
        String nums = "0123456789";
        boolean is_cell = false;
        int paren = 0;
        int prev = arr[1];
        if (!(abc.indexOf(prev) != -1 || nums.indexOf(prev) != -1 || prev == '(')) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }
        for (int i = 2; i < arr.length; i++) {
            if (ops.indexOf(arr[i]) != -1 && abc.indexOf(arr[i]) != -1 && nums.indexOf(arr[i]) != -1 && arr[i] != '.') {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (prev == '.' && nums.indexOf(arr[i]) == -1) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (arr[i] == '.' && nums.indexOf(prev) == -1 && is_cell) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (prev == ')' && (ops.indexOf(arr[i]) == -1 && arr[i] != ')'))
            {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if((ops.indexOf(prev) != -1 || prev == '(') && ops.indexOf(arr[i]) != -1)// checks operation is not followed by another operation
            {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if(ops.indexOf(prev) != -1 && arr[i] == ')')// checks operation is not followed by another operation
            {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if(abc.indexOf(prev) != -1 && nums.indexOf(arr[i]) == -1) // checks letter is followed by number
            {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if(prev == '(' && arr[i] == ')')
            {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if(prev == '(')
            {
                ++paren;
            }
            if(prev == ')')
            {
                --paren;
                if (paren < 0) {
                    return Ex2Utils.ERR_FORM_FORMAT;
                }
            }

            if (abc.indexOf(arr[i]) != -1)
            {
                is_cell = true;
            }

            if(is_cell && arr[i] == '('){
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if(is_cell && nums.indexOf(arr[i]) == -1)
            {
                is_cell = false;
            }

            prev = arr[i];
        }

        if(prev == ')')
        {
            --paren;
            if (paren < 0) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }
        }

        if(prev != ')' && nums.indexOf(prev) == -1)
        {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        if(paren != 0){
            return Ex2Utils.ERR_FORM_FORMAT;
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

    private int order;
    @Override
    public void setOrder(int t) {
        // Add your code here
        this.order = t;
        System.out.println("Order has been set to: " + t); // Optional: Log the order change


    }
}
