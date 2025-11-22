# Manual Testing Instructions for Sprint 6

## Overview
This document provides step-by-step instructions for performing manual testing of the Windows Terminal Theme for JetBrains plugin with the new editor color scheme registration feature.

**What you're testing:** The ability for editor color schemes to be automatically applied when selecting a UI theme.

**Expected outcome:** When you select a Windows Terminal theme (like "wt-dracula"), both the UI and the editor color scheme should change automatically - no manual color scheme selection needed.

---

## Prerequisites

### Required Software
- IntelliJ IDEA (Community or Ultimate Edition)
  - Recommended version: 2021.3.1 or later
  - Any JetBrains IDE will work (PyCharm, WebStorm, etc.)

### Test Artifact Location
- **Plugin ZIP file:** `/home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip`
- **File size:** 2.8 MB
- **Build date:** 2025-11-22

### Before You Start
1. Make sure you have IntelliJ IDEA installed
2. Have a sample project open (any programming language)
3. Have some source files to test syntax highlighting (recommended: .kt, .java, .py files)
4. Note your current theme settings (you may want to restore them later)

---

## Testing Procedure

### Part 1: Plugin Installation (5 minutes)

**Step 1.1: Open Plugin Settings**
1. Launch IntelliJ IDEA
2. Go to **File -> Settings** (Windows/Linux) or **IntelliJ IDEA -> Preferences** (macOS)
3. Navigate to **Plugins** in the left sidebar

**Step 1.2: Install Plugin from Disk**
1. Click the gear icon (⚙) at the top of the Plugins page
2. Select **Install Plugin from Disk...**
3. Navigate to: `/home/bithons/github/jetbrains-melly-theme/build/distributions/`
4. Select the file: `one-dark-theme.zip`
5. Click **OK**

**Step 1.3: Restart IDE**
1. You should see a message: "Plugin 'Windows Terminal Theme for JetBrains' was successfully installed"
2. Click **Restart IDE** when prompted
3. Wait for IntelliJ IDEA to restart

**Step 1.4: Verify Installation**
1. After restart, go back to **Settings -> Plugins**
2. Switch to the **Installed** tab
3. Verify "Windows Terminal Theme for JetBrains" appears in the list
4. Check that there are no error notifications in the bottom-right corner

**✅ Check in SPRINT_6_TEST_RESULTS.md:**
- [ ] Plugin installs without errors
- [ ] No error notifications after IDE restart
- [ ] Plugin appears in Settings -> Plugins

---

### Part 2: Verify Editor Color Schemes Are Visible (5 minutes)

**Step 2.1: Open Color Scheme Settings**
1. Go to **Settings -> Editor -> Color Scheme**
2. Click on the **Scheme** dropdown at the top

**Step 2.2: Look for Windows Terminal Themes**
1. Scroll through the dropdown list
2. Look for themes starting with "wt-" prefix
3. You should see at least 57 Windows Terminal themes

**Step 2.3: Verify Key Themes**
Look for these specific themes (check them off as you find them):
- [ ] wt-dracula
- [ ] wt-nord
- [ ] wt-tokyo-night-storm
- [ ] wt-gruvbox-dark
- [ ] wt-material
- [ ] wt-atom

**✅ Check in SPRINT_6_TEST_RESULTS.md:**
- [ ] Editor color schemes visible in Settings -> Editor -> Color Scheme
- [ ] All Windows Terminal color schemes appear in dropdown
- [ ] Verified presence of key themes (list them)

**📸 SCREENSHOT 1:** Take a screenshot of the Color Scheme dropdown showing the Windows Terminal themes

---

### Part 3: Test Automatic Application (10 minutes)

This is the CRITICAL test - verifying that editor color schemes automatically apply when selecting a UI theme.

**Step 3.1: Open UI Theme Settings**
1. Go to **Settings -> Appearance & Behavior -> Appearance**
2. Find the **Theme** dropdown

**Step 3.2: Select a Windows Terminal Theme**
1. Click the Theme dropdown
2. Select **"wt-dracula"** (or another wt-* theme of your choice)
3. Click **Apply** (do NOT close the settings window yet)

