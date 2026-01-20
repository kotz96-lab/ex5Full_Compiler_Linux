# Person C: MIPS Generation - Implementation Guide

## Status: Core Infrastructure Complete ✅

### What's Implemented

✅ **MipsGenerator.java** - Output management, labels, data/text sections
✅ **RuntimeChecks.java** - Division by zero, null pointer, bounds checks
✅ **SaturationArithmetic.java** - Saturated add, sub, mul, div, neg
✅ **StringTable.java** - String literal management

### What Needs Implementation

The main translator that uses all these components to translate each of the 32+ IR commands.

## Complete IR → MIPS Translation Reference

### Integer Arithmetic (with Saturation)

#### Addition
```java
// IR: Temp_dst := Temp_t1 + Temp_t2
// Register allocation: dst=$t5, t1=$t2, t2=$t7

// MIPS:
SaturationArithmetic sat = new SaturationArithmetic(gen);
sat.emitSaturatedAdd("$t5", "$t2", "$t7");
```

#### Subtraction
```java
// IR: Temp_dst := Temp_t1 - Temp_t2
sat.emitSaturatedSub("$t5", "$t2", "$t7");
```

#### Multiplication
```java
// IR: Temp_dst := Temp_t1 * Temp_t2
sat.emitSaturatedMul("$t5", "$t2", "$t7");
```

#### Division
```java
// IR: Temp_dst := Temp_t1 / Temp_t2
RuntimeChecks checks = new RuntimeChecks(gen);
checks.emitDivByZeroCheck("$t7");  // Check divisor
sat.emitSaturatedDiv("$t5", "$t2", "$t7");
```

#### Comparisons
```java
// IR: Temp_dst := Temp_t1 < Temp_t2
// MIPS:
gen.emit("slt $t5, $t2, $t7");  // Set if less than

// IR: Temp_dst := Temp_t1 = Temp_t2
// MIPS:
gen.emit("seq $t5, $t2, $t7");  // Set if equal
// Or use:
String labelEq = gen.getFreshLabel("eq");
String labelDone = gen.getFreshLabel("eq_done");
gen.emit("li $t5, 0");                  // Assume not equal
gen.emit("bne $t2, $t7, " + labelDone);
gen.emitLabel(labelEq);
gen.emit("li $t5, 1");                  // They are equal
gen.emitLabel(labelDone);
```

#### Unary Negation
```java
// IR: Temp_dst := -Temp_t1
sat.emitSaturatedNeg("$t5", "$t2");
```

### Constants

#### Integer Constant
```java
// IR: Temp_dst := 42
gen.emit("li $t5, 42");
```

#### String Constant
```java
// IR: Temp_dst := "hello"
StringTable strings = new StringTable();
String label = strings.addString("hello");
gen.emit("la $t5, " + label);  // Load address
```

#### Nil Constant
```java
// IR: Temp_dst := NIL
gen.emit("li $t5, 0");  // nil = null pointer = 0
```

### String Operations

#### String Concatenation
```java
// IR: Temp_dst := STRING_CONCAT(Temp_s1, Temp_s2)
// Complex! Need helper function

// Pseudocode:
// 1. Calculate length of s1
// 2. Calculate length of s2
// 3. Allocate (len1 + len2 + 1) bytes
// 4. Copy s1 to new memory
// 5. Copy s2 after s1
// 6. Add null terminator
// 7. Return address in dst

// MIPS (simplified - real implementation is longer):
gen.emitComment("String concatenation");

// Calculate lengths (loop through until null)
// ... (need strlen helper)

// Allocate memory
gen.emit("addi $a0, $t_total_len, 1");  // +1 for null
gen.emit("li $v0, 9");                   // malloc
gen.emit("syscall");
gen.emit("move $t5, $v0");               // dst = allocated memory

// Copy strings
// ... (need strcpy helper)
```

