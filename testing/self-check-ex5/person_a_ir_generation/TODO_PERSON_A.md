# Person A - Detailed Task List

## Priority 1: New IR Commands for Strings

### 1.1 String Constants
**File:** `src/ir/IrCommandConstString.java`
```java
// Create IR command for string constants
// Stores string literal in data section
// Returns temporary holding address
```

**Pattern:**
```
TEMP t1 = "hello world"
```

### 1.2 String Concatenation
**File:** `src/ir/IrCommandStringConcat.java`
```java
// Allocate new string on heap
// Copy first string
// Append second string
// Null terminate
// Return address in temporary
```

**Pattern:**
```
TEMP t3 = STRING_CONCAT(t1, t2)
// Must allocate heap memory for result
```

### 1.3 String Equality
**File:** `src/ir/IrCommandStringEqual.java`
```java
// Compare string contents character by character
// Return 1 if equal, 0 otherwise
```

**Pattern:**
```
TEMP t3 = STRING_EQUAL(t1, t2)
// Returns 0 or 1
```

---

## Priority 2: New IR Commands for Arrays

### 2.1 Array Allocation
**File:** `src/ir/IrCommandNewArray.java`
```java
// Allocate array on heap
// Store size in first word
// Initialize elements
// Return address
```

**Pattern:**
```
TEMP t_array = NEW_ARRAY(t_size, element_size)
// Allocates: [size][elem0][elem1]...[elemN-1]
```

### 2.2 Array Element Access (Load)
**File:** `src/ir/IrCommandArrayAccess.java`
```java
// Load from array[index]
// Check bounds (index >= 0 && index < array.length)
// Calculate offset: address + 4 + (index * element_size)
// Return value in temporary
```

**Pattern:**
```
TEMP t_value = ARRAY_ACCESS(t_array, t_index)
// Must generate bounds check!
```

### 2.3 Array Element Store
**File:** `src/ir/IrCommandArrayStore.java`
```java
// Store to array[index]
// Check bounds
// Calculate offset
// Store value
```

**Pattern:**
```
ARRAY_STORE(t_array, t_index, t_value)
```

### 2.4 Array Length
**File:** `src/ir/IrCommandArrayLength.java`
```java
// Load length from first word of array
```

**Pattern:**
```
TEMP t_len = ARRAY_LENGTH(t_array)
// Loads from offset 0
```

---

## Priority 3: New IR Commands for Classes/Objects

### 3.1 Object Allocation
**File:** `src/ir/IrCommandNewObject.java`
```java
// Allocate object on heap
// Initialize vtable pointer (if using vtables)
// Initialize data members with constants
// Return address
```

**Pattern:**
```
TEMP t_obj = NEW_OBJECT(class_name, size)
```

### 3.2 Field Access (Load)
**File:** `src/ir/IrCommandFieldAccess.java`
```java
// Load from object.field
// Calculate offset based on field position
// Return value in temporary
```

**Pattern:**
```
TEMP t_value = FIELD_ACCESS(t_object, field_offset)
```

### 3.3 Field Store
**File:** `src/ir/IrCommandFieldStore.java`
```java
// Store to object.field
// Calculate offset
// Store value
```

**Pattern:**
```
FIELD_STORE(t_object, field_offset, t_value)
```

### 3.4 Method Call
**File:** `src/ir/IrCommandMethodCall.java`
```java
// Call object method
// May need vtable lookup for dynamic dispatch
// Pass object as implicit first parameter
```

**Pattern:**
```
TEMP t_result = METHOD_CALL(t_object, method_name, [args...])
```

---

## Priority 4: Control Flow & Returns

### 4.1 Return with Value
**File:** `src/ir/IrCommandReturn.java`
```java
// Return value from function
// Place value in return register/location
```

**Pattern:**
```
RETURN t_value
```

### 4.2 Void Return
**File:** `src/ir/IrCommandReturnVoid.java`
```java
// Return from void function
```

**Pattern:**
```
RETURN_VOID
```

### 4.3 Nil Constant
**File:** `src/ir/IrCommandNilConst.java`
```java
// Load nil (null pointer = 0) into temporary
```

**Pattern:**
```
TEMP t1 = NIL
```

---

## Priority 5: Extend AST irMe() Methods

### 5.1 Simple Expressions
**Files to modify:**

1. `src/ast/AstSimpleExpString.java`
   - Add irMe() to generate IrCommandConstString
   - Return temporary with string address

2. `src/ast/AstSimpleExpNil.java`
   - Add irMe() to generate IrCommandNilConst
   - Return temporary with value 0

### 5.2 Variable References

1. `src/ast/AstVarSubscript.java` - Array subscript
   ```java
   public Temp irMe() {
       Temp t_array = var.irMe();
       Temp t_index = subscript.irMe();
       Temp t_result = TempFactory.getInstance().getFreshTemp();
       Ir.getInstance().AddIrCommand(
           new IrCommandArrayAccess(t_result, t_array, t_index)
       );
       return t_result;
   }
   ```

2. `src/ast/AstVarField.java` - Field access
   ```java
   public Temp irMe() {
       Temp t_object = var.irMe();
       int field_offset = /* calculate from symbol table */;
       Temp t_result = TempFactory.getInstance().getFreshTemp();
       Ir.getInstance().AddIrCommand(
           new IrCommandFieldAccess(t_result, t_object, field_offset)
       );
       return t_result;
   }
   ```

### 5.3 Statements

