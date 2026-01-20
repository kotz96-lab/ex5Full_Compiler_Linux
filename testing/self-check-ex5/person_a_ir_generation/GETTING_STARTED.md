# Person A: Getting Started Guide

## What You Have

### 1. Source Code (from ex4)
Located in `src/` directory:
- **88 Java files** copied from ex4/206055055/ex4/src/
- All existing IR commands (18 types)
- All AST node classes (42 types)
- Supporting infrastructure (temp, symboltable, types, cfg)

### 2. Documentation
- `README.md` - Overview and work plan
- `TODO_PERSON_A.md` - Detailed task checklist
- `GETTING_STARTED.md` - This file

### 3. Templates
Located in `templates/` directory:
- `IrCommandStringConcat.java` - Example string concatenation IR command
- `IrCommandArrayAccess.java` - Example array access IR command
- `IrCommandNewArray.java` - Example array allocation IR command

Use these as templates for creating the other new IR commands.

## Quick Start Steps

### Step 1: Understand Existing IR Structure

Read these files first to understand the pattern:
1. `src/ir/IrCommand.java` - Base class for all IR commands
2. `src/ir/Ir.java` - IR container (singleton) that holds command list
3. `src/ir/IrCommandBinopAddIntegers.java` - Example IR command
4. `src/temp/TempFactory.java` - How to create fresh temporaries

### Step 2: Understand Existing AST IR Generation

Read these files to see how irMe() works:
1. `src/ast/AstStmtAssign.java` - How assignments generate IR
2. `src/ast/AstExpBinop.java` - How binary operations generate IR
3. `src/ast/AstStmtIf.java` - How if statements generate IR with labels

**Pattern you'll see:**
```java
public Temp irMe() {
    // 1. Recursively generate IR for child nodes
    Temp t_child = childNode.irMe();

    // 2. Get fresh temporary for result
    Temp t_result = TempFactory.getInstance().getFreshTemp();

    // 3. Add IR command to global IR list
    Ir.getInstance().AddIrCommand(
        new IrCommandSomething(t_result, t_child)
    );

    // 4. Return temporary holding result
    return t_result;
}
```

### Step 3: Create New IR Commands

Start with the high-priority commands:

**Priority 1 - Strings:**
1. Copy `templates/IrCommandStringConcat.java` to `src/ir/`
2. Create `src/ir/IrCommandStringEqual.java` (similar pattern)
3. Create `src/ir/IrCommandConstString.java` (similar to IRcommandConstInt.java)

**Priority 2 - Arrays:**
1. Copy `templates/IrCommandArrayAccess.java` to `src/ir/`
2. Copy `templates/IrCommandNewArray.java` to `src/ir/`
3. Create `src/ir/IrCommandArrayStore.java` (similar to ArrayAccess)
4. Create `src/ir/IrCommandArrayLength.java`

**Priority 3 - Objects:**
1. Create `src/ir/IrCommandNewObject.java` (similar to NewArray)
2. Create `src/ir/IrCommandFieldAccess.java` (similar to ArrayAccess)
3. Create `src/ir/IrCommandFieldStore.java` (similar to ArrayStore)
4. Create `src/ir/IrCommandMethodCall.java` (similar to IrCommandCallFunc.java)

### Step 4: Implement irMe() in AST Nodes

**Easy wins - Simple expressions:**
1. `src/ast/AstSimpleExpNil.java` - Add irMe() that returns temp with value 0
2. `src/ast/AstSimpleExpString.java` - Add irMe() that creates string constant

**Medium difficulty - Variable access:**
3. `src/ast/AstVarSubscript.java` - Add irMe() for array[index]
4. `src/ast/AstVarField.java` - Add irMe() for object.field

**More complex - Statements:**
5. `src/ast/AstStmtReturn.java` - Add irMe() for return statements
6. Modify `src/ast/AstStmtAssign.java` - Extend to handle array and field assignments

### Step 5: Handle Evaluation Order

**Binary operations** (should already be correct):
- Check `src/ast/AstExpBinop.java`
- Make sure left side irMe() is called BEFORE right side irMe()

**Function arguments:**
- Check `src/ast/AstExpCall.java` or wherever function calls are handled
- Make sure arguments are evaluated left to right

### Step 6: Test Your Work

Create simple test programs:

