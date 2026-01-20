# Compiler Project - Lessons Learned & Critical Debugging Guide

**Status: 8/26 tests passing (30.8%)**
**Last Updated: 2026-01-18**

---

## 🎯 Current Progress

### Passing Tests (8/26)
- TEST_04 (Matrices) - Arrays work
- TEST_06 (Strings) - String concatenation fixed ✓
- TEST_11 (Precedence) - Arithmetic expressions work
- TEST_13 (Overflow) - Saturated arithmetic works
- TEST_14 (Many Local Variables) - Local vars work
- TEST_17 (Global Variables) - Globals work
- TEST_18 - Basic functionality
- TEST_25 - Basic functionality

### Failing Tests Categorized

**Timeout Issues (7 tests - Recursion/Loops):**
- TEST_01 (Print Primes) - Function calls in loops
- TEST_02 (Bubble Sort) - Nested loops
- TEST_03 (Merge Lists) - Recursion with field access
- TEST_10 (Tree) - Recursive tree operations
- TEST_12 (Fibonacci) - Recursion
- TEST_19 - Unknown timeout
- TEST_22 - Unknown timeout

**Other Failures (11 tests):**
- TEST_05, TEST_07, TEST_08, TEST_09, TEST_15, TEST_16, TEST_20, TEST_21, TEST_23, TEST_24, TEST_26

---

## 🔧 Major Fixes Implemented

### 1. Register Clobbering in Equality Checks
**Problem:** When comparing `a == b` where result goes to register that overlaps with operands, the operand gets overwritten before comparison.

**Example:**
```mips
# WRONG - overwrites $t1 before comparing
lw $t1, l_17        # Load operand
li $t2, 0           # Load nil
li $t1, 0           # Overwrite operand! ❌
bne $t1, $t2, done  # Compare 0 with 0 (always equal)
```

