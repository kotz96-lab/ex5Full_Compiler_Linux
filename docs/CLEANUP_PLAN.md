# Repository Cleanup Plan

**Status:** 11/26 tests passing (42.3%)
**Created:** 2026-01-21
**Priority:** HIGH - Repo is messy with old debug files

---

## 🎯 Cleanup Goals

1. Remove all temporary/debug files
2. Organize documentation properly
3. Clean up self-check-ex5 directory
4. Remove duplicate/old zips and tarballs
5. Keep only essential files for development

---

## 📂 Directories to Clean

### `/home/student/comp/ex5/` (Main Directory)
**Remove:**
- `debug_TEST_*.s` files (old debug outputs)
- `test_*.s` files (old test outputs)
- `*.tar.gz` files (old tarballs: ex5_all_fixes.tar.gz, ex5_complete_fix.tar.gz, etc.)
- Old zip: `123456789.zip` in ex5/ (wrong location)
- Any `test*.txt` files (temporary test files)

**Keep:**
- `src/` - Source code
- `bin/` - Compiled classes
- `COMPILER` - The JAR file
- `Makefile` - Build system
- `build.bat` - Windows build
- `ids.txt` - Student IDs
- Essential jars: `cup/`, `external_jars/`, `jflex/`, `manifest/`

### `/home/student/comp/ex5/self-check-ex5/` (Test Directory)
**THIS IS THE MESSIEST!**

**Remove:**
- All `test_*.s` files (test outputs)
- All `debug_*.s` files (debug outputs)
- `*.tar.gz` files (ex5_all_fixes.tar.gz, ex5_final.tar.gz, etc.)
- Old zip: `123456789.zip` (will be replaced from parent)
- Nested `self-check-ex5/` directory (if exists)
- Old `*.md` files except README.md
- `Makefile.bak`
- `*.txt` test files in root (test1.txt, test2.txt, test3.txt, etc.)
- `COMPILER` file (will extract from zip)
- Any extracted `ex5/` directory (recreated by self-check)

**Keep:**
- `tests/` - Test input files
- `expected_output/` - Expected outputs
- `self-check.py` - Test runner
- `self-check-ex5.zip` - Framework archive
- `ids.txt`
- Directory structure dirs: `jflex/`, `external_jars/`, `person_a_ir_generation/`, etc.

### `/home/student/comp/` (Root)
**Remove:**
- `ziQ1qb1f` (git artifact)

**Keep:**
- `123456789.zip` - Submission zip
- `CLAUDE.md` - Onboarding guide
- `LESSONS_LEARNED.md` - Documentation
- `RECURSION_NOTES.md` - Technical notes
- `SESSION_SUMMARY.md` - Session history
- `CLEANUP_PLAN.md` - This file
- `ex5/` directory
- `.git/` - Version control

---

## 🔨 Cleanup Commands

```bash
# 1. Clean main ex5 directory
cd /home/student/comp/ex5
rm -f debug_*.s test_*.s test*.txt *.tar.gz 123456789.zip

# 2. Clean self-check-ex5 directory (THE BIG ONE)
cd /home/student/comp/ex5/self-check-ex5
rm -f test_*.s debug_*.s *.tar.gz Makefile.bak
rm -f test1.txt test2.txt test3.txt test*.txt
rm -f COMPILER
rm -f TEST_RESULTS*.md PARAMETER_PASSING_FIX.md EX4_FIX_SUMMARY.md
rm -f PERSON_A_COMPLETE.md PERSON_C_COMPLETE.md COMPLETE_COMPILER.md
rm -f TESTING_NEXT_STEPS.md ex5_summary.txt test_results.txt
rm -f *.sh  # Remove run_tests.sh, analyze_failures.sh, test_recursive.sh
rm -rf ex5/ self-check-ex5/  # Remove extracted/nested directories
rm -f 123456789.zip  # Will be copied fresh from parent

# 3. Clean root directory
cd /home/student/comp
rm -f ziQ1qb1f

# 4. Verify cleanup
echo "=== Main ex5 directory ==="
ls ex5/ | grep -E "debug_|test_|\.tar\.gz"
echo "=== self-check-ex5 directory ==="
ls ex5/self-check-ex5/ | grep -E "debug_|test_|\.tar\.gz|\.txt$|COMPILER"
echo "=== Root directory ==="
ls | grep ziQ

# 5. Copy fresh zip to self-check
cp /home/student/comp/123456789.zip /home/student/comp/ex5/self-check-ex5/

# 6. Test that self-check still works
cd /home/student/comp/ex5/self-check-ex5
python3 self-check.py 2>&1 | grep -E "Total|Passed"
```

---

## ✅ Post-Cleanup Verification

Run these to ensure nothing broke:

```bash
# 1. Verify build still works
cd /home/student/comp/ex5
make

# 2. Verify self-check works
cd self-check-ex5
python3 self-check.py 2>&1 | grep -E "Total|Passed"
# Should show: Total tests: 26, Passed: 11

# 3. Verify git is clean
cd /home/student/comp
git status
# Should only show deleted files (the cleanup)
```

---

## 📋 Post-Cleanup Commit

```bash
cd /home/student/comp
git add -A
git commit -m "Clean up repository: Remove debug files, old tarballs, duplicates

Removed:
- All debug_*.s and test_*.s files
- Old tarballs (*.tar.gz)
- Duplicate COMPILER files
- Temporary test files
- Nested/extracted directories
- Git artifacts (ziQ1qb1f)

Verified:
- Build still works (make)
- Self-check passes (11/26 tests)
- All essential files preserved

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

git push origin master
```

---

## 🎯 Expected Results

**Before cleanup:**
- ~100+ files in self-check-ex5/
- Multiple duplicate files
- Confusing directory structure

**After cleanup:**
- ~30-40 essential files in self-check-ex5/
- Clean directory structure
- Easy to navigate

**Tests:** Still 11/26 passing ✅

---

*Run this cleanup before starting work on the remaining 15 test failures!*