**Step 3.3: Verify Automatic Editor Scheme Change**
1. Without closing Settings, navigate to **Editor -> Color Scheme**
2. Look at the **Scheme** dropdown
3. **EXPECTED:** The scheme should now show "wt-dracula" (matching the UI theme you selected)
4. **CRITICAL:** You should NOT have had to manually select the editor color scheme

**Step 3.4: Test with Another Theme**
1. Go back to **Appearance & Behavior -> Appearance**
2. Select a different Windows Terminal theme (e.g., "wt-nord")
3. Click **Apply**
4. Navigate to **Editor -> Color Scheme**
5. **EXPECTED:** The scheme should now show "wt-nord"

**Step 3.5: Test with a Third Theme**
1. Repeat the above steps with one more theme (e.g., "wt-tokyo-night-storm")
2. Verify the editor scheme updates automatically

**✅ Check in SPRINT_6_TEST_RESULTS.md:**
- [ ] Selected a Windows Terminal UI theme
- [ ] Editor color scheme automatically updated to match
- [ ] No manual color scheme selection needed
- [ ] Theme switch is instant and seamless
- [ ] Tested with at least 3 different themes

**📸 SCREENSHOT 2:** Settings -> Appearance showing selected theme

---

### Part 4: Test Syntax Highlighting (10 minutes)

**Step 4.1: Select a Test Theme**
1. Go to **Settings -> Appearance & Behavior -> Appearance**
2. Select **"wt-dracula"** theme
3. Click **OK** to close Settings

**Step 4.2: Open a Source File**
1. Open any source code file in your project
2. Recommended file types: .kt (Kotlin), .java (Java), .py (Python), .js (JavaScript)
3. If you don't have a project, create a new file with some sample code

**Step 4.3: Verify Syntax Highlighting**
Check that the following elements are colored distinctly:
- [ ] **Keywords** (e.g., `fun`, `class`, `if`, `def`, `function`)
- [ ] **Strings** (e.g., `"hello world"`)
- [ ] **Comments** (e.g., `// comment` or `# comment`)
- [ ] **Functions/Methods** (e.g., function names)
- [ ] **Variables**
- [ ] **Numbers**
- [ ] **Operators** (e.g., `+`, `-`, `=`)

**Step 4.4: Compare with Another Theme**
1. Switch to **"wt-nord"** theme (Settings -> Appearance -> Theme)
2. Verify the syntax colors change to match the new theme
3. All elements should still be clearly colored and distinct

**✅ Check in SPRINT_6_TEST_RESULTS.md:**
- [ ] Opened source files
- [ ] Keywords are colored correctly
- [ ] Strings are colored correctly
- [ ] Comments are colored correctly
- [ ] Functions/methods are colored correctly
- [ ] All syntax elements have distinct, visible colors

**📸 SCREENSHOT 3:** Code editor showing syntax highlighting with one of the themes

---

### Part 5: Test Console Colors (5 minutes)

**Step 5.1: Open Terminal**
1. Go to **View -> Tool Windows -> Terminal**
2. This will open the embedded terminal at the bottom of the IDE

**Step 5.2: Test Colored Output**
Run one or more of these commands:
```bash
# Linux/macOS
ls --color=auto
echo -e "\033[31mRed\033[0m \033[32mGreen\033[0m \033[33mYellow\033[0m \033[34mBlue\033[0m"

# Or if you have grep
grep --color=auto "pattern" somefile.txt
```

**Step 5.3: Verify Colors Display**
1. Check that directory listings show different colors for files vs directories
2. Verify that ANSI color codes display correctly
3. Colors should match the Windows Terminal theme palette

**Step 5.4: Test with Different Theme**
1. Switch to a different Windows Terminal theme
2. Re-run the color test commands
3. Verify colors change to match the new theme

**✅ Check in SPRINT_6_TEST_RESULTS.md:**
- [ ] Opened terminal in IntelliJ
- [ ] Ran command with colored output
- [ ] ANSI colors display correctly
- [ ] Terminal colors match Windows Terminal color scheme

---

### Part 6: Multiple Theme Switching Test (5 minutes)

