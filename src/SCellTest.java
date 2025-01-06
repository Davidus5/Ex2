import static org.junit.jupiter.api.Assertions.*;

class SCellTest {

    @org.junit.jupiter.api.Test
    void isNumber() {
        SCell cell = new SCell("");
        assertTrue(cell.isNumber("123"), "Positive integer should be recognized as a number");
        assertTrue(cell.isNumber("-123.45"), "Negative floating-point should be recognized as a number");
        assertFalse(cell.isNumber("abc"), "Text should not be recognized as a number");
        assertFalse(cell.isNumber("123abc"), "Mixed input should not be recognized as a number");
    }

    @org.junit.jupiter.api.Test
    void isText() {
        SCell cell = new SCell("");
        assertTrue(cell.isText("hello"), "Plain text should be recognized as text");
        assertFalse(cell.isText("=1+2"), "Formulas should not be recognized as text");
        assertFalse(cell.isText("123"), "Numbers should not be recognized as text");
    }

    @org.junit.jupiter.api.Test
    void isForm() {
        SCell cell = new SCell("");
        assertTrue(cell.isForm("=1+2"), "Valid formula should be recognized as a formula");
        assertFalse(cell.isForm("123"), "Numbers should not be recognized as a formula");
        assertFalse(cell.isForm("hello"), "Plain text should not be recognized as a formula");
    }

    @org.junit.jupiter.api.Test
    void computeForm() {
        SCell cell = new SCell("");
        assertEquals(3.0, cell.computeForm("=1+2"), 0.001, "Formula =1+2 should compute to 3.0");
        assertEquals(9.0, cell.computeForm("=3*3"), 0.001, "Formula =3*3 should compute to 9.0");
        assertNull(cell.computeForm("=1/0"), "Division by zero should return null");
    }

    @org.junit.jupiter.api.Test
    void getContent() {
        SCell cell = new SCell("123");
        assertEquals("123", cell.getContent(), "Content should return the cell's data");
    }

    @org.junit.jupiter.api.Test
    void getOrder() {
        SCell cell = new SCell("=A1+B1");
        cell.setOrder(2);
        assertEquals(2, cell.getOrder(), "Order should match the value set");
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        SCell cell = new SCell("123");
        assertEquals("123", cell.toString(), "toString should return the cell's data");
    }

    @org.junit.jupiter.api.Test
    void setData() {
        SCell cell = new SCell("");
        cell.setData("456");
        assertEquals("456", cell.getData(), "setData should update the cell's data");
        assertEquals(Ex2Utils.NUMBER, cell.getType(), "setData should correctly identify the type");
    }

    @org.junit.jupiter.api.Test
    void testSetData() {
        SCell source = new SCell("123");
        SCell target = new SCell("");
        target.setData(source);
        assertEquals("123", target.getData(), "setData(Cell) should copy data from source");
        assertEquals(source.getType(), target.getType(), "setData(Cell) should copy type from source");
    }

    @org.junit.jupiter.api.Test
    void getData() {
        SCell cell = new SCell("hello");
        assertEquals("hello", cell.getData(), "getData should return the cell's content");
    }

    @org.junit.jupiter.api.Test
    void getType() {
        SCell cell = new SCell("=1+2");
        assertEquals(Ex2Utils.FORM, cell.getType(), "getType should return the correct type for a formula");
    }

    @org.junit.jupiter.api.Test
    void setType() {
        SCell cell = new SCell("123");
        cell.setType(Ex2Utils.TEXT);
        assertEquals(Ex2Utils.TEXT, cell.getType(), "setType should update the cell's type");
    }

    @org.junit.jupiter.api.Test
    void setOrder() {
        SCell cell = new SCell("=A1+B1");
        cell.setOrder(3);
        assertEquals(3, cell.getOrder(), "setOrder should update the computational order");
    }
}
