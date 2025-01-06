// Add your documentation below:

import java.text.Format;

public class SCell implements Cell {
    private String line;// Stores the content of the cell (could be text, number, or formula)
    private int type; // Stores the type of the cell (e.g., TEXT, NUMBER, FORM, or error codes)


    // Add your code here
    public SCell(String s) {
        // Add your code here
        setData(s);// Sets the data and determines its type
    }

    public SCell(Cell cell) {
        // Add your code here
        setData(cell);// Copies the data and type from the provided cell
    }

    public boolean isNumber(String text) {
        try {
            Double.parseDouble(text);// Attempts to parse the text as a number
            return true;// Returns true if parsing succeeds
        } catch (NumberFormatException e) {
            return false;// Returns false if parsing fails
        }
    }
    // Helper method: Checks if the given text is plain text (not a number or formula)
    public boolean isText(String text) {
        return !text.startsWith("=") && !isNumber(text);// True if not a formula or number
    }

    public boolean isForm(String text) {
        return text.startsWith("=");// True if the text starts with '='
    }

    public Double computeForm(String form) {
        try {
            if (form.startsWith("=")) {
                form = form.substring(1); //  Removes '=' to evaluate the expression
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
        if (type == Ex2Utils.NUMBER) {
            return Double.parseDouble(line); // Return number as-is
        } else if (type == Ex2Utils.FORM) {
            Double result = computeForm(line);
            if (result != null) {
                return result;
            } else {
                throw new IllegalArgumentException("Invalid formula: " + line);
            }
        }
        return null; // For text or empty cells
    }


    public String getContent() {
        return line;// Returns the cell's content as a string
    }

    @Override
    public int getOrder() {
        if (type == Ex2Utils.NUMBER || type == Ex2Utils.TEXT) {
            return 0; // Numbers and text have depth 0
        }
        if (type == Ex2Utils.FORM) {
            // Compute depth based on formula dependencies
            // Example: If formula depends on other cells, find max depth of dependencies + 1
            return order; // Placeholder; actual logic depends on formula evaluation
        }
        return -1; // Return -1 for errors
    }

    //@Override
    @Override
    public String toString() {
        return getData();// Returns the cell's data
    }

    @Override
    public void setData(String s) {
        // Add your code here
        // TEXT=1, NUMBER=2, FORM=3, ERR_FORM_FORMAT=-2, ERR_CYCLE_FORM=-1, ERR=-1;
        line = s;// Assigns the input string to the cell's content
        // Determines the type of the cell based on its content
        if(isNumber(s)) {
            type = Ex2Utils.NUMBER;// Sets type to NUMBER
        }
        else if(isText(s)) {
            type = Ex2Utils.TEXT;// Sets type to TEXT
        }
        else if(isForm(s)) {
            type = checkForm(s);// Validates the formula and sets its type
        }
        else {
            type = Ex2Utils.ERR;// Sets type to ERR if content is invalid
            line = Ex2Utils.ERR_FORM;// Assigns an error message to the cell
        }
    }

    // Validates a formula string and determines if it is well-formed
    private int checkForm(String s){
        // Logic to check if the formula is valid
        // Parses the formula character by character to ensure proper syntax
        char[] arr = s.toCharArray();// Converts the formula to a char array
        String ops = "+-*/";// Defines valid operators
        String abc = "ABCDEFGHIJKLOMNPQRSTUVWXYZ";// Defines valid cell letters
        String nums = "0123456789";// Defines valid digits
        boolean is_cell = false;// Tracks whether a cell reference is being parsed
        int paren = 0;// Tracks the balance of parentheses
        int prev = arr[1];// Tracks the previous character in the formula

        // Initial validation for the first character after '='
        if (!(abc.indexOf(prev) != -1 || nums.indexOf(prev) != -1 || prev == '(')) {
            return Ex2Utils.ERR_FORM_FORMAT;// Invalid formula format
        }
        for (int i = 2; i < arr.length; i++) {
            // Add validation rules (e.g., operators, numbers, parentheses, etc.)
            // Details already provided in earlier explanations

            if (ops.indexOf(arr[i]) != -1 && abc.indexOf(arr[i]) != -1 && nums.indexOf(arr[i]) != -1 && arr[i] != '.') {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (prev == '.' && nums.indexOf(arr[i]) == -1) {
                return Ex2Utils.ERR_FORM_FORMAT; // Parentheses are not balanced
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
            return Ex2Utils.ERR_FORM_FORMAT; // Parentheses are not balanced
        }

        return Ex2Utils.FORM;// Formula is valid
    }

    public void setData(Cell s) {
        // Add your code here
        line = s.getData();// Copies the content from the provided cell
        type = s.getType(); // Copies the type from the provided cell

        /////////////////////
    }

    @Override
    public String getData() {
        return line;// Returns the cell's content
    }

    @Override
    public int getType() {
        return type;// Returns the cell's type
    }

    @Override
    public void setType(int t) {
        type = t;// Updates the cell's type
    }

    private int order;
    @Override
    public void setOrder(int t) {
        // Add your code here
        this.order = t;// Updates the order
        System.out.println("Order has been set to: " + t); // Optional: Log the order change
    }
}