#### String Equality
```java
// IR: Temp_dst := STRING_EQUAL(Temp_s1, Temp_s2)
// Compare character by character

gen.emitComment("String equality");
String labelLoop = gen.getFreshLabel("streq_loop");
String labelNotEq = gen.getFreshLabel("streq_ne");
String labelEq = gen.getFreshLabel("streq_eq");
String labelDone = gen.getFreshLabel("streq_done");

gen.emit("move $t_p1, $t2");  // pointer to s1
gen.emit("move $t_p2, $t7");  // pointer to s2

gen.emitLabel(labelLoop);
gen.emit("lb $t_c1, 0($t_p1)");  // load char from s1
gen.emit("lb $t_c2, 0($t_p2)");  // load char from s2
gen.emit("bne $t_c1, $t_c2, " + labelNotEq);  // different chars
gen.emit("beq $t_c1, $zero, " + labelEq);      // both null = equal
gen.emit("addi $t_p1, $t_p1, 1");  // increment pointers
gen.emit("addi $t_p2, $t_p2, 1");
gen.emit("j " + labelLoop);

gen.emitLabel(labelNotEq);
gen.emit("li $t5, 0");  // not equal
gen.emit("j " + labelDone);

gen.emitLabel(labelEq);
gen.emit("li $t5, 1");  // equal

gen.emitLabel(labelDone);
```

### Array Operations

#### Array Allocation
```java
// IR: Temp_dst := NEW_ARRAY(Temp_size, elemSize=4)

gen.emitComment("Array allocation");

// Calculate total bytes = 4 + (size * elemSize)
gen.emit("li $t_elem, 4");
gen.emit("mul $t_bytes, $t2, $t_elem");  // size * 4
gen.emit("addi $a0, $t_bytes, 4");       // +4 for length field
gen.emit("li $v0, 9");                    // malloc
gen.emit("syscall");
gen.emit("sw $t2, 0($v0)");               // store length at offset 0
gen.emit("move $t5, $v0");                // dst = array address
```

#### Array Access
```java
// IR: Temp_dst := ARRAY_ACCESS(Temp_arr[Temp_idx])

RuntimeChecks checks = new RuntimeChecks(gen);

gen.emitComment("Array access");

// Runtime checks
checks.emitBoundsCheck("$t2", "$t7", "$s0");  // array, index, temp

// Calculate address: array + 4 + (index * 4)
gen.emit("sll $s0, $t7, 2");        // index * 4
gen.emit("addi $s0, $s0, 4");       // + 4 (skip length)
gen.emit("add $s0, $t2, $s0");      // base + offset
gen.emit("lw $t5, 0($s0)");          // load value
```

#### Array Store
```java
// IR: ARRAY_STORE(Temp_arr[Temp_idx], Temp_val)

checks.emitBoundsCheck("$t2", "$t7", "$s0");

// Calculate address
gen.emit("sll $s0, $t7, 2");
gen.emit("addi $s0, $s0, 4");
gen.emit("add $s0, $t2, $s0");
gen.emit("sw $t5, 0($s0)");          // store value
```

#### Array Length
```java
// IR: Temp_dst := ARRAY_LENGTH(Temp_arr)

checks.emitNullCheck("$t2");
gen.emit("lw $t5, 0($t2)");  // load length from offset 0
```

### Object Operations

#### Object Allocation
```java
// IR: Temp_dst := NEW_OBJECT("ClassName", size=8)

gen.emitComment("Object allocation");
gen.emit("li $a0, 8");      // size in bytes
gen.emit("li $v0, 9");       // malloc
gen.emit("syscall");
gen.emit("move $t5, $v0");   // dst = object address

// Optional: Initialize fields to 0
gen.emit("sw $zero, 0($v0)");
gen.emit("sw $zero, 4($v0)");
```

#### Field Access
```java
// IR: Temp_dst := FIELD_ACCESS(Temp_obj, offset=4, field="x")

checks.emitFieldAccessCheck("$t2");
gen.emit("lw $t5, 4($t2)");  // load from offset
```

#### Field Store
```java
// IR: FIELD_STORE(Temp_obj, offset=4, Temp_val)

checks.emitFieldAccessCheck("$t2");
gen.emit("sw $t7, 4($t2)");  // store to offset
```

#### Method Call
```java
// IR: Temp_dst := METHOD_CALL(Temp_obj, "methodName", [args...])

// For now, treat like function call
// Later: add vtable support for dynamic dispatch

checks.emitNullCheck("$t2");  // check object not null

// Push arguments (right to left)
// ...

// Call method
gen.emit("jal method_ClassName_methodName");

// Get return value
gen.emit("move $t5, $v0");
```

### Control Flow

#### Label
```java
// IR: Label_start:
gen.emitLabel("Label_start");
```

#### Jump
```java
// IR: JUMP Label_end
gen.emit("j Label_end");
```

