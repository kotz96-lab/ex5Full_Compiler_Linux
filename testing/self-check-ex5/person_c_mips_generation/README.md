# Person C: MIPS Code Generation - Work Plan

## Overview
Translate IR commands to MIPS assembly, implementing runtime checks, saturation arithmetic, and system calls.

## Responsibility
Take the IR from Person A and register allocation from Person B, and generate executable MIPS assembly code.

## What Person C Needs to Do

### 1. MIPS Generator Infrastructure
Create the framework for MIPS code generation:
- Output file management
- Data section for strings
- Text section for code
- Label management
- Helper methods for common patterns

### 2. Runtime Checks ⚠️ (CRITICAL)
Implement three required runtime checks:

**Division by Zero:**
```assembly
# Before: div $t0, $t1, $t2
beq $t2, $zero, error_div_by_zero
div $t0, $t1, $t2
```

**Null Pointer Dereference:**
```assembly
# Before accessing array[index] or object.field
beq $t_array, $zero, error_null_pointer
# Then do the access
```

**Array Bounds Check:**
```assembly
# Check: index >= 0 && index < array.length
bltz $t_index, error_bounds        # index < 0
lw $t_len, 0($t_array)             # load array length
bge $t_index, $t_len, error_bounds # index >= length
# Then do the access
```

### 3. Saturation Arithmetic 🔢
Integers are bounded: -32768 to 32767

**For each arithmetic operation:**
```assembly
# Example: add with saturation
add $t0, $t1, $t2

# Check overflow (result > 32767)
li $t_max, 32767
bgt $t0, $t_max, saturate_max

# Check underflow (result < -32768)
li $t_min, -32768
blt $t0, $t_min, saturate_min

# Normal case
j done

saturate_max:
    li $t0, 32767
    j done

saturate_min:
    li $t0, -32768

done:
```

### 4. Translate IR Commands to MIPS
For each of the 32+ IR command types, implement MIPS translation.

**Example: Integer Addition**
```java
// IR: Temp_5 := Temp_3 + Temp_4
// Registers: Temp_3=$t2, Temp_4=$t7, Temp_5=$t1

// MIPS:
add $t1, $t2, $t7
# Then apply saturation checks
```

### 5. System Calls
Implement using MIPS syscalls:

**PrintInt:**
```assembly
# PrintInt(value in $a0)
li $v0, 1
syscall
# Print space
li $a0, 32      # ASCII space
li $v0, 11      # print character
syscall
```

**PrintString:**
```assembly
# PrintString(address in $a0)
li $v0, 4
syscall
```

**Malloc:**
```assembly
# Allocate size bytes (size in $a0)
li $v0, 9
syscall
# Address returned in $v0
```

**Exit:**
```assembly
li $v0, 10
syscall
```

### 6. String Data Section
Collect all string literals and emit in .data section:

```assembly
.data
string_0: .asciiz "hello"
string_1: .asciiz "world"

.text
main:
    # Code here
```

### 7. Function Prologue/Epilogue
Handle function calls:

**Prologue:**
```assembly
# Save return address and frame pointer
addi $sp, $sp, -8
sw $ra, 4($sp)
sw $fp, 0($sp)
move $fp, $sp
```

**Epilogue:**
```assembly
# Restore and return
move $sp, $fp
lw $fp, 0($sp)
lw $ra, 4($sp)
addi $sp, $sp, 8
jr $ra
```

## Input from Person A & B

**From Person A:**
- IR commands via `Ir.getInstance().getCommands()`
- String literals to place in data section

**From Person B:**
- Register allocation via `RegisterAllocation.getRegister(Temp)`
- Maps each temp to a register like "$t5"

## Output

Single MIPS assembly file with:
1. `.data` section with strings
2. `.text` section with code
3. Runtime error handlers
4. Exit syscall at end

## Files to Create

### Core MIPS Generation
1. **MipsGenerator.java** - Main MIPS code generator
   - Output management
   - Emit methods (data, text, label, instruction)
   - String literal collection

2. **MipsTranslator.java** - Translates IR to MIPS
   - For each IR command type, generate MIPS
   - Uses register allocation
   - Calls runtime check helpers

3. **RuntimeChecks.java** - Runtime check generators
   - `emitDivByZeroCheck(register)`
   - `emitNullCheck(register)`
   - `emitBoundsCheck(array, index)`

4. **SaturationArithmetic.java** - Saturation helpers
   - `emitSaturatedAdd(dst, src1, src2)`
   - `emitSaturatedSub(dst, src1, src2)`
   - `emitSaturatedMul(dst, src1, src2)`
   - `emitSaturatedDiv(dst, src1, src2)`

### Helper Classes
5. **StringTable.java** - Manage string literals
   - Collect strings from IR
   - Assign labels (string_0, string_1, ...)
   - Emit .data section

6. **LabelGenerator.java** - Generate unique labels
   - Fresh labels for jumps, checks, etc.

## IR Command → MIPS Translation Guide

### Integer Operations
```
// IR: Temp_dst := Temp_t1 + Temp_t2
// MIPS:
add $dst, $t1, $t2
# Apply saturation

// IR: Temp_dst := Temp_t1 - Temp_t2
sub $dst, $t1, $t2
# Apply saturation

// IR: Temp_dst := Temp_t1 * Temp_t2
mul $dst, $t1, $t2
# Apply saturation

// IR: Temp_dst := Temp_t1 / Temp_t2
# Check division by zero first!
beq $t2, $zero, error_div_by_zero
div $dst, $t1, $t2
# Apply saturation
```