**Step 6.1: Rapid Theme Switching**
Switch between these themes in quick succession:
1. wt-dracula
2. wt-nord
3. wt-gruvbox-dark
4. wt-material

**Step 6.2: Verify for Each Switch**
For each theme switch, verify:
- [ ] UI changes immediately
- [ ] Editor color scheme updates automatically
- [ ] No error notifications appear
- [ ] IDE remains responsive

**Step 6.3: Check Error Log**
1. Go to **Help -> Show Log in File Manager**
2. Open the log file (idea.log)
3. Search for any errors or warnings containing "theme" or "color scheme"
4. **EXPECTED:** No errors related to theme switching

**✅ Check in SPRINT_6_TEST_RESULTS.md:**
- [ ] Switched between at least 3 different themes
- [ ] Editor color scheme updated automatically for each switch
- [ ] No errors or warnings appeared
- [ ] Each theme displays correctly

---

## Automated Verification Commands

These commands can be run to verify the build before manual testing:

### Verify Plugin Artifact
```bash
ls -lh /home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip
```
**Expected:** File exists, size ~2.8M

### Count Theme Files in Build
```bash
unzip -l /home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip | grep -c "themes/wt-.*\.xml"
```
**Expected:** 57

```bash
unzip -l /home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip | grep -c "themes/wt-.*\.theme\.json"
```
**Expected:** 57

### Verify Dual Registration in plugin.xml
```bash
# Extract and check plugin.xml from the built artifact
cd /tmp
unzip -q /home/bithons/github/jetbrains-melly-theme/build/distributions/one-dark-theme.zip
unzip -p one-dark-theme/lib/instrumented-one-dark-theme.jar META-INF/plugin.xml | grep -c "themeProvider"
```
**Expected:** 57

```bash
unzip -p /tmp/one-dark-theme/lib/instrumented-one-dark-theme.jar META-INF/plugin.xml | grep -c "bundledColorScheme"
```
**Expected:** 57

---

## Troubleshooting

### Plugin Won't Install
- **Issue:** Error message when installing from disk
- **Solution:**
  - Verify the ZIP file is not corrupted (check file size is ~2.8M)
  - Try extracting the ZIP manually to verify contents
  - Restart IntelliJ IDEA and try again

### Themes Don't Appear
- **Issue:** No Windows Terminal themes in the theme dropdown
- **Solution:**
  - Verify plugin is enabled in Settings -> Plugins
  - Restart IDE
  - Check idea.log for errors

### Editor Scheme Doesn't Auto-Apply
- **Issue:** Editor color scheme doesn't change when selecting UI theme
- **Solution:**
  - This is a critical bug - document in test results
  - Verify you're selecting a "wt-*" theme (not other themes)
  - Check if bundledColorScheme entries are in plugin.xml

### Syntax Highlighting Not Working
- **Issue:** Code appears in plain text without colors
- **Solution:**
  - Go to Settings -> Editor -> Color Scheme
  - Verify a Windows Terminal scheme is selected
  - Try selecting a different scheme and switching back

---

## Completing the Test

### Fill Out Test Results
1. Open **SPRINT_6_TEST_RESULTS.md**
2. Check off each completed test item
3. Fill in your name, IDE version, and date
4. Document any issues found
5. Add screenshot file paths

### Report Results
After completing all tests:
1. Save the completed SPRINT_6_TEST_RESULTS.md
2. If all tests passed, mark "Overall Test Status: PASS"
3. If any tests failed, mark "Overall Test Status: FAIL" and detail the issues
4. Add any additional comments or observations

---

## Expected Time
- **Total testing time:** 40-50 minutes
- **Installation:** 5 minutes
- **Visibility test:** 5 minutes
- **Automatic application:** 10 minutes
- **Syntax highlighting:** 10 minutes
- **Console colors:** 5 minutes
- **Multiple themes:** 5 minutes
- **Documentation:** 5-10 minutes

---

## Questions or Issues?
If you encounter any problems during testing, document them in the "Issues Found" section of SPRINT_6_TEST_RESULTS.md with as much detail as possible:
- What you were doing when the issue occurred
- Expected behavior vs actual behavior
- Any error messages (copy full text)
- Screenshots if applicable

---

**Good luck with testing!**
