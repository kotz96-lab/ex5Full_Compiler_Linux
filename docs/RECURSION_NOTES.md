# Recursion Implementation Notes

## ✅ RECURSION FIXED! (2026-01-21)

### Update (latest session)
- **Self-check still at 17/26** (no net change).
- **Manual fix verified:** `TEST_05_Classes` now outputs `8400 8400`.
- **Note:** Self-check does include `TEST_05`; my earlier `grep` filtered out non-OK lines, so failures were hidden.

### Changes made
- Cached class member lists and field initializers for object construction.
- Cached `this_#` for field access and used it during IR generation.
- Enforced LHS-before-RHS evaluation in assignments to preserve side effects.

### Final Implementation
- ✅ Function prologue/epilogue (saves $ra, $fp)
- ✅ Caller-saved register preservation ($t0-$t9)
- ✅ **Lookahead caller-save for globals** (THE KEY FIX!)
- ✅ Stack frame management

### Test Results (Session 1)
- **11/26 tests passing (42.3%)** - up from 8/26!
- **3 new tests passing:** TEST_12_Fib ✅, TEST_19 ✅, TEST_22 ✅
- **2 tests improved:** TEST_02, TEST_10 now run (fail, but don't timeout)
- **2 tests still timeout:** TEST_01, TEST_03 (may have other issues)

### BREAKTHROUGH UPDATE (Session 2 - 2026-01-21)
- **13/26 tests passing (50%)** - HALFWAY THERE! 🎉
- **2 MORE tests passing:** TEST_01_Print_Primes ✅, TEST_02_Bubble_Sort ✅

### The Root Cause We Discovered

**Person A's IR generator has a specific pattern that breaks with global variables:**

Example from fib(n-1) + fib(n-2):
```
Line 12: n_7 := n-1        # Caller modifies global parameter
Line 13: call fib(n-1)     # Uses modified global
Line 14: load n_7          # IR expects ORIGINAL n, gets n-1!
Line 16: n_7 - 2           # Computes (n-1)-2 = n-3, not n-2
```

The IR generator:
1. Stores modified parameter value to global
2. Makes call
3. Reads from global expecting ORIGINAL value
4. This only works with proper caller-save around the modification

### The Solution: Lookahead Caller-Save

**Key insight:** We can detect and fix the Store→Call pattern at MIPS generation time!

**Implementation in [MipsTranslator.java:682-722](src/mips/MipsTranslator.java#L682-L722):**

1. **In translateStore():** Look ahead to next IR command
   - If next is Call AND this variable is used by the function:
     - Save current global value to stack BEFORE the store
     - Track that we saved this variable (push to stack)

2. **In translateCallFunc():** After call and register restore
   - If we saved a global (stack not empty):
     - Restore the original value from stack
     - Pop the saved variable tracker

**Timeline:**
```
Store n_7 := n-1
  → Lookahead sees Call next
  → Save current n_7 (value 2) to stack
  → Perform store (n_7 becomes 1)
Call fib()
  → Save caller-saved registers
  → Make call
  → Restore caller-saved registers
  → Restore n_7 from stack (back to 2)
  → Now Load n_7 gets correct value!
```

### Why This Works

- ✅ No changes to IR generator required (Person A's code untouched)
- ✅ No changes to register allocation (Person B's code untouched)
- ✅ All fixes in MIPS generation (Person C's territory)
- ✅ Handles the specific IR pattern that causes recursion bugs
- ✅ Minimal overhead - only saves when Store→Call detected

### THE SECOND BUG: Function Label Detection (Session 2)

**Root Cause:**
Person A's IR generator emits function labels that can start with either:
- Uppercase: `BubbleSort`, `PrintInt`, etc.
- Lowercase: `main`, `fib`, etc.

Jump labels always start with: `Label_1_start`, `Label_0_end`, etc.

Our original detection used `Character.isLowerCase(labelName.charAt(0))` which:
- ✅ Correctly identified `main`, `fib`
- ❌ **MISSED** `BubbleSort`, `PrintInt`, and any function starting with uppercase!

**Impact:**
- Functions with uppercase names got NO prologue/epilogue
- When called, they would corrupt the stack (no $ra/$fp save)
- When they "returned", stack was completely wrong
- Caused infinite loops, crashes, and wrong outputs

**The Fix:**
Changed detection from checking first character case to:
```java
// OLD (WRONG):
if (Character.isLowerCase(labelName.charAt(0)))

// NEW (CORRECT):
if (!labelName.startsWith("Label_"))
```

This correctly identifies ALL function labels (uppercase or lowercase) and excludes all jump labels.

**Bonus Discovery: Missing ReturnVoid**
Person A doesn't always generate explicit `ReturnVoid` for void functions!
- Void functions just end, then next function starts
- This causes "fall-through" - one function falls into the next
- Stack gets corrupted because no epilogue/return

**The Fix:**
When we encounter a new function label while already in a function:
```java
if (currentFunction != null) {
    gen.emitComment("Implicit return for void function");
    emitFunctionEpilogue();
    gen.emit("jr $ra");
}
```

This adds the missing return between functions automatically.

**Results:**
- TEST_01_Print_Primes: TIMEOUT → PASS ✅ (was calling PrintInt with no prologue!)
- TEST_02_Bubble_Sort: FAIL → PASS ✅ (BubbleSort now gets prologue/epilogue!)

### Remaining Issues (13/26 still failing)

**Still timing out (1):**
- TEST_03_Merge_Lists - May have different issue

**Wrong output (12):**
- TEST_05, 07, 08, 09, 10, 15, 16, 20, 21, 23, 24, 26
- These likely have bugs unrelated to function calls/recursion

### Key Insights for Future Sessions

1. **Person A's IR is quirky but fixable at MIPS level:**
   - Function names can be any case
   - Void functions may not have explicit ReturnVoid
   - Store→Call→Load pattern expects value preservation
   - All fixable in Person C's MIPS generation!

2. **The winning strategy:**
   - Don't try to fix Person A's IR generator
   - Don't try to change Person B's register allocation
   - Work entirely in MipsTranslator.java (Person C's domain)
   - Use lookahead, implicit returns, and smart detection

3. **What worked:**
   - Lookahead for Store→Call pattern
   - Implicit returns between functions
   - Correct function label detection (!startsWith("Label_"))
   - Caller-save for all $t registers
   - Saving/restoring globals around Store→Call

4. **Test categories:**
   - Recursion tests: FIXED ✅ (3/3 passing: TEST_12, 19, 22)
   - Function call tests: FIXED ✅ (2/2 passing: TEST_01, 02)
   - Remaining: Likely arrays, classes, field access, bounds checking

### Files Modified
- `/home/student/comp/ex5/src/mips/MipsTranslator.java`:
  - Lines 165-173: Fixed function label detection
  - Lines 657-664: Added implicit returns for void functions
  - Lines 694-718: Lookahead caller-save for Store→Call pattern
  - Lines 749-756: Caller-saved register preservation
  - Lines 779-785: Global restoration after calls
- All changes in Person C's territory (MIPS generation)
- Zero changes to Person A's IR generator (as required)
- Zero changes to Person B's register allocation (as required)