#### Conditional Jump
```java
// IR: JUMP_IF_EQ_ZERO Temp_cond, Label_else
gen.emit("beq $t2, $zero, Label_else");
```

#### Return
```java
// IR: RETURN Temp_val
gen.emit("move $v0, $t2");  // return value in $v0

// Function epilogue (if needed)
// restore $ra, $fp
// jr $ra
gen.emit("jr $ra");
```

#### Return Void
```java
// IR: RETURN_VOID

// Function epilogue
gen.emit("jr $ra");
```

### Variable Operations

#### Load
```java
// IR: Temp_dst := LOAD(var_name)
gen.emit("lw $t5, var_name");  // load from global/stack
```

#### Store
```java
// IR: STORE(var_name, Temp_src)
gen.emit("sw $t2, var_name");  // store to global/stack
```

### System Calls

#### PrintInt
```java
// Call PrintInt(value in temp)
gen.emit("move $a0, $t2");   // arg in $a0
gen.emit("li $v0, 1");        // syscall 1 = print int
gen.emit("syscall");

// Print space after
gen.emit("li $a0, 32");       // ASCII space
gen.emit("li $v0, 11");       // syscall 11 = print char
gen.emit("syscall");
```

#### PrintString
```java
// Call PrintString(address in temp)
gen.emit("move $a0, $t2");   // string address in $a0
gen.emit("li $v0, 4");        // syscall 4 = print string
gen.emit("syscall");
```

## Main Translator Structure

```java
public class MipsTranslator {
    private MipsGenerator gen;
    private RuntimeChecks checks;
    private SaturationArithmetic sat;
    private StringTable strings;
    private RegisterAllocation regAlloc;

    public void translate(List<IrCommand> commands, RegisterAllocation allocation) {
        this.regAlloc = allocation;

        // Collect strings
        collectStrings(commands);

        // Emit strings to data section
        strings.emitAllStrings(gen);

        // Emit text section
        for (IrCommand cmd : commands) {
            translateCommand(cmd);
        }

        // Emit error handlers
        gen.emitAllErrorHandlers();
    }

    private void translateCommand(IrCommand cmd) {
        if (cmd instanceof IrCommandBinopAddIntegers) {
            translateAdd((IrCommandBinopAddIntegers) cmd);
        }
        else if (cmd instanceof IrCommandBinopSubIntegers) {
            translateSub((IrCommandBinopSubIntegers) cmd);
        }
        // ... handle all 32+ IR command types
    }

    private void translateAdd(IrCommandBinopAddIntegers cmd) {
        String dst = regAlloc.getRegister(cmd.dst);
        String src1 = regAlloc.getRegister(cmd.t1);
        String src2 = regAlloc.getRegister(cmd.t2);

        sat.emitSaturatedAdd(dst, src1, src2);
    }

    // ... implement translate method for each IR command type
}
```

## Testing Pattern

```java
// For each feature:

// 1. Create simple L program
void main() {
    int x := 5;
    int y := 10;
    int z := x + y;
    PrintInt(z);
}

// 2. Translate to IR (Person A)
Temp_1 := 5
Temp_2 := 10
Temp_3 := Temp_1 + Temp_2
CALL PrintInt(Temp_3)

// 3. Allocate registers (Person B)
Temp_1 → $t0
Temp_2 → $t1
Temp_3 → $t0  # reuse

// 4. Generate MIPS (Person C)
li $t0, 5
li $t1, 10
add $t0, $t0, $t1
# saturation checks
move $a0, $t0
li $v0, 1
syscall

// 5. Run in SPIM
spim -file output.s
# Expected: 15
```

## File Structure

```
src/mips/
├── MipsGenerator.java        ✅ Complete
├── RuntimeChecks.java         ✅ Complete
├── SaturationArithmetic.java  ✅ Complete
├── StringTable.java           ✅ Complete
└── MipsTranslator.java        ⏳ Need to implement
```

## Summary

**Core infrastructure is COMPLETE:**
- ✅ Output management
- ✅ Runtime checks
- ✅ Saturation arithmetic
- ✅ String management

**Remaining work:**
- Implement MipsTranslator class
- Add translateXXX() method for each of 32+ IR command types
- Test with real L programs
- Debug and fix issues

**Estimated remaining effort:** 10-15 hours to implement all translations and test.

The hard algorithmic work is done - now it's systematic translation following the patterns above!
