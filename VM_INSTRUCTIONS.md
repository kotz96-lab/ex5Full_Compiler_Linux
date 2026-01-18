# Ex5 Compiler - VM Setup and Testing Instructions

## CURRENT STATUS

We fixed the **parameter passing** system for Person A's IR generation code. The fixes are complete and correct:

✅ Parameters use symbol table offsets (prevtopIndex) instead of AST serial numbers
✅ Parameters stored in reverse order to match parser's reversed argument list
✅ Library functions (PrintInt, PrintString) have parameter names registered
✅ Parameter variables are allocated in .data section
✅ Arguments correctly stored to parameter variables before function calls

## REMAINING BUGS (Not parameter-related)

❌ **Recursion doesn't work** - architectural limitation (global variables get overwritten)
❌ **Division/precedence bug** - 3*2/6 = 0 instead of 1
❌ **Null/nil generation** - produces invalid MIPS syntax
❌ **Other bugs in Person B/C code** (register allocation, MIPS generation)

---

## WHAT YOU NEED TO DO IN THE VM

### 1. Install Required Tools

```bash
sudo apt update
sudo apt install -y openjdk-11-jdk spim make
```

### 2. Navigate to Project

```bash
cd /path/to/ex5
# The ex5 folder is in the zip file
```

### 3. Compile the Compiler

```bash
make compile
```

### 4. Test with Simple Programs

**Test 1: Basic output**
```bash
echo 'void main() { PrintInt(42); }' > test1.txt
java -jar COMPILER test1.txt test1.s
spim -file test1.s
# Expected output: 42
```

**Test 2: Arithmetic**
```bash
echo 'void main() { PrintInt(1 + 2 * 3); }' > test2.txt
java -jar COMPILER test2.txt test2.s
spim -file test2.s
# Expected output: 7
```

**Test 3: Function with parameters (non-recursive)**
```bash
cat > test3.txt << 'EOF'
int add(int a, int b) {
    return a + b;
}
void main() {
    PrintInt(add(5, 7));
}
EOF
java -jar COMPILER test3.txt test3.s
spim -file test3.s
# Expected output: 12
```

### 5. Run Self-Check Tests

```bash
# Extract self-check if needed
cd ..
unzip self-check-ex5.zip
cd self-check-ex5

# Create submission zip
cd ..
zip -r 123456789.zip ex5 ids.txt

# Run tests
python3 self-check.py
```

---

## KNOWN ISSUES TO FIX

### Priority 1: Division Bug (TEST_11 fails)
**Problem**: `3 * 2 / 6` returns 0 instead of 1
**File**: Check `ex5/src/mips/MipsTranslator.java` - division translation
**File**: Check `ex5/src/ir/IrCommandBinopDivIntegers.java`

### Priority 2: Null/Nil Bug (TEST_02 fails)
**Problem**: Generates `sw null, arr_9` which is invalid MIPS
**File**: Check `ex5/src/mips/MipsTranslator.java` - nil constant handling
**File**: Check `ex5/src/ir/IrCommandNilConst.java`

### Priority 3: Recursion (TEST_01, TEST_12, TEST_19, TEST_22 timeout)
**Problem**: Recursive functions overwrite their own parameters
**Solution**: Would need major redesign (stack frames) or call-depth naming
**Status**: May not be fixable in time - focus on non-recursive tests

---

## FILES MODIFIED (Already in the zip)

**Person A (IR Generation):**
1. `ex5/src/types/TypeFunction.java` - Added paramVarNames field
2. `ex5/src/ast/AstDecFunc.java` - Capture param names, allocate params in irMe()
3. `ex5/src/ast/AstTypeName.java` - Capture method param names
4. `ex5/src/ast/AstExpCall.java` - Store arguments to correct param variables
5. `ex5/src/symboltable/SymbolTable.java` - Register PrintInt/PrintString param names

**Also updated in ex5persona/** (Person A standalone version)

---

## TESTING STRATEGY

1. **Start simple** - test basic PrintInt, arithmetic
2. **Test non-recursive functions** - parameter passing should work
3. **Avoid recursion** - it's architecturally broken
4. **One bug at a time** - fix division, then nil, etc.
5. **Check MIPS output** - look at generated .s files to debug

---

## GOAL

Get 10-15 tests passing (out of 26) to show parameter passing works.
Focus on non-recursive tests without arrays/classes/nil values.

Tests likely to pass:
- TEST_11 (Precedence) - if we fix division
- TEST_17 (Global Variables)
- TEST_13 (Overflow)
- Others without recursion

---

## QUESTIONS FOR NEXT SESSION

1. What is the minimum passing score needed?
2. How much time do we have left?
3. Should we focus on getting SOME tests passing, or try to fix everything?

Good luck!