1. `src/ast/AstStmtReturn.java`
   ```java
   public Temp irMe() {
       if (exp != null) {
           Temp t_value = exp.irMe();
           Ir.getInstance().AddIrCommand(new IrCommandReturn(t_value));
       } else {
           Ir.getInstance().AddIrCommand(new IrCommandReturnVoid());
       }
       return null;
   }
   ```

2. `src/ast/AstStmtAssign.java` - Extend for array/field assignments
   ```java
   public Temp irMe() {
       Temp t_value = exp.irMe(); // Evaluate RHS first

       if (var instanceof AstVarSimple) {
           // Existing code for simple vars
       } else if (var instanceof AstVarSubscript) {
           AstVarSubscript arrVar = (AstVarSubscript) var;
           Temp t_array = arrVar.var.irMe();
           Temp t_index = arrVar.subscript.irMe();
           Ir.getInstance().AddIrCommand(
               new IrCommandArrayStore(t_array, t_index, t_value)
           );
       } else if (var instanceof AstVarField) {
           AstVarField fieldVar = (AstVarField) var;
           Temp t_object = fieldVar.var.irMe();
           int offset = /* calculate */;
           Ir.getInstance().AddIrCommand(
               new IrCommandFieldStore(t_object, offset, t_value)
           );
       }
       return null;
   }
   ```

### 5.4 Expressions

1. `src/ast/AstExpBinop.java` - Extend for strings
   - Check operand types
   - If string + string: use IrCommandStringConcat
   - If string = string: use IrCommandStringEqual
   - Keep existing integer operations

### 5.5 Object/Array Creation

Create new AST nodes or extend existing ones:
- Object creation: `new ClassName`
- Array creation: `new Type[size]`

---

## Priority 6: Global Variable Initialization

### 6.1 Modify Program Entry Point
**File:** `src/ast/AstProgram.java`

Generate IR that:
1. Initializes all global variables in order
2. Evaluates initialization expressions (can be non-constant!)
3. Stores results in global variable locations
4. Then calls main()

**Pattern:**
```
// Generated IR:
LABEL _init_globals
  TEMP t1 = <init_exp_for_global_1>
  STORE global_1, t1
  TEMP t2 = <init_exp_for_global_2>
  STORE global_2, t2
  ...
  JUMP _main

LABEL _main
  <main function code>
```

---

## Priority 7: Evaluation Order

### 7.1 Binary Operations
**File:** `src/ast/AstExpBinop.java`

Ensure left side is evaluated BEFORE right side:
```java
public Temp irMe() {
    Temp t_left = exp_left.irMe();  // LEFT FIRST!
    Temp t_right = exp_right.irMe(); // RIGHT SECOND!
    Temp t_result = TempFactory.getInstance().getFreshTemp();
    // Generate appropriate binop command
    return t_result;
}
```

### 7.2 Function Call Arguments
**File:** `src/ast/AstExpCall.java` or similar

Evaluate arguments left to right:
```java
public Temp irMe() {
    List<Temp> arg_temps = new ArrayList<>();
    for (AstExp arg : args) {
        arg_temps.add(arg.irMe()); // Evaluate in order!
    }
    Temp t_result = TempFactory.getInstance().getFreshTemp();
    Ir.getInstance().AddIrCommand(
        new IrCommandCallFunc(t_result, func_name, arg_temps)
    );
    return t_result;
}
```

---

## Testing Checklist

- [ ] Test string concatenation
- [ ] Test string equality
- [ ] Test array allocation
- [ ] Test array access (with bounds checking)
- [ ] Test array assignment
- [ ] Test object creation
- [ ] Test field access
- [ ] Test field assignment
- [ ] Test method calls
- [ ] Test return statements
- [ ] Test global variable initialization order
- [ ] Test left-to-right evaluation of function arguments
- [ ] Test left-to-right evaluation of binary operations

---

## Questions to Coordinate with Team

1. **IR Format:** What format should we use to pass IR to Person B?
   - LinkedList of IrCommand objects?
   - Text format?
   - Serialized objects?

2. **Register Allocation Interface:** How does Person B return register mappings?
   - Map<Temp, String> mapping temps to register names?

3. **MIPS Translation:** What additional info does Person C need?
   - String literal locations?
   - Label addresses?
   - Function signatures?

4. **Runtime Checks:** Should Person A generate IR for runtime checks, or should Person C add them during MIPS translation?
   - Division by zero
   - Null pointer checks
   - Array bounds checks

---

## Files Created/Modified Summary

**New IR Command Files (11):**
1. IrCommandConstString.java
2. IrCommandStringConcat.java
3. IrCommandStringEqual.java
4. IrCommandNewArray.java
5. IrCommandArrayAccess.java
6. IrCommandArrayStore.java
7. IrCommandArrayLength.java
8. IrCommandNewObject.java
9. IrCommandFieldAccess.java
10. IrCommandFieldStore.java
11. IrCommandMethodCall.java
12. IrCommandReturn.java
13. IrCommandReturnVoid.java
14. IrCommandNilConst.java

**Modified AST Files (~10):**
1. AstSimpleExpString.java
2. AstSimpleExpNil.java
3. AstVarSubscript.java
4. AstVarField.java
5. AstStmtReturn.java
6. AstStmtAssign.java
7. AstExpBinop.java
8. AstProgram.java
9. AstDecVar.java
10. AstExpCall.java (or create AstExpMethodCall.java)
