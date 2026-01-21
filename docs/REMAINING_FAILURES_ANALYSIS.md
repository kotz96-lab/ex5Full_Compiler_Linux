# Remaining Test Failures Analysis (13/26 failing)

**Last Updated:** 2026-01-21
**Current Status:** 13/26 tests passing (50%)
**Remaining:** 13 failures

---

## 📊 Classification by Problem Type

### 🔴 CATEGORY 1: Method Calls (Class Methods) - **10 tests!** ⚠️ BIGGEST BLOCKER
**Problem:** Method calls generate `jal <methodName>` but methods aren't emitted as global labels

**⚠️ CRITICAL DISCOVERY:** This blocks MORE tests than originally thought!

**Affected Tests:**
- **TEST_05** - Classes (birthday, getAverage methods)
- **TEST_07** - Inheritance (WALK, RUN, SWIM methods)
- **TEST_08** - Access Violation (BLOCKED by birthday/getAverage calls first!)
- **TEST_09** - Access Violation (BLOCKED by birthday/getAverage calls first!)
- **TEST_15** - Many Data Members (BLOCKED by f.sum() method call!)
- **TEST_16** - Classes (BLOCKED by birthday/getAverage methods)
- **TEST_20** - Simple method call (what method)
- **TEST_21** - Inheritance with method override (go method)
- **TEST_23** - Multiple inheritance (go, come methods)
- **TEST_24** - Method with division by zero (oops method)

**Symptoms:**
```
Instruction references undefined symbol at 0x004002a4
[0x004002a4] 0x0c000000  jal 0x00000000 [WALK]
```

**Root Cause:**
- IR generates `IrCommandMethodCall` for class methods
- We translate to `jal methodName`
- But class methods aren't emitted as standalone labels in .text
- They need virtual dispatch through vtables OR direct labels

**Difficulty:** ⭐⭐⭐ HARD
- Need to understand how Person A generates method IR
- May need virtual method tables (vtables) for inheritance
- Or need to emit methods as `ClassName_methodName` labels
- Inheritance complicates this (override behavior)

**Estimated Impact:** If fixed, +10 tests (50% → **88%**!) 🎯
- This is THE big unlock!
- Gets us from 13/26 → 23/26
- Only 3 tests left after this!

---

### 🟡 CATEGORY 2: Access Violation Checks - **0 tests** ❌ FALSE ALARM
**Problem:** ~~Need to detect and report specific access violations~~ BLOCKED BY CATEGORY 1

**⚠️ UPDATE:** These tests are actually BLOCKED by method calls!

**Affected Tests:**
- **TEST_08** - ~~Access Violation~~ → Actually crashes on method calls first
- **TEST_09** - ~~Invalid Pointer Dereference~~ → Actually crashes on method calls first
- **TEST_16** - ~~Access Violation~~ → Actually crashes on method calls first

**Status:** NOT a separate category - these will pass once method calls work!

**Expected Outputs:**
- TEST_08: "Access Violation"
- TEST_09: "Invalid Pointer Dereference"
- TEST_16: "Access Violation"

**Current Output:**
- TEST_08/09/16: "Invalid Pointer Dereference" (wrong, or crashes)

**Symptoms:**
```
The following symbols are undefined:
p_10

Invalid Pointer Dereference
```

**Root Cause:**
- Tests deliberately trigger errors (null pointer access, array bounds)
- Our runtime checks may be incomplete or trigger wrong error
- May need better bounds checking or null checking

**Difficulty:** ⭐⭐ MEDIUM
- Runtime checks already exist (`RuntimeChecks.java`)
- Need to find what triggers "Access Violation" vs "Invalid Pointer Dereference"
- May need array bounds checking improvements

**Estimated Impact:** If fixed, +3 tests (50% → 61%)

---

### 🟢 CATEGORY 3: Wrong Calculations - **2 tests** (was 3, TEST_15 moved to Category 1)
**Problem:** Code runs but produces wrong numerical results

**Affected Tests:**
- **TEST_10** - Tree recursion (got 152, expected 1729)
- ~~**TEST_15** - Many data members~~ → MOVED TO CATEGORY 1 (blocked by method calls)
- **TEST_26** - Many parameters (got 81, expected "Register Allocation Failed")

**Symptoms:**
- TEST_10: 152 instead of 1729 (tree sum wrong)
- TEST_15: 4 instead of 47 (field access wrong)
- TEST_26: 81 instead of error message (should fail register allocation!)

