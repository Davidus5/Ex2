# Ex2: Foundation of Object-Oriented and Recursion

## Introduction
This project focuses on the fundamentals of **object-oriented design** and **recursion** through the implementation of a simplified **Spreadsheet application**. The spreadsheet is a 2D array of **Cells**, where each cell can store:
- **Numbers** (e.g., `123`, `-45.6`)
- **Formulas** (e.g., `=1+2`, `=(A1+B2)*3`)

### Supported Formula Types:
1. `=number` (e.g., `=5`, `=1.2`)
2. `=(Formula)` (e.g., `=(5+3)`)
3. `=Formula op Formula` (e.g., `=A1+B2`, where `op` is one of `{+, -, *, /}`)
4. `=cell` (e.g., `A0`, `B1`)

#### Valid Formulas:
- `=1`
- `=1.2`
- `=(0.2)`
- `=1+2`
- `=1+2*3`
- `=(1+2)*((3))-1`
- `=A1`
- `=A2+3`
- `=(2+A3)/A2`

#### Invalid Formulas (ERR_WRONG_FORM):
- `a`, `AB`, `@2`
- `2+)`, `(3+1*2)-`
- `=()`, `=5**`

### Error Handling:
1. **ERR_WRONG_FORM**: Invalid formula format.
2. **ERR_CYCLE**: Self-referencing cells or cyclic dependencies (e.g., `A0:A0`, `A1 depends on E4 while E4 depends on A1`).

---
An example of the Ex2 (partial) solution
![צילום מסך 2025-01-06 131025](https://github.com/user-attachments/assets/c1ec5b5b-211c-4361-81cc-26a568503358)
-------
## Project Features
1. **Formula Evaluation**: Handles arithmetic operations, parentheses, and cell references.
2. **Cycle Detection**: Prevents infinite loops in cell dependencies.
3. **Error Propagation**: Ensures errors in referenced cells propagate correctly.
4. **GUI Integration**: Provides a user-friendly interface for interacting with the spreadsheet.
-----
Inspired by the assignment description provided in this document. https://docs.google.com/document/d/1-18T-dj00apE4k1qmpXGOaqttxLn-Kwi/edit Built for educational purposes to explore string manipulation and base conversions in Java.

