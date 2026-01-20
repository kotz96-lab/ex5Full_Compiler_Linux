# Person A Workspace Summary

## Directory Structure
```
person_a_ir_generation/
├── GETTING_STARTED.md       # Quick start guide - READ THIS FIRST
├── README.md                 # Overview and work plan
├── TODO_PERSON_A.md          # Detailed task checklist
├── WORKSPACE_SUMMARY.md      # This file
├── src/                      # Source code (copied from ex4)
│   ├── Main.java
│   ├── ast/                  # 42 AST node classes
│   ├── ir/                   # 18 existing IR commands (need to add ~14 more)
│   ├── temp/                 # Temporary variable factory
│   ├── symboltable/          # Symbol table
│   ├── types/                # Type system
│   └── cfg/                  # Control flow graph
└── templates/                # Example IR command templates
    ├── IrCommandStringConcat.java
    ├── IrCommandArrayAccess.java
    └── IrCommandNewArray.java
```

## Files Inventory

### Documentation (4 files)
1. **GETTING_STARTED.md** - ⭐ START HERE - Step-by-step guide
2. **README.md** - Work overview and what's already done vs. what's needed
3. **TODO_PERSON_A.md** - Detailed checklist of all tasks
4. **WORKSPACE_SUMMARY.md** - This file