**Root Cause:**
**TEST_10:**
- Tree with 14 nodes weighted 0*19 through 13*19
- Sum should be 19*(0+1+2+...+13) = 19*91 = 1729
- Got 152 = 8*19, only summing 8 nodes
- Recursion may be breaking early or miscounting

**TEST_15:**
- Class with 26 int fields (i01 through i26)
- Should sum to 1+2+...+26 = 351... wait, expected is 47?
- Field offset calculation may be wrong

**TEST_26:**
- Function with 18 parameters
- Expected: "Register Allocation Failed"
- We're succeeding and computing wrong value
- Person B should FAIL this (not enough registers for 18 params!)
- Our Person B is too lenient OR we're interfering

**Difficulty:**
- TEST_10: ⭐⭐ MEDIUM (recursion logic bug)
- TEST_15: ⭐⭐ MEDIUM (field offset bug)
- TEST_26: ⭐⭐⭐ HARD (register allocation should reject this!)

**Estimated Impact:** If fixed, +3 tests (50% → 61%)

---

### 🔵 CATEGORY 4: Timeout - **1 test**
**Problem:** Infinite loop or very slow execution

**Affected Tests:**
- **TEST_03** - MergeLists (linked list merge)

**Expected Output:** `12 34 50 70 92 96 97 99`

**Symptoms:**
- Times out (>2 seconds)
- Likely infinite loop in merge algorithm

**Root Cause:**
- Recursive list merge
- May have issue with:
  - Nil checking (if (l1 = nil) condition)
  - Field access (l1.head, l1.tail)
  - Recursive calls breaking
  - List construction

**Difficulty:** ⭐⭐⭐ MEDIUM-HARD
- Need to debug why recursion loops
- Could be field access, nil checks, or recursion issue

**Estimated Impact:** If fixed, +1 test (50% → 54%)

---

## 🎯 Recommended Attack Strategy

### ⚠️ **REVISED STRATEGY (After Investigation):**

The "quick wins" turned out to be **FALSE ALARMS** - they're all blocked by method calls!

**The ONLY path forward:**

1. **FIX METHOD CALLS FIRST** - 10 tests unlocked! (13/26 → 23/26 = 88%)
   - This is THE big blocker
   - Everything else depends on this
   - **Est. Time:** 3-8 hours
   - **Est. Difficulty:** ⭐⭐⭐⭐ HARD
   - **Impact:** +10 tests in one go!

### **MEDIUM EFFORT:**

3. **TEST_10 (Tree Recursion)** - 1 test
   - Tree recursion already works in other tests
   - Debug why only 8/14 nodes counted
   - May be field access or nil check issue
   - **Est. Time:** 1-2 hours
   - **Est. Difficulty:** ⭐⭐⭐ MEDIUM-HARD

4. **TEST_03 (Merge Lists)** - 1 test
   - Linked list recursion
   - Debug timeout/infinite loop
   - Related to field access like TEST_10
   - **Est. Time:** 1-2 hours
   - **Est. Difficulty:** ⭐⭐⭐ MEDIUM-HARD

### **BIG PROJECTS:**

5. **Method Calls (TEST_05, 07, 16, 20, 21, 23, 24)** - 7 tests (but TEST_16 overlaps with #1)
   - Need to implement method dispatch
   - Options:
     - **A) Simple:** Emit methods as `ClassName_methodName` labels
     - **B) Complex:** Implement vtables for virtual dispatch
   - Inheritance complicates override behavior
   - **Est. Time:** 3-5 hours (simple) or 8+ hours (vtables)
   - **Est. Difficulty:** ⭐⭐⭐⭐ HARD

