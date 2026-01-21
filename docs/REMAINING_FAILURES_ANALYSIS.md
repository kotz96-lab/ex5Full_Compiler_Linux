# Remaining Test Failures Analysis (13/26 failing)

**Last Updated:** 2026-01-21
**Current Status:** 13/26 tests passing (50%)
**Remaining:** 13 failures

---

## 📊 Classification by Problem Type

### 🔴 CATEGORY 1: Method Calls (Class Methods) - **6 tests**
**Problem:** Method calls generate `jal <methodName>` but methods aren't emitted as global labels

**Affected Tests:**
- **TEST_05** - Classes (birthday, getAverage methods)
- **TEST_07** - Inheritance (WALK, RUN, SWIM methods)
- **TEST_16** - Classes (birthday, getAverage methods - same as TEST_05)
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

**Estimated Impact:** If fixed, +6 tests (46% → 69%)

---

### 🟡 CATEGORY 2: Access Violation Checks - **3 tests**
**Problem:** Need to detect and report specific access violations

**Affected Tests:**
- **TEST_08** - Access Violation (array/null pointer check)
- **TEST_09** - Invalid Pointer Dereference (null pointer check)
- **TEST_16** - Access Violation (array/null pointer check - same as TEST_08)

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

### 🟢 CATEGORY 3: Wrong Calculations - **3 tests**
**Problem:** Code runs but produces wrong numerical results

**Affected Tests:**
- **TEST_10** - Tree recursion (got 152, expected 1729)
- **TEST_15** - Many data members (got 4, expected 47)
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

### **QUICK WINS (Start Here):**

1. **Access Violation Tests (TEST_08, 09, 16)** - 3 tests
   - Already partially working (detects errors)
   - Just need to trigger correct error message
   - Check `RuntimeChecks.java` for bounds vs null checks
   - **Est. Time:** 30-60 min
   - **Est. Difficulty:** ⭐⭐ MEDIUM

2. **TEST_15 (Many Data Members)** - 1 test
   - Field offset calculation likely wrong
   - Check how we compute object field offsets
   - May be off-by-one or wrong stride
   - **Est. Time:** 30-60 min
   - **Est. Difficulty:** ⭐⭐ MEDIUM

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

## 📈 Impact Analysis

### By Effort/Reward Ratio:

**Best ROI:**
1. Access Violations (3 tests) - MEDIUM effort, 3 test gain = **+12%**
2. TEST_15 (1 test) - MEDIUM effort, 1 test gain = **+4%**

**Subtotal if we fix quick wins: 17/26 (65%)**

**Good ROI:**
3. TEST_10 (1 test) - MEDIUM-HARD effort = **+4%**
4. TEST_03 (1 test) - MEDIUM-HARD effort = **+4%**

**Subtotal if we fix medium: 19/26 (73%)**

**Major Investment:**
5. Method Calls (6 tests) - HARD effort = **+23%**
6. TEST_26 (1 test) - HARD effort = **+4%**

**Theoretical max: 26/26 (100%)**

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

## 📝 Next Steps

**Recommended Order:**

1. ✅ Fix access violation checks (TEST_08, 09, 16) → **17/26 (65%)**
2. ✅ Fix TEST_15 field offsets → **18/26 (69%)**
3. ⚠️ Debug TEST_10 tree recursion → **19/26 (73%)**
4. ⚠️ Debug TEST_03 list recursion → **20/26 (77%)**
5. 🔴 Implement method calls (big project) → **26/26 (100%)** or **25/26 (96%)** if TEST_26 unfixable

**Timeline Estimate:**
- Quick wins (1-2): ~1-2 hours → 65-69%
- Medium tasks (3-4): ~2-4 hours → 73-77%
- Method calls (5): ~3-8 hours → 96-100%
- **Total to 100%: ~6-14 hours** (depends on method call approach)

---

**End of Analysis**