### Source Code (88 Java files)
All copied from `ex4/206055055/ex4/src/`:
- **Main.java** - Entry point
- **ast/** - 42 AST node classes
- **ir/** - 18 IR command classes
- **temp/** - Temporary variable management
- **symboltable/** - Symbol table for variable lookup
- **types/** - Type system (10 classes)
- **cfg/** - Control flow graph (5 classes)

### Templates (3 files)
Example implementations to use as templates:
1. **IrCommandStringConcat.java** - String concatenation
2. **IrCommandArrayAccess.java** - Array element access
3. **IrCommandNewArray.java** - Array allocation

## What Person A Needs to Do

### High-Level Goals
1. **Extend IR** - Add ~14 new IR command types for strings, arrays, objects
2. **Implement irMe()** - Add/modify ~10 AST node irMe() methods
3. **Test** - Verify IR generation for full L language
4. **Document** - Provide IR specification to Person B and C

### Work Breakdown

#### New IR Commands to Create (14)
**Strings (3):**
- IrCommandConstString.java
- IrCommandStringConcat.java ✓ (template provided)
- IrCommandStringEqual.java

**Arrays (4):**
- IrCommandNewArray.java ✓ (template provided)
- IrCommandArrayAccess.java ✓ (template provided)
- IrCommandArrayStore.java
- IrCommandArrayLength.java

**Objects/Classes (4):**
- IrCommandNewObject.java
- IrCommandFieldAccess.java
- IrCommandFieldStore.java
- IrCommandMethodCall.java

**Control Flow (3):**
- IrCommandReturn.java
- IrCommandReturnVoid.java
- IrCommandNilConst.java

#### AST Nodes to Modify/Implement (10)
**Add irMe() to:**
- src/ast/AstSimpleExpString.java
- src/ast/AstSimpleExpNil.java
- src/ast/AstVarSubscript.java
- src/ast/AstVarField.java
- src/ast/AstStmtReturn.java

**Extend irMe() in:**
- src/ast/AstStmtAssign.java (handle array/field assignments)
- src/ast/AstExpBinop.java (handle string operations)
- src/ast/AstProgram.java (global variable initialization)
- src/ast/AstDecVar.java (non-constant initialization)
- src/ast/AstExpCall.java (ensure left-to-right arg evaluation)

## Key Concepts

### IR Command Pattern
Every IR command:
1. Extends `IrCommand` base class
2. Has fields for destination temp and operand temps
3. Has constructor to initialize fields
4. Has toString() for debugging/printing

Example:
```java
public class IrCommandSomething extends IrCommand {
    public Temp dst;
    public Temp operand;

    public IrCommandSomething(Temp dst, Temp operand) {
        this.dst = dst;
        this.operand = operand;
    }

    public String toString() {
        return String.format("Temp_%d := SOMETHING(Temp_%d)",
            dst.getSerialNumber(),
            operand.getSerialNumber());
    }
}
```

### AST irMe() Pattern
Every irMe() method:
1. Recursively calls irMe() on child nodes
2. Gets fresh temporaries via TempFactory
3. Adds IR commands to global Ir instance
4. Returns temporary with result

Example:
```java
public Temp irMe() {
    // Generate IR for children
    Temp t_child = child.irMe();

    // Get fresh temp for result
    Temp t_result = TempFactory.getInstance().getFreshTemp();

    // Add IR command
    Ir.getInstance().AddIrCommand(
        new IrCommandSomething(t_result, t_child)
    );

    // Return result temp
    return t_result;
}
```

## Quick Reference - Existing IR Commands (from ex4)

Already implemented in `src/ir/`:
- **Control:** IrCommandLabel, IrCommandJumpLabel, IrCommandJumpIfEqToZero
- **Arithmetic:** IrCommandBinopAddIntegers, IrCommandBinopSubIntegers, IrCommandBinopMulIntegers, IrCommandBinopDivIntegers
- **Comparison:** IrCommandBinopEqIntegers, IrCommandBinopLtIntegers
- **Unary:** IrCommandBinopMinusInteger
- **Memory:** IrCommandLoad, IrCommandStore, IrCommandAllocate
- **Other:** IRcommandConstInt, IrCommandCallFunc

## Quick Reference - AST Nodes with irMe() (from ex4)

Already implemented in `src/ast/`:
- **Statements:** AstStmtAssign, AstStmtIf, AstStmtWhile, AstStmtCall
- **Expressions:** AstExpBinop, AstExpCall, AstExpVar, AstSimpleExpInt
- **Functions:** AstDecFunc

## Testing Strategy

### Unit Testing
Test each new IR command:
- Create simple L program that uses the feature
- Run through AST → IR translation
- Verify IR commands are generated correctly

### Integration Testing
Test complete programs:
1. String concatenation and equality
2. Array allocation, access, and assignment
3. Object creation, field access, and methods
4. Global variable initialization
5. Function returns
6. Evaluation order (arguments, binary ops)

### Example Test Programs
See `GETTING_STARTED.md` for example test programs.

## Timeline Suggestion

**Week 1:**
- Understand existing code
- Create new IR command classes
- Test with simple examples

**Week 2:**
- Implement irMe() methods in AST nodes
- Test with complex programs
- Handle edge cases

**Week 3:**
- Integration with Person B and C
- Final testing
- Documentation

## Deliverables to Team

### To Person B (Register Allocation):
- Complete IR representation of program
- List of all temporaries used
- Documentation of IR format

### To Person C (MIPS Generation):
- IR command semantics documentation
- String literal table
- Memory layout specifications (arrays, objects)

## Getting Help

### Resources:
1. ex4 assignment specification (ex4.pdf)
2. ex5 assignment specification (ex5.pdf)
3. Course tutorial materials
4. Team members (coordinate early and often!)

### Common Questions:
- **How do I run the code?** - Need to set up build system (copy Makefile from ex4)
- **How do I test?** - Create simple L programs, run through compiler
- **What if I get stuck?** - Ask team, check ex4 code for similar patterns
- **How do runtime checks work?** - Coordinate with Person C on who generates them

## Status Tracking

Use this checklist to track progress:

### IR Commands:
- [ ] IrCommandConstString
- [ ] IrCommandStringConcat
- [ ] IrCommandStringEqual
- [ ] IrCommandNewArray
- [ ] IrCommandArrayAccess
- [ ] IrCommandArrayStore
- [ ] IrCommandArrayLength
- [ ] IrCommandNewObject
- [ ] IrCommandFieldAccess
- [ ] IrCommandFieldStore
- [ ] IrCommandMethodCall
- [ ] IrCommandReturn
- [ ] IrCommandReturnVoid
- [ ] IrCommandNilConst

### AST irMe() Methods:
- [ ] AstSimpleExpString.java
- [ ] AstSimpleExpNil.java
- [ ] AstVarSubscript.java
- [ ] AstVarField.java
- [ ] AstStmtReturn.java
- [ ] AstStmtAssign.java (extended)
- [ ] AstExpBinop.java (extended)
- [ ] AstProgram.java (extended)
- [ ] AstDecVar.java (extended)
- [ ] AstExpCall.java (extended)

### Testing:
- [ ] String operations
- [ ] Array operations
- [ ] Object operations
- [ ] Method calls
- [ ] Returns
- [ ] Global initialization
- [ ] Evaluation order

### Integration:
- [ ] Documentation for Person B
- [ ] Documentation for Person C
- [ ] Team coordination meeting
- [ ] Final integration test

---

**Total estimated effort:** 20-30 hours
**Difficulty:** Medium-High
**Key success factors:** Understanding ex4 patterns, careful implementation, good testing

Good luck! 🚀
