# 🤖 New Claude - Start Here!

**Project:** Ex5 Compiler - Getting ALL tests passing
**Current Status:** 11/26 tests passing (42.3%)
**Goal:** Reach 26/26 tests (100%)
**Last Updated:** 2026-01-21 (Recursion fixed!)

---

## ⚡ Quick Start (Do This First!)

```bash
# 1. Check current status
cd /home/student/comp/ex5/self-check-ex5
python3 self-check.py 2>&1 | grep -E "OK$|Total|Passed"

# 2. Read the comprehensive guide
cat /home/student/comp/LESSONS_LEARNED.md

# 3. Check last session summary
cat /home/student/comp/SESSION_SUMMARY.md
```

---

## 📊 Current Status

### ✅ Passing Tests (11/26 - 42.3%)
1. TEST_04 (Matrices) - Arrays work
2. TEST_06 (Strings) - Fixed!
3. TEST_11 (Precedence) - Arithmetic
4. **TEST_12 (Fib) - Recursion fixed!** ⭐
5. TEST_13 (Overflow) - Saturated arithmetic
6. TEST_14 (Many Local Variables)
7. TEST_17 (Global Variables)
8. TEST_18 - Basic functionality
9. **TEST_19 - Recursion fixed!** ⭐
10. **TEST_22 - Recursion fixed!** ⭐
11. TEST_25 - Basic functionality

### ⏱️ Still Timeout (2) - Unknown Issues
TEST_01 (Print_Primes), TEST_03 (Merge_Lists)
May have infinite loops or missing features

### ⚠️ Improved Tests (2) - Now run but fail
TEST_02 (Bubble_Sort), TEST_10 (Tree)
Used to timeout, now produce wrong output

### ❌ Other Failures (11) - Your Target!
TEST_05, TEST_07, TEST_08, TEST_09, TEST_15, TEST_16, TEST_20, TEST_21, TEST_23, TEST_24, TEST_26

---

## 🎯 Recommended Next Steps

### **Start with these (likely quick wins):**

1. **TEST_02 (Bubble_Sort)** - Now runs! Wrong output, easier to debug
2. **TEST_10 (Tree)** - Now runs! Wrong output, easier to debug
3. **TEST_05 (Classes)** - Basic classes work, check what's missing
4. **TEST_20, TEST_21** - Non-timeout failures, unknown issues
5. **TEST_08, TEST_09 (Access Violation)** - May just need error handling labels

### How to Debug a Test:
```bash
# 1. Go to working directory
cd /home/student/comp/ex5

# 2. Compile the test
java -jar COMPILER self-check-ex5/tests/TEST_XX_Name.txt /tmp/test.s 2>&1

# 3. Check for compilation errors
grep -i "error\|exception"

# 4. Look at generated assembly
head -50 /tmp/test.s  # Check .data section
grep -i "error\|undefined" /tmp/test.s

# 5. Run in SPIM
spim -file /tmp/test.s 2>&1

# 6. Compare with expected output
diff self-check-ex5/expected_output/TEST_XX_Name_Expected_Output.txt <(spim -file /tmp/test.s 2>&1)
```

---

## 🚨 CRITICAL: Self-Check Workflow

**THE MOST IMPORTANT THING TO KNOW:**

The self-check extracts from `123456789.zip` EVERY TIME, overwriting all files in `self-check-ex5/ex5/`!

### ✅ CORRECT Workflow:

```bash
# 1. ALWAYS work in main ex5 directory
cd /home/student/comp/ex5
# Edit files here: src/mips/, src/ast/, src/symboltable/

# 2. Rebuild
make

# 3. Test manually first
java -jar COMPILER self-check-ex5/tests/TEST_XX.txt /tmp/test.s
spim -file /tmp/test.s

# 4. When test works, UPDATE THE ZIP (CRITICAL!)
cd /home/student/comp
rm 123456789.zip
cd ex5
zip -r ../123456789.zip . -x "*.git/*" "self-check-ex5/*"

# 5. Now run self-check
cd self-check-ex5
python3 self-check.py 2>&1 | grep -E "OK$|Total|Passed"

# 6. If test passes, commit & push
cd /home/student/comp
git add -A
git commit -m "Fix TEST_XX: description"
git push origin master
```

### ❌ WRONG Workflow:
- Editing files in `self-check-ex5/ex5/` ← Gets overwritten!
- Forgetting to update `123456789.zip` ← Self-check uses old code!
- Running self-check without rebuilding zip ← Tests fail!

---

## 🔧 Major Fixes Already Implemented

### 1. Equality Check Register Clobbering (FIXED)
**Location:** `ex5/src/mips/MipsTranslator.java:322-355`
- Saves operands before overwriting destination register
- Uses `$t8`, `$t9` for temporary storage

### 2. Field Assignment Caching (FIXED)
**Location:** `ex5/src/ast/AstVarField.java` + `AstStmtAssign.java`
- Caches field offsets during semantic analysis
- Uses cached values during IR generation
- Never calls `semantMe()` during IR phase

### 3. String Handling (FIXED)
**Location:** `ex5/src/ast/AstSimpleExpString.java`
- Strips quotes from string literals
- Generates proper `.asciiz` directives

### 4. Reserved Register Fix (FIXED)
**Location:** `ex5/src/mips/MipsTranslator.java`
- Never uses `$at` (reserved for assembler)
- Uses `$t8` or `$t9` instead

### 5. Null Register Handling (FIXED)
**Location:** `ex5/src/mips/MipsTranslator.java:106-113`
- `getReg()` helper returns `$zero` for null temps

---

## ⚠️ Common Pitfalls (Don't Do These!)

