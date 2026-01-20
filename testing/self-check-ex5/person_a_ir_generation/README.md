# Person A: IR Generation - Work Plan

## Overview
Extend IR generation from ex4 (limited subset) to handle the **entire L language**.

## Current State (from ex4)
The ex4 implementation handles a limited subset of L:
- Only `void main()` as entry point
- Only `int` variables (no strings, arrays, classes)
- Basic binary operations on integers
- If/else and while statements
- Simple variable assignments
- Function calls (basic)

## What's Already Implemented in ex4

### IR Commands Available (18 types):
Located in: `ex4/206055055/ex4/src/ir/`

1. **Control Flow:**
   - `IrCommandLabel.java` - Labels for jumps
   - `IrCommandJumpLabel.java` - Unconditional jumps
   - `IrCommandJumpIfEqToZero.java` - Conditional jumps

2. **Binary Operations:**
   - `IrCommandBinopAddIntegers.java` - Addition
   - `IrCommandBinopSubIntegers.java` - Subtraction
   - `IrCommandBinopMulIntegers.java` - Multiplication
   - `IrCommandBinopDivIntegers.java` - Division
   - `IrCommandBinopEqIntegers.java` - Equality
   - `IrCommandBinopLtIntegers.java` - Less-than
   - `IrCommandBinopMinusInteger.java` - Unary negation

3. **Memory Operations:**
   - `IrCommandLoad.java` - Load from variables
   - `IrCommandStore.java` - Store to variables
   - `IrCommandAllocate.java` - Memory allocation

4. **Other:**
   - `IRcommandConstInt.java` - Integer constants
   - `IrCommandCallFunc.java` - Function calls

### AST Nodes with irMe() Already Implemented:
Located in: `ex4/206055055/ex4/src/ast/`

1. **Statements:**
   - `AstStmtAssign.java` - Simple assignments (var := exp)
   - `AstStmtIf.java` - If/else statements
   - `AstStmtWhile.java` - While loops
   - `AstStmtCall.java` - Function call statements

2. **Expressions:**
   - `AstExpBinop.java` - Binary operations
   - `AstExpCall.java` - Function calls
   - `AstExpVar.java` - Variable references
   - `AstSimpleExpInt.java` - Integer literals

## Work To Do: Extend IR Generation

### Phase 1: Add Missing IR Commands

Create new IR command types for full L support:

#### String Operations:
- [ ] `IrCommandStringConcat.java` - String concatenation
- [ ] `IrCommandStringEqual.java` - String equality (content comparison)
- [ ] `IrCommandConstString.java` - String constants

#### Array Operations:
- [ ] `IrCommandArrayAccess.java` - Array element access
- [ ] `IrCommandArrayStore.java` - Store to array element
- [ ] `IrCommandArrayLength.java` - Get array length
- [ ] `IrCommandNewArray.java` - Array allocation

#### Class/Object Operations:
- [ ] `IrCommandFieldAccess.java` - Class field access
- [ ] `IrCommandFieldStore.java` - Store to class field
- [ ] `IrCommandNewObject.java` - Object instantiation
- [ ] `IrCommandMethodCall.java` - Method calls (with vtable support)

#### Other Operations:
- [ ] `IrCommandReturn.java` - Function return with value
- [ ] `IrCommandReturnVoid.java` - Void function return
- [ ] `IrCommandNilConst.java` - nil constant

### Phase 2: Implement irMe() in AST Nodes

Extend/create irMe() methods in AST nodes:

#### Currently Missing irMe():
- [ ] `AstStmtReturn.java` - Return statements
- [ ] `AstVarSubscript.java` - Array subscript access (var[exp])
- [ ] `AstVarField.java` - Class field access (var.field)
- [ ] `AstSimpleExpString.java` - String literals
- [ ] `AstSimpleExpNil.java` - nil values

#### Need to Extend:
- [ ] `AstStmtAssign.java` - Handle array and field assignments
- [ ] `AstDecVar.java` - Global variable initialization with non-constant expressions
- [ ] `AstDecClass.java` - Generate IR for class layout/vtables
- [ ] `AstDecFunc.java` - Handle function prologue/epilogue properly

#### New Expression Types:
- [ ] Object creation (`new ClassName`)
- [ ] Array creation (`new Type[size]`)
- [ ] Method calls (different from function calls)

### Phase 3: Handle Evaluation Order

Ensure proper left-to-right evaluation:
- [ ] Binary operations: evaluate left side first
- [ ] Function arguments: evaluate from left to right
- [ ] Assignment: evaluate RHS, then LHS (for array/field assignments)

### Phase 4: Handle Global Variables

- [ ] Initialize global variables in order of appearance
- [ ] Support non-constant expressions in global initializers
- [ ] Generate initialization code before main() entry

## Files to Copy from ex4

Copy these directories from `ex4/206055055/ex4/src/`:
- [x] `ir/` - All IR command classes
- [x] `ast/` - All AST node classes
- [x] `temp/` - Temporary variable factory
- [x] `symboltable/` - Symbol table
- [x] `types/` - Type system
- [x] `cfg/` - Control flow graph (if needed for liveness)

## Testing Strategy

1. **Unit testing:** Test each new IR command individually
2. **Integration testing:** Test complete AST→IR translation for:
   - String operations
   - Array operations
   - Class/object operations
   - Method calls
   - Global variable initialization
3. **Evaluation order testing:** Verify left-to-right evaluation

## Deliverables to Person B & C

1. **Complete IR representation** of the L program
2. **Documentation** of all IR commands
3. **IR format specification** for integration
4. **List of temporaries** used (for register allocation)

## Integration Points

- **To Person B:** Provide IR code with all temporaries clearly identified
- **To Person C:** Document IR command semantics for MIPS translation
- **From ex4:** Import existing IR infrastructure and extend it

## Next Steps

1. Copy ex4 source files to person_a_ir_generation workspace
2. Create new IR command classes for strings, arrays, objects
3. Implement irMe() methods in AST nodes
4. Test with simple L programs
5. Coordinate with team on IR format
