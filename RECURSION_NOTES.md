# Recursion Implementation Notes

## Current Situation (2026-01-21)

### What's Implemented
- ✅ Function prologue/epilogue (saves $ra, $fp)
- ✅ Caller-saved register preservation ($t0-$t9)
- ✅ Callee-saved global variable preservation
- ✅ Stack frame management

### Test Results
- Still 8/26 passing (no regression, but no improvement)
- Recursion tests (TEST_01, TEST_02, TEST_03, TEST_10, TEST_12, TEST_19, TEST_22) still fail

### The Root Cause

**Person A's IR generator is incompatible with global variables for recursion.**

Example from fib(n-1) + fib(n-2):
```
Line 12: n_7 := n-1        # Caller modifies global
Line 13: call fib(n-1)     # Uses modified global
Line 14: load n_7          # IR expects ORIGINAL n, gets n-1!
Line 16: n_7 - 2           # Computes (n-1)-2 = n-3, not n-2
```

The IR generator:
1. Stores modified parameter value to global
2. Makes call
3. Reads from global expecting ORIGINAL value
4. This only works if variables are NOT global (stack-local)

### Why Our Fix Doesn't Work

**Callee-save** means: callee saves/restores on function entry/exit
- fib(2) sets n_7=1, calls fib(1)
- fib(1) saves n_7=1 (current value)
- fib(1) restores n_7=1 on return
- fib(2) needs n_7=2 (original), gets n_7=1 ❌

We need **caller to save BEFORE modifying**, but we can't control when modifications happen (that's in the IR).

### Paths Forward

#### Option 1: Full Stack-Local Parameters (HARD)
- Make parameters live on stack
- Implement caller pushes arguments
- Callee reads from stack
- **Problem:** Requires rewriting IR generator (Person A's code)

#### Option 2: Compiler Transformation (MEDIUM-HARD)
- Detect parameter modifications before calls
- Insert save/restore around those modifications
- **Problem:** Complex analysis, fragile

#### Option 3: Accept Limitation (CURRENT)
- Document that recursion doesn't work
- Focus on 11 non-timeout test failures
- Get to 19/26 without recursion

#### Option 4: Minimal Calling Convention (MEDIUM)
- Use $a0-$a3 for first 4 parameters
- Caller loads parameter value into $aX before call
- Callee reads from $aX, not from global
- **Problem:** Still requires IR changes to emit parameter loads

### Recommendation

**For immediate progress:** Option 3 - fix the 11 non-timeout failures
**For eventual 26/26:** Option 1 or 4 - requires IR generator changes

### Files Modified
- `/home/student/comp/ex5/src/mips/MipsTranslator.java` - Added prologue/epilogue, caller-save, callee-save
- All changes are in Person C's territory (MIPS generation)
- Zero changes to Person A's IR generator (as required)

