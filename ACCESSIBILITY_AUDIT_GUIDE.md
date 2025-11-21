# Windows Terminal Theme Accessibility Audit Guide

## Quick Start

Run the audit tool:
```bash
python3 accessibility-audit.py
```

View the reports:
- **Summary:** Console output (immediate feedback)
- **Detailed:** `reports/accessibility-audit-report.txt`
- **Markdown:** `reports/ACCESSIBILITY_AUDIT_REPORT.md`

## Understanding WCAG AA Standards

### What is WCAG AA?

**WCAG** = Web Content Accessibility Guidelines
**AA** = Level AA compliance (industry standard)

### Contrast Requirements

| Content Type | Minimum Ratio | Visual Example |
|--------------|---------------|----------------|
| Normal text | 4.5:1 | #ffffff on #595959 |
| Large text (18pt+) | 3.0:1 | #ffffff on #767676 |
| UI components | 3.0:1 | Button borders, icons |

### Why This Matters

Users with:
- **Low vision** - Need higher contrast to read text
- **Color blindness** - Rely on luminance difference, not color
- **Age-related vision decline** - Benefit from clear contrast
- **Situational impairment** - Bright sunlight, low-quality displays

**~4% of the population** has some form of color vision deficiency.

## Reading the Audit Report

### Overall Status

```
✓ PASS   Theme Name                      (19/19)  <- All checks passed
✗ FAIL   Theme Name                      (17/19)  <- 2 checks failed
```

### Failure Details

```
• Console black on background
  Foreground: #21222c
  Background: #282a36
  Ratio: 1.11:1 (required: 4.5:1)
  Deficit: 3.39:1
  Suggested fix: Lightened #21222c → #9b9ba0
  New ratio: 5.15:1
```

**Reading this:**
1. **Problem:** "Console black" color has poor contrast
2. **Current:** 1.11:1 ratio (way below 4.5:1 requirement)
3. **Deficit:** Need 3.39 more contrast
4. **Solution:** Change `#21222c` to `#9b9ba0`
5. **Result:** Will achieve 5.15:1 (passing!)

## Common Issues and Fixes

### Issue 1: Black/BrightBlack Too Dark

**Problem:** Black colors blend into dark backgrounds
```json
"black": "#21222c",      // 1.11:1 - FAIL
"background": "#282a36"
```

**Fix:** Lighten the black to a medium gray
```json
"black": "#9b9ba0",      // 5.15:1 - PASS
"background": "#282a36"
```

**Why:** Terminal "black" isn't meant to be pure black. It's for text that needs to be readable.

### Issue 2: Red/Purple Insufficient Saturation

**Problem:** Dark saturated colors don't contrast enough
```json
"red": "#bd0013",        // 2.40:1 - FAIL
"background": "#1f1d45"
```

**Fix:** Increase brightness/lightness
```json
"red": "#d76671",        // 4.57:1 - PASS
"background": "#1f1d45"
```

**Why:** Dark red on dark background is hard to see. Lighter red maintains color while improving contrast.

### Issue 3: Selection Background Problems

**Problem:** Selection doesn't contrast with text
```json
"foreground": "#f8dcc0",
"selectionBackground": "#706b4e"  // 4.10:1 - FAIL (needs 4.5:1)
```

**Fix:** Slightly darken or lighten selection
```json
"foreground": "#fae8d6",           // Lightened foreground
"selectionBackground": "#706b4e"   // 4.50:1 - PASS
```

## Real-World Examples

### Example 1: Dracula Theme (17/19 Passing - Excellent!)

**What They Did Right:**
- ✅ Primary text: #f8f8f2 on #282a36 = 13.11:1 (way above 4.5:1!)
- ✅ Most console colors are vibrant and bright
- ✅ Cursor and selection are clearly visible

**Small Issues:**
- ❌ Black: #21222c on #282a36 = 1.11:1
- ❌ BrightBlack: #6272a4 on #282a36 = 3.03:1

**Easy Fix:**
```json
{
  "black": "#9b9ba0",       // Changed from #21222c
  "brightBlack": "#8995ba"  // Changed from #6272a4
}
```

### Example 2: Atom One Light (17/19 Passing - Excellent!)

Light themes are harder! But Atom One Light does well:
- ✅ Primary text: #383a42 on #fafafa = 10.37:1
- ✅ Most colors work on light background
- ❌ White: #fafafa on #fafafa = 1.00:1 (same color!)
- ❌ BrightWhite: #ffffff on #fafafa = 1.09:1

**Fix:**
```json
{
  "white": "#777777",       // Dark gray for "white" text on light bg
  "brightWhite": "#383a42"  // Even darker for bright variant
}
```

### Example 3: Ayu Light (3/19 Passing - Needs Work)

Light themes struggle with many colors:
- ❌ 16 failures out of 19 checks
- Primary text usually OK
- Most console colors too bright/light

**Common pattern in light theme failures:**
```json
{
  "background": "#fafafa",
  "yellow": "#f2ae49",     // 2.12:1 - FAIL
  "cyan": "#399ee6",       // 2.73:1 - FAIL
  "white": "#fafafa",      // 1.00:1 - FAIL (identical to bg!)
  "brightYellow": "#f2ae49"  // 2.12:1 - FAIL
}
```