### String Operations
```
// IR: Temp_dst := "hello"
// MIPS:
la $dst, string_label  # Load address of string

// IR: Temp_dst := STRING_CONCAT(Temp_s1, Temp_s2)
// MIPS:
# 1. Calculate lengths
# 2. Allocate memory (len1 + len2 + 1 bytes)
# 3. Copy s1 to new memory
# 4. Copy s2 after s1
# 5. Add null terminator
# 6. Return address in $dst

// IR: Temp_dst := STRING_EQUAL(Temp_s1, Temp_s2)
// MIPS:
# Loop comparing characters until null or mismatch
# Return 1 if equal, 0 otherwise
```

### Array Operations
```
// IR: Temp_dst := NEW_ARRAY(Temp_size, elemSize=4)
// MIPS:
# size_in_bytes = 4 + (size * 4)
lw $t_size, ... # get size
li $t0, 4
mul $t0, $t_size, $t0  # size * 4
addi $a0, $t0, 4       # + 4 for length
li $v0, 9              # malloc
syscall
sw $t_size, 0($v0)     # store length
move $dst, $v0

// IR: Temp_dst := ARRAY_ACCESS(Temp_arr[Temp_idx])
// MIPS:
# Check null pointer
beq $arr, $zero, error_null_pointer
# Check bounds
bltz $idx, error_bounds
lw $t_len, 0($arr)
bge $idx, $t_len, error_bounds
# Calculate address: arr + 4 + (idx * 4)
sll $t_offset, $idx, 2  # idx * 4
addi $t_offset, $t_offset, 4
add $t_addr, $arr, $t_offset
lw $dst, 0($t_addr)

// IR: ARRAY_STORE(Temp_arr[Temp_idx], Temp_val)
// Similar to access, but store instead of load
```

### Object Operations
```
// IR: Temp_dst := NEW_OBJECT("ClassName", size=8)
// MIPS:
li $a0, 8       # size in bytes
li $v0, 9       # malloc
syscall
move $dst, $v0
# Initialize fields to 0 if needed

// IR: Temp_dst := FIELD_ACCESS(Temp_obj, offset=4)
// MIPS:
# Check null
beq $obj, $zero, error_null_pointer
lw $dst, 4($obj)  # load from offset

// IR: FIELD_STORE(Temp_obj, offset=4, Temp_val)
// MIPS:
# Check null
beq $obj, $zero, error_null_pointer
sw $val, 4($obj)  # store to offset
```

### Control Flow
```
// IR: Label_start:
// MIPS:
Label_start:

// IR: JUMP Label_end
// MIPS:
j Label_end

// IR: JUMP_IF_EQ_ZERO Temp_cond, Label_else
// MIPS:
beq $cond, $zero, Label_else

// IR: RETURN Temp_val
// MIPS:
move $v0, $val  # return value in $v0
# Epilogue
# jr $ra

// IR: RETURN_VOID
// MIPS:
# Epilogue
# jr $ra
```

## Error Message Handlers

```assembly
.data
msg_div_zero: .asciiz "Illegal Division By Zero"
msg_null_ptr: .asciiz "Invalid Pointer Dereference"
msg_bounds: .asciiz "Access Violation"

.text
error_div_by_zero:
    la $a0, msg_div_zero
    li $v0, 4
    syscall
    li $v0, 10
    syscall

error_null_pointer:
    la $a0, msg_null_ptr
    li $v0, 4
    syscall
    li $v0, 10
    syscall

error_bounds:
    la $a0, msg_bounds
    li $v0, 4
    syscall
    li $v0, 10
    syscall
```

## Testing Strategy

### Test 1: Simple Arithmetic
```
void main() {
    int x := 5;
    int y := 10;
    int z := x + y;
    PrintInt(z);
}
```
Expected output: `15 `

### Test 2: Saturation
```
void main() {
    int x := 32767;
    int y := 1;
    int z := x + y;  # Should saturate to 32767
    PrintInt(z);
}
```
Expected output: `32767 `

### Test 3: Division by Zero
```
void main() {
    int x := 5;
    int y := 0;
    int z := x / y;  # Should print error and exit
}
```
Expected output: `Illegal Division By Zero`

### Test 4: Array Bounds
```
array IntArray = int[];
void main() {
    IntArray arr := new int[5];
    arr[10] := 42;  # Should print error and exit
}
```
Expected output: `Access Violation`

## Deliverables

1. **Working MIPS generator** that:
   - Translates all IR commands to MIPS
   - Implements runtime checks
   - Implements saturation arithmetic
   - Handles strings, arrays, objects
   - Produces runnable MIPS code

2. **Documentation** explaining:
   - How each IR command is translated
   - Runtime check implementation
   - Saturation arithmetic approach

3. **Test cases** showing:
   - Normal execution
   - Runtime errors
   - Edge cases (saturation, bounds, etc.)

## Estimated Effort
30-40 hours (longest part, but straightforward)

---

**Status:** Ready to start
**Next:** Implement MIPS generator infrastructure