**Test 1: String concatenation**
```java
// test_string.l
void main() {
    string s1 := "hello";
    string s2 := "world";
    string s3 := s1 + s2;
    PrintString(s3);
}
```

**Test 2: Array access**
```java
// test_array.l
array IntArray = int[];
void main() {
    IntArray arr := new int[5];
    arr[2] := 42;
    PrintInt(arr[2]);
}
```

**Test 3: Object creation**
```java
// test_object.l
class Point {
    int x := 0;
    int y := 0;
}
void main() {
    Point p := new Point;
    p.x := 10;
    PrintInt(p.x);
}
```

## How to Build and Test

### Building (assuming you set up build infrastructure)
```bash
cd person_a_ir_generation
javac -cp ".:external_jars/*" -d bin src/**/*.java
```

### Testing (you'll need to integrate with parser/lexer)
```bash
# Run your compiler on a test file
java -cp "bin:external_jars/*" Main test_string.l output.txt

# Check the IR output
cat output.txt
```

## Integration with Team

### Deliverables to Person B (Register Allocation)
1. **IR code** - The linked list of IrCommand objects
2. **List of temporaries** - All Temp objects used
3. **IR format documentation** - Explain what each IR command does

**Suggested format for handoff:**
- Provide the Ir singleton instance
- Ensure all temporaries are accessible via Ir.getInstance()
- Document any special temporaries (e.g., return value temporary)

### Deliverables to Person C (MIPS Generation)
1. **IR command semantics** - What each IR command should do in MIPS
2. **String literal table** - All string constants used in program
3. **Data layout info** - How arrays and objects are laid out in memory

**Example documentation:**
```
IrCommandStringConcat(dst, str1, str2):
  1. Load address of str1 into register
  2. Calculate length of str1
  3. Load address of str2 into register
  4. Calculate length of str2
  5. Allocate (len1 + len2 + 1) bytes on heap
  6. Copy str1 to new memory
  7. Copy str2 after str1
  8. Add null terminator
  9. Store address in dst
```

## Common Patterns and Tips

### Pattern 1: Load from Memory/Variable
```java
Temp t = TempFactory.getInstance().getFreshTemp();
Ir.getInstance().AddIrCommand(
    new IrCommandLoad(t, variableName)
);
return t;
```

### Pattern 2: Store to Memory/Variable
```java
Temp t_value = exp.irMe(); // Get value to store
Ir.getInstance().AddIrCommand(
    new IrCommandStore(variableName, t_value)
);
```

### Pattern 3: Binary Operation
```java
Temp t_left = left.irMe();
Temp t_right = right.irMe();
Temp t_result = TempFactory.getInstance().getFreshTemp();
Ir.getInstance().AddIrCommand(
    new IrCommandBinopAddIntegers(t_result, t_left, t_right)
);
return t_result;
```

### Pattern 4: Control Flow (If/While)
```java
String label_end = IrCommand.getFreshLabel("if_end");
String label_else = IrCommand.getFreshLabel("if_else");

Temp t_cond = condition.irMe();
Ir.getInstance().AddIrCommand(
    new IrCommandJumpIfEqToZero(t_cond, label_else)
);

// True branch
body.irMe();
Ir.getInstance().AddIrCommand(
    new IrCommandJumpLabel(label_end)
);

// Else branch
Ir.getInstance().AddIrCommand(
    new IrCommandLabel(label_else)
);
elseBody.irMe();

Ir.getInstance().AddIrCommand(
    new IrCommandLabel(label_end)
);
```

## Questions to Ask Team

Before you start, coordinate with your team on:

1. **What IR format should we use?**
   - Java objects (current approach)?
   - Text representation?
   - Both?

2. **How should runtime checks be handled?**
   - Should Person A generate IR for bounds checks?
   - Or should Person C add them during MIPS generation?

3. **How should we handle method dispatch?**
   - Static dispatch (simple)?
   - Dynamic dispatch with vtables (complex)?

4. **What's our testing strategy?**
   - Unit tests for each IR command?
   - Integration tests with full programs?
   - Who writes the test cases?

## Next Steps

1. Read through the existing code to understand the patterns
2. Create the new IR command classes
3. Implement irMe() methods in AST nodes
4. Test with simple L programs
5. Coordinate with Person B and C for integration
6. Document your IR format for the team

Good luck! You're building the foundation that the rest of the compiler depends on.