**Requires darkening many colors:**
```json
{
  "background": "#fafafa",
  "yellow": "#a57409",     // Darkened -> 4.52:1 PASS
  "cyan": "#1273b7",       // Darkened -> 4.51:1 PASS
  "white": "#6e6e6e",      // Changed to medium gray -> 4.51:1 PASS
  "brightYellow": "#8d6107"  // Darkened -> 4.61:1 PASS
}
```

## Best Practices for Theme Authors

### 1. Test Early and Often
```bash
# After every color change
python3 accessibility-audit.py
```

### 2. Start with Background/Foreground
These are the most critical:
```json
{
  "background": "#282a36",
  "foreground": "#f8f8f2"   // Must be 4.5:1 or higher!
}
```

Test: Should be 7:1 or higher for comfort.

### 3. Console Color Strategy

**For Dark Themes:**
- Blacks: Use medium grays (#888888 to #999999)
- Colors: Use bright, saturated variants
- Whites: Can use pure white or light gray

**For Light Themes:**
- Blacks: Use dark grays (#333333 to #444444)
- Colors: Use darker, desaturated variants
- Whites: Use dark gray (#666666 to #777777) - NOT white!

### 4. Selection/Cursor Strategy

**Selection Background:**
- Should be noticeably different from main background
- Must maintain 4.5:1 with foreground
- Typical approach: Lighten dark bg by 30-40%, darken light bg by 20-30%

**Cursor:**
- Only needs 3.0:1 (easier requirement)
- High contrast is better (use bright color)
- Can match foreground if foreground has good contrast

### 5. Use Color Adjustment Suggestions

The audit suggests specific hex codes:
```
Suggested fix: Lightened #21222c → #9b9ba0
```

**You can:**
- ✅ Use the suggestion exactly
- ✅ Use it as a starting point and adjust
- ✅ Use a different approach to achieve 4.5:1
- ❌ Ignore it (your theme will fail)

## Tools and Resources

### Audit Tools Created
1. **accessibility-audit.py** - Main tool (Python, no dependencies)
2. **AccessibilityAudit.kt** - Kotlin library version
3. **AccessibilityAuditTest.kt** - Test suite

### Running Tests
```bash
# Python version (fastest)
python3 accessibility-audit.py

# Kotlin version (if Gradle available)
./gradlew :buildSrc:test --tests AccessibilityAuditTest
```

### External Tools
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Colorable](https://colorable.jxnblk.com/)
- [Contrast Ratio Calculator](https://contrast-ratio.com/)

## Frequently Asked Questions

### Q: Why did my favorite theme fail?

**A:** Most themes prioritize aesthetics over accessibility. Designers often choose colors that look good together but don't have sufficient contrast for users with vision impairments.

### Q: Will fixing these issues make my theme look worse?

**A:** Usually no! The suggested changes are often subtle:
- `#6272a4` → `#8995ba` (slightly lighter blue)
- `#21222c` → `#9b9ba0` (black becomes medium gray)

Most users won't notice the difference visually, but it makes a huge difference for accessibility.

### Q: Do I need to fix ALL failures?

**A:** Priority order:
1. **Primary text** (foreground/background) - MUST FIX
2. **Selection** - SHOULD FIX
3. **Cursor** - SHOULD FIX
4. **Console colors** - NICE TO FIX

At minimum, fix primary text and UI components.

### Q: Why is "black" so light in the suggestions?

**A:** Terminal "black" is used for **text**, not backgrounds. It needs to be visible against the dark background. Think of it as "dark gray" rather than "pure black".

### Q: Light themes are failing more. Why?

**A:** Light themes are harder because:
- Many colors are naturally dark (red, blue, purple)
- Brightening colors can make them look washed out
- "White" text on white background is obviously bad

You need to invert your color strategy for light themes.

### Q: Can I automatically apply all suggested fixes?

**A:** Not yet implemented, but possible! The audit tool provides exact hex codes for fixes. A future enhancement could auto-apply them.

## Accessibility Impact

### Who Benefits?

**Direct Beneficiaries:**
- 8% of men have some color vision deficiency
- 0.5% of women have color vision deficiency
- ~2% have low vision
- ~4% total population with vision-related accessibility needs

**Indirect Beneficiaries:**
- Users with cheap/old monitors (poor contrast)
- Users in bright sunlight (outdoor work)
- Users with eye strain/fatigue
- Everyone (higher contrast = less eye strain)

### Legal Requirements

In some contexts, WCAG AA compliance is **legally required**:
- US Section 508
- EU Web Accessibility Directive
- UK Equality Act
- Many corporate accessibility policies

Even for personal projects, it's good practice!

## Summary

### Key Takeaways

1. ✅ **All themes have room for improvement** (0/36 passed fully)
2. ✅ **Most issues are minor** (average 68% of checks pass)
3. ✅ **Fixes are straightforward** (use suggested colors or adjust)
4. ✅ **Testing is automated** (run audit script anytime)
5. ✅ **Impact is significant** (helps 4%+ of users)

### Next Steps

1. **Review** your theme's audit results
2. **Apply** suggested fixes for critical issues
3. **Test** with the audit tool
4. **Iterate** until all checks pass
5. **Share** your accessible theme!

---

**Remember:** Accessibility isn't about perfection, it's about inclusion. Every improvement helps!
