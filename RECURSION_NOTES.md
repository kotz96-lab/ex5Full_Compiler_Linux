# Recursion Implementation Notes

## ✅ RECURSION FIXED! (2026-01-21)

### Final Implementation
- ✅ Function prologue/epilogue (saves $ra, $fp)
- ✅ Caller-saved register preservation ($t0-$t9)
- ✅ **Lookahead caller-save for globals** (THE KEY FIX!)
- ✅ Stack frame management

### Test Results
- **11/26 tests passing (42.3%)** - up from 8/26!
- **3 new tests passing:** TEST_12_Fib ✅, TEST_19 ✅, TEST_22 ✅
- **2 tests improved:** TEST_02, TEST_10 now run (fail, but don't timeout)
- **2 tests still timeout:** TEST_01, TEST_03 (may have other issues)

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

### Remaining Issues

**Still timing out (2/7):**
- TEST_01_Print_Primes - May have infinite loop or other issue
- TEST_03_Merge_Lists - May have infinite loop or other issue

**Now run but fail (2/7):**
- TEST_02_Bubble_Sort - Runs to completion, wrong output
- TEST_10_Tree - Runs to completion, wrong output

These may have different bugs unrelated to the recursion fix.

### Files Modified
- `/home/student/comp/ex5/src/mips/MipsTranslator.java` - Added prologue/epilogue, caller-save, callee-save
- All changes are in Person C's territory (MIPS generation)
- Zero changes to Person A's IR generator (as required)

