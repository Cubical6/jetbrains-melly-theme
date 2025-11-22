# Sprint 6 Testing Documentation Index

This directory contains the complete testing framework for Sprint 6's editor color scheme registration feature.

## For Human Testers

**START HERE:** [HUMAN_TESTER_START_HERE.md](HUMAN_TESTER_START_HERE.md)

This is your entry point. It will guide you through the entire testing process.

---

## Quick Links

### Testing Documents (Read in Order)

1. **[HUMAN_TESTER_START_HERE.md](HUMAN_TESTER_START_HERE.md)** - Start here!
2. **[TESTING_QUICK_START.md](TESTING_QUICK_START.md)** - Quick overview (5 min read)
3. **[MANUAL_TESTING_INSTRUCTIONS.md](MANUAL_TESTING_INSTRUCTIONS.md)** - Detailed steps (use during testing)
4. **[SPRINT_6_TEST_RESULTS.md](SPRINT_6_TEST_RESULTS.md)** - Checklist to fill out

### Verification Tools

- **[verify_build.sh](verify_build.sh)** - Run this first to verify build
  ```bash
  ./verify_build.sh
  ```

### Background Information

- **[TESTING_PREPARATION_SUMMARY.md](TESTING_PREPARATION_SUMMARY.md)** - What was automated vs manual
- **[TASK_3_COMPLETION_REPORT.md](TASK_3_COMPLETION_REPORT.md)** - Task completion report

---

## Quick Start

```bash
# 1. Verify the build
./verify_build.sh

# 2. Read the entry point
cat HUMAN_TESTER_START_HERE.md

# 3. Start testing
# Follow MANUAL_TESTING_INSTRUCTIONS.md
# Fill out SPRINT_6_TEST_RESULTS.md as you go
```

---

## Plugin Location

**Install this file in IntelliJ IDEA:**

```
/home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip
```

**Size:** 2.8 MB  
**Build Date:** 2025-11-22

---

## What You're Testing

**Goal:** Verify that editor color schemes automatically apply when selecting a UI theme.

**Expected Behavior:** 
- Select "wt-dracula" UI theme
- Editor color scheme automatically changes to "wt-dracula"
- No manual color scheme selection needed

**This is the core feature of Sprint 6.**

---

## Time Required

- **Build verification:** 2 minutes
- **Reading docs:** 10 minutes
- **Testing:** 30 minutes
- **Documentation:** 5 minutes
- **Total:** ~45 minutes

---

## Need Help?

- **Don't know where to start?** → Read [HUMAN_TESTER_START_HERE.md](HUMAN_TESTER_START_HERE.md)
- **Build verification failing?** → Check output of `./verify_build.sh`
- **Need detailed steps?** → Follow [MANUAL_TESTING_INSTRUCTIONS.md](MANUAL_TESTING_INSTRUCTIONS.md)
- **Where to document results?** → Fill out [SPRINT_6_TEST_RESULTS.md](SPRINT_6_TEST_RESULTS.md)

---

## File Structure

```
Testing Documentation:
├── README_TESTING.md                    ← This file (index)
├── HUMAN_TESTER_START_HERE.md          ← Entry point
├── TESTING_QUICK_START.md              ← Quick overview
├── MANUAL_TESTING_INSTRUCTIONS.md      ← Detailed guide
├── SPRINT_6_TEST_RESULTS.md            ← Fill this out
├── TESTING_PREPARATION_SUMMARY.md      ← Background
├── TASK_3_COMPLETION_REPORT.md         ← Task report
└── verify_build.sh                     ← Verification script

Plugin to Test:
└── build/distributions/one-dark-theme.zip
```

---

## Status

- **Framework Preparation:** ✅ COMPLETE
- **Automated Checks:** ✅ COMPLETE (all passing)
- **Manual Testing:** ⏳ READY FOR HUMAN
- **Plugin Build:** ✅ VERIFIED (2.8 MB, 57 themes, dual registration)

---

**Ready to test? Start with [HUMAN_TESTER_START_HERE.md](HUMAN_TESTER_START_HERE.md)!**