6. **TEST_26 (Register Allocation)** - 1 test
   - SHOULD fail (18 parameters, not enough registers)
   - Person B is incorrectly succeeding
   - Either we broke Person B, or need to add validation
   - **Est. Time:** 2-4 hours
   - **Est. Difficulty:** ⭐⭐⭐⭐ HARD (involves Person B's code)

---

## 📈 Impact Analysis (REVISED AFTER INVESTIGATION)

### The Truth About "Quick Wins":

**❌ FALSE ALARMS (all blocked by methods):**
- ~~Access Violations (3 tests)~~ → Need method calls first
- ~~TEST_15 (1 test)~~ → Need method calls first

**✅ ACTUAL REMAINING TESTS:**

1. **Method Calls (10 tests!)** - 13/26 → 23/26 = **+38%** 🎯
   - THE critical path
   - Unlocks: TEST_05, 07, 08, 09, 15, 16, 20, 21, 23, 24
   - **Est. Time:** 3-8 hours

2. **TEST_10 (1 test)** - 23/26 → 24/26 = **+4%**
   - Tree recursion wrong calculation
   - **Est. Time:** 1-2 hours

3. **TEST_03 (1 test)** - 24/26 → 25/26 = **+4%**
   - List merge timeout
   - **Est. Time:** 1-2 hours

4. **TEST_26 (1 test)** - 25/26 → 26/26 = **+4%**
   - Should fail register allocation
   - **Est. Time:** 2-4 hours (involves Person B)

**Path to 100%: Fix method calls → Fix TEST_10/03 → Fix TEST_26**
**Total est. time: 7-16 hours**

---

## 🔍 Detailed Breakdown

### Category 1: Method Calls (6 tests)

| Test | Description | Method Called | Expected | Got |
|------|-------------|---------------|----------|-----|
| TEST_05 | Classes | birthday, getAverage | 8400 8400 | Crash (undefined) |
| TEST_07 | Inheritance | WALK, SWIM | SonWALKFatherSWIM | Crash (undefined) |
| TEST_16 | Classes | birthday, getAverage | Access Violation | Crash (undefined) |
| TEST_20 | Simple method | what | what | Crash (undefined) |
| TEST_21 | Override | go | no | Crash (undefined) |
| TEST_23 | Multiple methods | go, come | noyesgo666 | Crash (undefined) |
| TEST_24 | Div by zero | oops | whatIllegal Division By Zero | Crash (undefined) |

**Common Pattern:**
```
jal methodName    # Generated by us
# But methodName label doesn't exist!
```

### Category 2: Access Violations (3 tests)

| Test | Description | Expected | Got |
|------|-------------|----------|-----|
| TEST_08 | Array bounds | Access Violation | Invalid Pointer Dereference |
| TEST_09 | Null pointer | Invalid Pointer Dereference | Invalid Pointer Dereference ✓ (maybe?) |
| TEST_16 | Array bounds | Access Violation | Invalid Pointer Dereference |

**Note:** TEST_09 might already be passing! Need to verify.

### Category 3: Wrong Calculations (3 tests)

| Test | Description | Expected | Got | Difference |
|------|-------------|----------|-----|------------|
| TEST_10 | Tree sum | 1729 | 152 | Only 8/14 nodes |
| TEST_15 | Field sum | 47 | 4 | Wrong field offsets? |
| TEST_26 | 18 params | Reg Alloc Failed | 81 | Should fail! |

### Category 4: Timeout (1 test)

| Test | Description | Expected | Got |
|------|-------------|----------|-----|
| TEST_03 | Merge lists | 12 34 50 70 92 96 97 99 | Timeout | Infinite loop |

---

## 🎓 Key Insights

1. **Method calls are the biggest blocker** (6 tests = 23%)
2. **Quick wins available** (access violations + TEST_15 = 4 tests = 15%)
3. **Recursion mostly works** but edge cases exist (TEST_03, TEST_10)
4. **Person B may need hardening** (TEST_26 should fail)

---

## 📝 Next Steps (REVISED - Method Calls are THE Path)

**⚠️ CRITICAL:** The "quick wins" were illusions - everything needs method calls first!

**THE PLAN FOR NEXT SESSION:**

1. 🔴 **Implement Method Calls** → **23/26 (88%)** 🎯
   - This is THE unlock
   - +10 tests in one shot
   - Unlocks: TEST_05, 07, 08, 09, 15, 16, 20, 21, 23, 24
   - **Est. Time:** 3-8 hours
   - **Approaches:**
     - **Simple:** Emit methods as `ClassName_methodName` labels (faster)
     - **Complex:** Implement vtables for virtual dispatch (proper, harder)

2. ⚠️ **Fix TEST_10/03** → **25/26 (96%)**
   - After methods work, tackle recursion edge cases
   - **Est. Time:** 2-4 hours

3. 🔵 **Fix TEST_26** → **26/26 (100%)** ✅
   - Final boss: should fail register allocation
   - **Est. Time:** 2-4 hours

**Path to 23/26 (88%):**
- **Next session goal:** Fix method calls → instant +10 tests!
- **Total est. time:** 3-8 hours for method calls alone
- **Remaining after that:** Only 3 tests (TEST_03, 10, 26)

---

**End of Analysis**
