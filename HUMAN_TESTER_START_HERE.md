# 👤 HUMAN TESTER - START HERE

## You've Been Asked to Test the Sprint 6 Plugin

This is a **manual testing task** that requires a human with IntelliJ IDEA to verify the plugin works correctly.

---

## ⚡ Quick Start (Do This First)

### Step 1: Verify the Build
```bash
cd /home/bithons/github/jetbrains-melly-theme
./verify_build.sh
```

**Expected:** All checks pass with ✅ green checkmarks

**If this fails:** Don't proceed - the build is broken. Report the error.

### Step 2: Read the Quick Start Guide
```bash
cat TESTING_QUICK_START.md
# Or open it in your editor
```

This gives you a 5-minute overview of what you need to test.

### Step 3: Start Testing
Open these two files side-by-side:
- **MANUAL_TESTING_INSTRUCTIONS.md** - Step-by-step what to do
- **SPRINT_6_TEST_RESULTS.md** - Checklist to fill out

Follow the instructions and check off items as you complete them.

---

## 📁 Files You Need

| File | What It Is | When to Use It |
|------|-----------|----------------|
| **THIS FILE** | You're reading it now | Start here |
| **verify_build.sh** | Automated checks | Run first (2 min) |
| **TESTING_QUICK_START.md** | Quick overview | Read second (5 min) |
| **MANUAL_TESTING_INSTRUCTIONS.md** | Detailed testing steps | Use during testing (40 min) |
| **SPRINT_6_TEST_RESULTS.md** | Checklist to complete | Fill out as you test |
| **TESTING_PREPARATION_SUMMARY.md** | What was automated | Reference if needed |

---

## 🎯 What You're Testing

**The Goal:** When you select a UI theme (like "wt-dracula"), the editor color scheme should AUTOMATICALLY change to match it.

**Before Sprint 6:** Users had to manually select both the UI theme AND the editor color scheme separately.

**After Sprint 6:** Selecting a UI theme automatically applies the matching editor color scheme.

**Your Job:** Verify this actually works in a real IntelliJ IDEA installation.

---

## 🚀 Testing Workflow

```
1. Run verify_build.sh
   ↓
2. Install plugin in IntelliJ IDEA
   ↓
3. Test automatic color scheme application
   ↓
4. Test syntax highlighting
   ↓
5. Test with multiple themes
   ↓
6. Fill out test results
   ↓
7. Report PASS or FAIL
```

**Total Time:** ~45 minutes

---

## ✅ How to Know If It Passes

The plugin **PASSES** if:
- Plugin installs without errors
- You can see 57 "wt-*" color schemes in Settings → Editor → Color Scheme
- **When you select a UI theme, the editor color scheme changes automatically**
- Syntax highlighting works (keywords, strings, comments are colored)
- No error messages appear

The plugin **FAILS** if:
- Plugin won't install or crashes
- Color schemes don't appear
- **Editor scheme doesn't auto-change** (you have to manually select it)
- Syntax highlighting is broken
- Errors appear when switching themes

---

## 📍 Plugin Location

**Install this file in IntelliJ IDEA:**
```
/home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip
```

**Size:** 2.8 MB

**How to Install:**
1. IntelliJ IDEA → Settings → Plugins
2. Gear icon → Install Plugin from Disk
3. Select the ZIP file above
4. Restart IDE

---

## 🆘 Need Help?

**Problem:** Build verification fails
- **Solution:** Report the error - don't proceed with testing

**Problem:** Don't know what to test
- **Solution:** Read TESTING_QUICK_START.md

**Problem:** Need detailed steps
- **Solution:** Follow MANUAL_TESTING_INSTRUCTIONS.md

**Problem:** Don't know where to write results
- **Solution:** Fill out SPRINT_6_TEST_RESULTS.md

**Problem:** Found a bug
- **Solution:** Document it in the "Issues Found" section with full details

---

## 📸 Screenshots Needed

Take these 3 screenshots during testing:

1. **Color Scheme dropdown** showing Windows Terminal themes
   - Settings → Editor → Color Scheme → (click dropdown)

2. **Code editor** with syntax highlighting active
   - Open any .java/.kt/.py file

3. **Theme settings** showing selected theme
   - Settings → Appearance & Behavior → Appearance

Save screenshots and note their locations in SPRINT_6_TEST_RESULTS.md

---

## 📝 Reporting Results

When you're done testing:

1. **Open:** SPRINT_6_TEST_RESULTS.md
2. **Fill in:**
   - Your name
   - IDE version
   - Date tested
   - Check off all completed items
   - Document any issues
   - Add screenshot paths
3. **Mark overall status:** PASS or FAIL
4. **Save** the file

---

## ⏱️ Time Estimate

- Verify build: 2 minutes
- Read quick start: 5 minutes
- Install plugin: 5 minutes
- Test features: 30 minutes
- Document results: 5 minutes
- **Total: ~45 minutes**

---

## 🎬 Ready to Start?

### Do This Now:

```bash
# 1. Go to the project directory
cd /home/bithons/github/jetbrains-melly-theme

# 2. Verify the build is correct
./verify_build.sh

# 3. Read the quick start guide
cat TESTING_QUICK_START.md

# 4. Open the testing instructions
# (in your editor of choice)
```

Then follow MANUAL_TESTING_INSTRUCTIONS.md step-by-step!

---

## ✨ Important Notes

- This is **manual testing** - automated tests can't verify UI behavior
- The **critical test** is automatic color scheme application
- Take your time and document everything
- If something doesn't work, that's valuable information - report it!

---

**You've got this! The framework is prepared - all we need is your human verification.** 🚀

**Questions?** All the answers are in the documentation files listed above.

**Good luck testing!** 🎉