**Fix in [MipsTranslator.java:322-355](ex5/src/mips/MipsTranslator.java#L322-L355):**
```java
private void translateEq(IrCommandBinopEqIntegers cmd) {
    String dst = getReg(cmd.dst);
    String src1 = getReg(cmd.t1);
    String src2 = getReg(cmd.t2);

    // Save operands if dst overlaps
    if (dst.equals(src1) || dst.equals(src2)) {
        if (dst.equals(src1)) {
            gen.emit(String.format("move $t8, %s", src1));
            src1 = "$t8";
        }
        if (dst.equals(src2)) {
            gen.emit(String.format("move $t9, %s", src2));
            src2 = "$t9";
        }
    }

    gen.emit(String.format("li %s, 0", dst), "assume not equal");
    gen.emit(String.format("bne %s, %s, %s", src1, src2, labelDone));
    gen.emit(String.format("li %s, 1", dst), "they are equal");
    gen.emitLabel(labelDone);
}
```

**Lesson:** Always save operands BEFORE overwriting the destination register in binary operations.

---

### 2. Field Assignment Symbol Table Scope Issues
**Problem:** During IR generation, calling `semantMe()` again to get field offsets fails because symbol table scopes have been cleared after semantic analysis.

**Error Messages:**
```
variable a not found
variable l3 not found
```

**Fix - Cache Offsets During Semantic Analysis:**

**[AstVarField.java:14-15, 96-97](ex5/src/ast/AstVarField.java):**
```java
public class AstVarField extends AstVar {
    private int cachedOffset = -1;  // Cache field offset
    private String cachedClassName = null;

    public Type semantMe() throws SemanticException {
        // ... find field type ...

        // Cache offset for IR generation
        cachedOffset = classType.getFieldOffset(fieldName);
        cachedClassName = classType.name;

        return fieldType;
    }

    public int getCachedOffset() {
        return cachedOffset;
    }
}
```

**[AstStmtAssign.java:155-165](ex5/src/ast/AstStmtAssign.java):**
```java
else if (var instanceof AstVarField fieldVar) {
    Temp t_object = fieldVar.var.irMe();

    // Use cached offset instead of calling semantMe()
    int fieldOffset = fieldVar.getCachedOffset();

    Ir.getInstance().AddIrCommand(
        new IrCommandFieldStore(t_object, fieldOffset, src, fieldVar.fieldName)
    );
}
```

**Lesson:** Semantic information needed during IR generation must be cached during semantic analysis phase. Don't re-run semantic analysis during IR generation.

---

### 3. Reserved Register Usage ($at)
**Problem:** MIPS reserves register `$at` (assembler temporary) for assembler use. Using it causes errors.

**Error:**
```
spim: (parser) Register 1 is reserved for assembler on line 99
  lb $at, 0($s2)
```

**Fix in [MipsTranslator.java:428, 439, 457, 468](ex5/src/mips/MipsTranslator.java):**
```java
// WRONG
gen.emit(String.format("lb $at, 0(%s)", ptr));

// CORRECT
gen.emit(String.format("lb $t8, 0(%s)", ptr));
```

**Lesson:** Never use `$at` register directly. Use `$t8` or `$t9` for temporary operations.

---

### 4. String Quote Stripping
**Problem:** String literals stored with surrounding quotes: `"\"Having\""` instead of `"Having"`

**Fix in [AstSimpleExpString.java:58-73](ex5/src/ast/AstSimpleExpString.java):**
```java
public Temp irMe() {
    Temp t = TempFactory.getInstance().getFreshTemp();

    // Strip surrounding quotes
    String strValue = value;
    if (strValue.startsWith("\"") && strValue.endsWith("\"")
        && strValue.length() >= 2) {
        strValue = strValue.substring(1, strValue.length() - 1);
    }

    Ir.getInstance().AddIrCommand(new IrCommandConstString(t, strValue));
    return t;
}
```

**Lesson:** AST node values may include delimiters that need stripping before code generation.

---

### 5. Null Register Allocation
**Problem:** Null temporaries were generating `null` in MIPS code instead of `$zero`.

**Fix in [MipsTranslator.java:106-113](ex5/src/mips/MipsTranslator.java):**
```java
private String getReg(Temp t) {
    if (t == null) {
        return "$zero";
    }
    String reg = regAlloc.getRegister(t);
    return (reg == null) ? "$zero" : reg;
}
```

**Lesson:** Always handle null/special cases in register allocation with proper MIPS equivalents.

---

## 🚨 Critical Workflow & Infrastructure Issues

### Self-Check Infrastructure

**CRITICAL:** The self-check extracts from `123456789.zip` EVERY time, overwriting any manual fixes!

**Workflow:**
```bash
# 1. Make fixes in main ex5 directory
cd /home/student/comp/ex5
# Edit source files...

# 2. Rebuild
make

# 3. Test manually
java -jar COMPILER self-check-ex5/tests/TEST_XX.txt /tmp/test.s
spim -file /tmp/test.s

# 4. Update the zip file
cd /home/student/comp
rm 123456789.zip
cd ex5
zip -r ../123456789.zip . -x "*.git/*" "self-check-ex5/*"

# 5. Run self-check
cd self-check-ex5
python3 self-check.py
```

**Lessons:**
1. **Never** edit files in `self-check-ex5/ex5/` directly - they get overwritten
2. **Always** work in `/home/student/comp/ex5/`
3. **Always** update the zip file after making fixes
4. The zip must have `ex5/` directory structure inside it

---

## 🔍 Debugging Techniques That Worked

### 1. Manual Test Execution
```bash
# Compile
java -jar COMPILER path/to/test.txt /tmp/output.s 2>&1

# Check for specific errors
grep "error\|ERROR\|Exception"
grep "lb.*\$at"  # Check for reserved register
grep "p_10"      # Check for missing variables

# Run in SPIM
spim -file /tmp/output.s 2>&1

# Compare output
diff expected_output.txt <(spim -file /tmp/output.s 2>&1)
```

### 2. Check Compiled Class Files
```bash
# Extract from jar
jar xf COMPILER mips/MipsTranslator.class

# Check actual strings in compiled code
strings mips/MipsTranslator.class | grep "lb"

# Verify source was compiled
ls -la src/mips/MipsTranslator.java bin/mips/MipsTranslator.class
```

### 3. Add Debug Output (then remove before final)
```java
System.err.println("DEBUG: Processing " + fieldName);
System.err.println("DEBUG: Offset = " + offset);
```

**Remember:** Remove debug output before creating final zip!

---

## 🎓 Architecture Insights

### Critical Architecture Overview

**Three-Person Team Structure:**
- **Person A (IR Generation):** AST → Intermediate Representation (IR commands)
  - Located in: `ex5/src/ast/`, `ex5/src/ir/`
  - Generates IR commands like `IrCommandStore`, `IrCommandFieldStore`, `IrCommandBinopEqIntegers`

- **Person B (Register Allocation):** Assigns temporaries to MIPS registers
  - Located in: `ex5/src/regalloc/` (if exists) or within compilation pipeline
  - Uses liveness analysis and graph coloring
  - **BUG POTENTIAL:** May allocate null temps incorrectly (fixed with `getReg()` helper)

- **Person C (MIPS Generation):** IR commands → MIPS assembly
  - Located in: `ex5/src/mips/`
  - `MipsTranslator.java` - Main translation logic
  - `MipsGenerator.java` - Assembly output generation
  - `StringTable.java` - String constant management

**Compilation Pipeline:**
```
Source Code (.txt)
    ↓
Lexer/Parser → AST
    ↓ [Person A]
Semantic Analysis → Type Checking
    ↓
IR Generation → IR Commands
    ↓ [Person B]
Register Allocation → Temp → Register mapping
    ↓ [Person C]
MIPS Generation → Assembly (.s)
    ↓
SPIM → Execution
```

### Current Limitations

**1. No Stack-Based Locals (CRITICAL LIMITATION)**
- All variables (including function parameters and locals) are allocated as globals in `.data`
- Variable names include offset: `variable_<offset>` (e.g., `l1_8`, `p_10`)
- This breaks recursion and nested function calls
- Affects: TEST_01, TEST_02, TEST_03, TEST_10, TEST_12, etc.

**Example Problem:**
```java
void MergeLists(IntList l1, IntList l2) {
    IntList l3 := l1;  // l3_11 in .data
    l3.tail := MergeLists(l1.tail, l2);  // Recursive call overwrites global l3_11!
    return l3;
}
```

**To Fix (Major Architecture Change):**
- Implement stack frames
- Allocate locals on stack
- Save/restore $ra for nested calls
- Requires changes to IR generation and MIPS translation

**2. No Method Dispatch**
- Method calls on objects not implemented
- Virtual method dispatch not working
- Affects: TEST_05, TEST_07, TEST_16

### What Works Well

**1. Expression Evaluation**
- Arithmetic expressions ✓
- Binary operations ✓
- Saturated arithmetic ✓

**2. Array Operations**
- Array allocation ✓
- Array access ✓
- Array storage ✓

**3. String Operations**
- String constants ✓
- String concatenation ✓
- PrintString ✓

**4. Basic Control Flow**
- If statements ✓
- While loops (without recursion) ✓

---

## 📋 Next Steps Priority

### High Priority (Low Hanging Fruit)
1. **TEST_05 (Classes)** - Check what's failing, might be simple
2. **TEST_20, TEST_21** - Non-timeout failures, check error messages
3. **TEST_08, TEST_09 (Access Violation)** - Should test null checks/bounds

### Medium Priority
1. **Method Dispatch** - Needed for TEST_07, TEST_16
2. **Error Handling** - For TEST_08, TEST_09

### Low Priority (Requires Major Changes)
1. **Stack-Based Locals** - For recursion tests
2. **Advanced Class Features** - For complex OOP tests

---

## 🔧 Quick Reference Commands

```bash
# Navigate to project
cd /home/student/comp/ex5

# Build
make

# Test single file
java -jar COMPILER self-check-ex5/tests/TEST_XX.txt /tmp/test.s
spim -file /tmp/test.s

# Run full self-check
cd self-check-ex5
python3 self-check.py 2>&1 | grep -E "OK$|Total|Passed"

# Update zip and test
cd /home/student/comp
rm 123456789.zip && cd ex5 && zip -r ../123456789.zip . -x "*.git/*" "self-check-ex5/*"
cd self-check-ex5 && python3 self-check.py 2>&1 | tail -30

# Git
git add -A
git commit -m "Description"
git push origin master
```

---

## 📊 File Locations Reference

**Main Source:**
- `/home/student/comp/ex5/src/` - All source files
- `/home/student/comp/ex5/COMPILER` - Built jar

**Self-Check:**
- `/home/student/comp/ex5/self-check-ex5/tests/` - Test cases
- `/home/student/comp/ex5/self-check-ex5/expected_output/` - Expected results
- `/home/student/comp/123456789.zip` - **CRITICAL** submission zip

**Modified Files (Current Session):**
1. `ex5/src/mips/MipsTranslator.java` - Equality check, $at fix, getReg() helper
2. `ex5/src/ast/AstVarField.java` - Field offset caching
3. `ex5/src/ast/AstStmtAssign.java` - Field assignment handling
4. `ex5/src/ast/AstSimpleExpString.java` - String quote stripping
5. `ex5/src/symboltable/SymbolTable.java` - PrintString parameter fix

---

## 💡 Tips for Next Session

### Start Here
1. Read this entire document first
2. Check current status: `cd /home/student/comp/ex5/self-check-ex5 && python3 self-check.py 2>&1 | grep -E "OK$|Total|Passed"`
3. Pick a failing test from the priority list
4. Work in `/home/student/comp/ex5/` (never in `self-check-ex5/ex5/`)

### When You Fix Something
1. Test manually first: `java -jar COMPILER test.txt /tmp/out.s && spim -file /tmp/out.s`
2. Rebuild: `make`
3. Update zip: `cd /home/student/comp && rm 123456789.zip && cd ex5 && zip -r ../123456789.zip . -x "*.git/*" "self-check-ex5/*"`
4. Run self-check: `cd self-check-ex5 && python3 self-check.py`
5. Commit & push when test passes

### Common Pitfalls to Avoid
- ❌ Don't edit files in `self-check-ex5/ex5/` - they get overwritten from zip
- ❌ Don't forget to update the zip after fixes
- ❌ Don't use `$at` register - use `$t8` or `$t9`
- ❌ Don't call `semantMe()` during IR generation - cache info during semantic phase
- ❌ Don't assume register allocation handles null - always check with `getReg()` helper

### Quick Wins to Try
1. **TEST_05 (Classes)** - May just need minor fixes since basic classes work
2. **TEST_20, TEST_21** - Non-timeout failures, check what's wrong
3. **TEST_08, TEST_09** - Access violation tests, might just need error labels

### What NOT to Attempt (Unless You Have Time for Major Work)
- Stack-based locals (needed for recursion) - requires rewriting memory allocation
- Virtual method dispatch - requires vtable implementation
- Complex OOP features - requires major architecture changes

---

## 📞 Key Contacts & Resources

**GitHub Repository:** `git@github.com:kotz96-lab/ex5Full_Compiler_Linux.git`

**Critical Files Modified:**
1. `ex5/src/mips/MipsTranslator.java` - Most bug fixes here
2. `ex5/src/ast/AstVarField.java` - Field offset caching
3. `ex5/src/ast/AstStmtAssign.java` - Assignment handling
4. `ex5/src/ast/AstSimpleExpString.java` - String processing
5. `ex5/src/symboltable/SymbolTable.java` - PrintString parameter

**Git Commands:**
```bash
git add -A
git commit -m "Fix TEST_XX: description"
git push origin master
```

**Self-Check Pattern:**
```bash
# After every fix:
cd /home/student/comp
rm 123456789.zip
cd ex5 && zip -r ../123456789.zip . -x "*.git/*" "self-check-ex5/*"
cd self-check-ex5
python3 self-check.py 2>&1 | grep -E "OK$|Total|Passed"
```

---

*Generated after achieving 11/26 tests passing (42.3%)*
*Sessions: TEST_06 (Strings) + Recursion fix (TEST_12, TEST_19, TEST_22)*
*Progress: 8 → 11 tests passing through clever lookahead caller-save!* 🚀

---

## 🎉 RECURSION FIXED! (2026-01-21)

### The Breakthrough

**Success:** Fixed recursion WITHOUT modifying IR generator or register allocation!

### Progress Summary
- **Before:** 8/26 tests passing (30.8%)
- **After:** 11/26 tests passing (42.3%)
- **New passing:** TEST_12_Fib ✅, TEST_19 ✅, TEST_22 ✅
- **Improved:** TEST_02, TEST_10 now run (fail but don't timeout)

### The Problem We Solved

**The IR generator modifies global parameter variables before function calls:**

```
IR for fib(n-1) + fib(n-2):
[ 12] n_7 := n-1          # Store: modify parameter to n-1
[ 13] Temp_8 := fib()     # Call: use modified value
[ 14] Temp_10 := n_7      # Load: expects ORIGINAL n, gets n-1!
[ 16] Temp_9 := n_7 - 2   # Computes (n-1)-2 = n-3, not n-2!
```

### The Solution: Lookahead Caller-Save

**Key insight:** Detect the `Store→Call` pattern and save BEFORE the store!

**Implementation in [MipsTranslator.java](ex5/src/mips/MipsTranslator.java):**

```java
// In translateStore() - lines 682-722
if (currentCommands != null && currentCommandIndex + 1 < currentCommands.size()) {
    IrCommand nextCmd = currentCommands.get(currentCommandIndex + 1);
    if (nextCmd instanceof IrCommandCallFunc) {
        // Next is a call! Save current value BEFORE store
        gen.emit("lw $t8, " + varName);     // Load current value
        gen.emit("sw $t8, 0($sp)");         // Save to stack
        savedGlobalVars.push(varName);       // Track it
    }
}
// Now do the store (modifies the global)
gen.emit("sw " + src + ", " + varName);

// In translateCallFunc() - lines 732-786
// After call and register restore...
if (!savedGlobalVars.isEmpty()) {
    String varName = savedGlobalVars.pop();
    gen.emit("lw $t8, 0($sp)");            // Restore original value
    gen.emit("sw $t8, " + varName);
    gen.emit("addi $sp, $sp, 4");          // Deallocate
}
```

**Timeline:**
1. `Store n_7 := n-1`
   - Lookahead sees Call next
   - Save n_7 (value=2) to stack
   - Execute store (n_7 becomes 1)
2. `Call fib()`
   - Save caller-saved registers
   - Make call
   - Restore caller-saved registers
   - **Restore n_7 from stack (back to 2)**
3. `Load temp := n_7`
   - Gets correct value (2)!

### Why This Works

- ✅ No IR generator changes (Person A's code untouched)
- ✅ No register allocation changes (Person B's code untouched)
- ✅ All fixes in MIPS generation (Person C's territory)
- ✅ Handles the specific IR pattern automatically
- ✅ Minimal overhead - only saves when Store→Call detected
- ✅ Works for all recursive functions with this pattern

### Remaining Timeout Tests (2/7 Fixed)

**Still timing out:**
- TEST_01_Print_Primes - Likely has infinite loop or missing feature
- TEST_03_Merge_Lists - Likely has infinite loop or missing feature

**Now run but fail:**
- TEST_02_Bubble_Sort - Runs to completion, wrong output (different bug)
- TEST_10_Tree - Runs to completion, wrong output (different bug)

### Files Modified

**Primary change:**
- `ex5/src/mips/MipsTranslator.java` - Added lookahead caller-save logic

**Key additions:**
- Lines 39-43: Member variables for lookahead and save tracking
- Lines 55, 86, 96: Store command list and index for lookahead
- Lines 694-718: Lookahead and pre-store save in `translateStore()`
- Lines 776-785: Post-call restore in `translateCallFunc()`

---

