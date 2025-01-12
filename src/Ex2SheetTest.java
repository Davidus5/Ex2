import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Ex2SheetTest {
    private Ex2Sheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Ex2Sheet(5, 5); // Create a 5x5 sheet for testing
    }

    @Test
    void xCell() {
        assertEquals(0, sheet.xCell("A1"));
        assertEquals(1, sheet.xCell("B2"));
        assertEquals(-1, sheet.xCell("1A"));
        assertEquals(-1, sheet.xCell("Z"));
    }

    @Test
    void yCell() {
        assertEquals(1, sheet.yCell("A1"));
        assertEquals(2, sheet.yCell("B2"));
        assertEquals(-1, sheet.yCell("A"));
        assertEquals(-1, sheet.yCell("A-1"));
    }

    @Test
    void value() {
        sheet.set(0, 0, "5");
        assertEquals("5", sheet.value(0, 0));

        sheet.set(0, 1, "=A1+5");
        assertEquals("10.0", sheet.value(0, 1));

        sheet.set(1, 1, "=B1");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(1, 1)); // Circular reference
    }

    @Test
    void get() {
        sheet.set(0, 0, "Hello");
        assertEquals("Hello", sheet.get(0, 0).toString());

        assertNull(sheet.get("Z10")); // Invalid coordinates
        assertNotNull(sheet.get("A1")); // Valid coordinates
    }

    @Test
    void width() {
        assertEquals(5, sheet.width());
    }

    @Test
    void height() {
        assertEquals(5, sheet.height());
    }

    @Test
    void set() {
        sheet.set(0, 0, "10");
        assertEquals("10", sheet.get(0, 0).toString());

        sheet.set(0, 1, "=A1*2");
        assertEquals("20.0", sheet.value(0, 1));
    }

    @Test
    void eval() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A1+10");
        sheet.eval();
        assertEquals("15.0", sheet.value(0, 1));
    }

    @Test
    void isIn() {
        assertTrue(sheet.isIn(0, 0));
        assertFalse(sheet.isIn(-1, 0));
        assertFalse(sheet.isIn(5, 5));
    }

    @Test
    void depth() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A1+10");
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]); // Number
        assertEquals(1, depths[0][1]); // Formula dependent on A1
    }

    @Test
    void load() throws Exception {
        sheet.load("test_sheet.txt");
        assertEquals("5", sheet.value(0, 0));
        assertEquals("10", sheet.value(0, 1));
    }

    @Test
    void save() throws Exception {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A1+5");
        sheet.save("test_sheet_save.txt");

        Ex2Sheet loadedSheet = new Ex2Sheet();
        loadedSheet.load("test_sheet_save.txt");
        assertEquals("5", loadedSheet.value(0, 0));
        assertEquals("10.0", loadedSheet.value(0, 1));
    }

    @Test
    void computeForm() {
        sheet.set(0, 0, "5");
        assertEquals(10.0, sheet.computeForm("=A1+5"));

        sheet.set(0, 1, "=A1+B1");
        assertNull(sheet.computeForm("=A1+B1")); // Invalid (B1 is empty)
    }

    @Test
    void evaluateExpression() {
        assertEquals(10.0, Ex2Sheet.evaluateExpression("5+5"));
        assertEquals(25.0, Ex2Sheet.evaluateExpression("5*5"));
        assertEquals(2.0, Ex2Sheet.evaluateExpression("10/5"));
        assertEquals(null, Ex2Sheet.evaluateExpression("5/0")); // Division by zero
    }

    @Test
    void evalAll() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A1+10");
        String[][] results = sheet.evalAll();
        assertEquals("5", results[0][0]);
        assertEquals("15.0", results[0][1]);
    }

    @Test
    void testEval() {
        sheet.set(0, 0, "5");
        assertEquals("5", sheet.eval(0, 0));

        sheet.set(0, 1, "=A1+10");
        assertEquals("15.0", sheet.eval(0, 1));

        sheet.set(1, 1, "=B1");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(1, 1)); // Circular reference
    }
}