1. ❌ **Editing `self-check-ex5/ex5/`** - Files get overwritten from zip
2. ❌ **Forgetting to update zip** - Self-check uses old code
3. ❌ **Using `$at` register** - Reserved for assembler, use `$t8`/`$t9`
4. ❌ **Calling `semantMe()` during IR** - Symbol table cleared, will fail
5. ❌ **Not testing manually first** - Wastes time on broken code
6. ❌ **Attempting recursion tests** - Need stack locals (major work)

---

## 📁 Key Files & Locations

### Where to Make Changes:
```
/home/student/comp/ex5/src/
├── mips/
│   ├── MipsTranslator.java    ← Most bug fixes here!
│   ├── MipsGenerator.java     ← Assembly output
│   └── StringTable.java       ← String constants
├── ast/
│   ├── AstVarField.java       ← Field access (modified)
│   ├── AstStmtAssign.java     ← Assignments (modified)
│   └── AstSimpleExpString.java ← Strings (modified)
└── symboltable/
    └── SymbolTable.java       ← Symbol table (modified)
```

### Test Files:
```
/home/student/comp/ex5/self-check-ex5/
├── tests/                     ← Test source files
│   ├── TEST_01_Print_Primes.txt
│   ├── TEST_06_Strings.txt (fixed!)
│   └── ...
├── expected_output/           ← Expected SPIM output
│   ├── TEST_01_Print_Primes_Expected_Output.txt
│   └── ...
└── self-check.py             ← Test runner script
```

### Documentation:
- **LESSONS_LEARNED.md** ← Comprehensive guide (READ THIS!)
- **SESSION_SUMMARY.md** ← Last session details
- **CLAUDE.md** ← This file

---

## 🎓 Architecture Quick Reference

### Compilation Pipeline:
```
Source Code (.txt)
    ↓ Lexer/Parser
  AST
    ↓ [Person A - Semantic Analysis]
  Type Checking
    ↓ [Person A - IR Generation]
  IR Commands
    ↓ [Person B - Register Allocation]
  Temp → Register mapping
    ↓ [Person C - MIPS Generation]
  MIPS Assembly (.s)
    ↓ SPIM
  Execution
```

### Critical Limitation: No Stack Locals
- All variables allocated as globals in `.data`
- Variables named: `variableName_offset` (e.g., `l1_8`, `p_10`)
- **Breaks recursion and nested calls**
- Affects 7 timeout tests

---

## 🔍 Debugging Commands Cheat Sheet

```bash
# Check what's failing in a test
java -jar COMPILER test.txt /tmp/out.s 2>&1 | grep -i error

# Look for specific issues
grep "\$at" /tmp/out.s              # Reserved register
grep "undefined" /tmp/out.s         # Missing symbols
grep "p_10\|str_11" /tmp/out.s      # Parameter variables

# Check .data section
head -30 /tmp/out.s

# Run and capture output
spim -file /tmp/out.s 2>&1 > /tmp/output.txt

# Compare with expected
diff expected_output.txt /tmp/output.txt

# Check class file contents (if suspicious)
jar xf COMPILER mips/MipsTranslator.class
strings mips/MipsTranslator.class | grep "lb"
```

---

## 📝 When You Fix a Test

```bash
# 1. Document what was wrong
echo "## TEST_XX Fix" >> FIXES.md
echo "Problem: ..." >> FIXES.md
echo "Solution: ..." >> FIXES.md

# 2. Update zip
cd /home/student/comp
rm 123456789.zip
cd ex5 && zip -r ../123456789.zip . -x "*.git/*" "self-check-ex5/*"

# 3. Verify self-check passes
cd self-check-ex5
python3 self-check.py 2>&1 | grep "TEST_XX.*OK"

# 4. Commit
cd /home/student/comp
git add -A
git commit -m "Fix TEST_XX: brief description

Detailed explanation of what was wrong and how fixed.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
git push origin master
```

---

## 🎯 Your Mission

**Goal:** Get from 8/26 to 13/26 tests (50%)

**Strategy:**
1. Pick a test from the recommended list (TEST_05, TEST_20, TEST_21, TEST_08, TEST_09)
2. Debug it thoroughly (compile, run, check errors)
3. Fix the issue (likely in `MipsTranslator.java` or AST classes)
4. Update zip and verify with self-check
5. Commit and move to next test

**Time Management:**
- Spend ~30-60 min per test maximum
- If stuck, document findings and move to next test
- Focus on quick wins, avoid recursion tests

---

## 📞 Reference Info

**Git Repository:** `git@github.com:kotz96-lab/ex5Full_Compiler_Linux.git`

**Recent Commits:**
- `b936bee` - Session summary: 8/26 tests passing, TEST_06 fixed
- `952d1ca` - Enhanced lessons learned
- `77e0885` - Progress: 8/26 tests passing - Fixed TEST_06
- `fcadb73` - Initial commit with base fixes

**Key Resources:**
- LESSONS_LEARNED.md (496 lines) - Comprehensive guide
- SESSION_SUMMARY.md - Last session recap
- Git history - See all previous fixes

---

## 💪 You Got This!

The hardest parts are done:
- ✅ Self-check workflow figured out
- ✅ Core fixes implemented (equality, fields, strings, registers)
- ✅ 8/26 tests already passing
- ✅ Complete documentation available

**Just follow the workflow, debug systematically, and you'll reach 13/26!** 🚀

---

*Last session: 2026-01-18*
*Tests fixed: TEST_06 (Strings)*
*Next targets: TEST_05, TEST_20, TEST_21, TEST_08, TEST_09*

Good luck! 💪
