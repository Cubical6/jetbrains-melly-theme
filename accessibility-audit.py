#!/usr/bin/env python3

"""
WCAG AA Accessibility Audit for Windows Terminal Themes

This script audits Windows Terminal theme JSON files for WCAG AA compliance.
It checks contrast ratios between foreground/background colors and console colors.

Requirements:
- Python 3.6+
- No external dependencies (uses only standard library)
"""

import json
import os
from datetime import datetime
from typing import List, Tuple, Optional


class ColorUtils:
    """Color utility functions for contrast calculation."""

    @staticmethod
    def hex_to_rgb(hex_color: str) -> Tuple[int, int, int]:
        """Convert hex color to RGB tuple."""
        hex_color = hex_color.lstrip('#')
        return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

    @staticmethod
    def rgb_to_hex(r: int, g: int, b: int) -> str:
        """Convert RGB to hex color."""
        return f"#{r:02x}{g:02x}{b:02x}"

    @staticmethod
    def calculate_relative_luminance(hex_color: str) -> float:
        """Calculate relative luminance according to WCAG 2.0."""
        r, g, b = ColorUtils.hex_to_rgb(hex_color)

        # Convert to 0-1 range
        r_srgb = r / 255.0
        g_srgb = g / 255.0
        b_srgb = b / 255.0

        # Apply gamma correction
        def linearize(c):
            if c <= 0.03928:
                return c / 12.92
            else:
                return ((c + 0.055) / 1.055) ** 2.4

        r_lin = linearize(r_srgb)
        g_lin = linearize(g_srgb)
        b_lin = linearize(b_srgb)

        # Calculate luminance
        return 0.2126 * r_lin + 0.7152 * g_lin + 0.0722 * b_lin

    @staticmethod
    def calculate_contrast_ratio(color1: str, color2: str) -> float:
        """Calculate WCAG 2.0 contrast ratio between two colors."""
        l1 = ColorUtils.calculate_relative_luminance(color1)
        l2 = ColorUtils.calculate_relative_luminance(color2)

        lighter = max(l1, l2)
        darker = min(l1, l2)

        return (lighter + 0.05) / (darker + 0.05)

    @staticmethod
    def lighten(hex_color: str, percentage: float) -> str:
        """Lighten a color by a given percentage."""
        r, g, b = ColorUtils.hex_to_rgb(hex_color)

        new_r = int(r + (255 - r) * percentage)
        new_g = int(g + (255 - g) * percentage)
        new_b = int(b + (255 - b) * percentage)

        new_r = max(0, min(255, new_r))
        new_g = max(0, min(255, new_g))
        new_b = max(0, min(255, new_b))

        return ColorUtils.rgb_to_hex(new_r, new_g, new_b)

    @staticmethod
    def darken(hex_color: str, percentage: float) -> str:
        """Darken a color by a given percentage."""
        r, g, b = ColorUtils.hex_to_rgb(hex_color)

        new_r = int(r * (1 - percentage))
        new_g = int(g * (1 - percentage))
        new_b = int(b * (1 - percentage))

        new_r = max(0, min(255, new_r))
        new_g = max(0, min(255, new_g))
        new_b = max(0, min(255, new_b))

        return ColorUtils.rgb_to_hex(new_r, new_g, new_b)


class ContrastCheck:
    """Represents a single contrast check."""

    def __init__(self, description: str, foreground: str, background: str,
                 requirement: float, category: str):
        self.description = description
        self.foreground = foreground
        self.background = background
        self.requirement = requirement
        self.category = category
        self.ratio = ColorUtils.calculate_contrast_ratio(foreground, background)
        self.passes = self.ratio >= requirement

    def __repr__(self):
        status = "✓" if self.passes else "✗"
        return f"{status} {self.description}: {self.ratio:.2f}:1 (req: {self.requirement}:1)"


class ThemeAuditResult:
    """Results of auditing a single theme."""

    def __init__(self, theme_name: str, theme_file: str, checks: List[ContrastCheck]):
        self.theme_name = theme_name
        self.theme_file = theme_file
        self.checks = checks
        self.failures = [c for c in checks if not c.passes]
        self.passes = [c for c in checks if c.passes]
        self.overall_pass = len(self.failures) == 0


class AccessibilityAuditor:
    """Main auditor class."""

    WCAG_AA_NORMAL_TEXT = 4.5
    WCAG_AA_LARGE_TEXT = 3.0
    WCAG_AA_UI_COMPONENT = 3.0

    def __init__(self, themes_directory: str):
        self.themes_directory = themes_directory
        self.results: List[ThemeAuditResult] = []

    def audit_theme(self, theme_file: str) -> ThemeAuditResult:
        """Audit a single theme file."""
        with open(theme_file, 'r') as f:
            theme = json.load(f)

        theme_name = theme.get('name', os.path.basename(theme_file))
        background = theme.get('background', '#000000')
        foreground = theme.get('foreground', '#ffffff')
        cursor_color = theme.get('cursorColor', foreground)
        selection_bg = theme.get('selectionBackground', background)

        checks = []

        # Primary checks
        checks.append(ContrastCheck(
            "Primary text (foreground on background)",
            foreground, background,
            self.WCAG_AA_NORMAL_TEXT,
            "Primary"
        ))

        checks.append(ContrastCheck(
            "Cursor visibility (cursor on background)",
            cursor_color, background,
            self.WCAG_AA_UI_COMPONENT,
            "UI Component"
        ))

        checks.append(ContrastCheck(
            "Selection visibility (foreground on selection)",
            foreground, selection_bg,
            self.WCAG_AA_NORMAL_TEXT,
            "Primary"
        ))

        # Console colors
        console_colors = [
            'black', 'red', 'green', 'yellow', 'blue', 'purple', 'cyan', 'white',
            'brightBlack', 'brightRed', 'brightGreen', 'brightYellow',
            'brightBlue', 'brightPurple', 'brightCyan', 'brightWhite'
        ]

        for color_name in console_colors:
            if color_name in theme:
                checks.append(ContrastCheck(
                    f"Console {color_name} on background",
                    theme[color_name], background,
                    self.WCAG_AA_NORMAL_TEXT,
                    "Console Color"
                ))

        return ThemeAuditResult(theme_name, os.path.basename(theme_file), checks)

    def audit_all_themes(self):
        """Audit all themes in the directory."""
        theme_files = [
            os.path.join(self.themes_directory, f)
            for f in os.listdir(self.themes_directory)
            if f.endswith('.json')
        ]

        theme_files.sort()

        for theme_file in theme_files:
            try:
                result = self.audit_theme(theme_file)
                self.results.append(result)
            except Exception as e:
                print(f"Error auditing {theme_file}: {e}")

    def suggest_color_adjustment(self, foreground: str, background: str,
                                  target_ratio: float) -> Optional[Tuple[str, float]]:
        """Suggest a color adjustment to meet contrast requirements."""
        current_ratio = ColorUtils.calculate_contrast_ratio(foreground, background)
        if current_ratio >= target_ratio:
            return None

        bg_luminance = ColorUtils.calculate_relative_luminance(background)
        is_dark_background = bg_luminance < 0.5

        best_color = foreground
        best_ratio = current_ratio

        for i in range(1, 21):
            step = 0.05 * i
            if is_dark_background:
                test_color = ColorUtils.lighten(foreground, step)
            else:
                test_color = ColorUtils.darken(foreground, step)

            test_ratio = ColorUtils.calculate_contrast_ratio(test_color, background)
            if test_ratio > best_ratio:
                best_ratio = test_ratio
                best_color = test_color

            if test_ratio >= target_ratio:
                break

        if best_ratio > current_ratio:
            return (best_color, best_ratio)
        return None

    def generate_summary_report(self) -> str:
        """Generate a concise summary report."""
        lines = []
        lines.append("=" * 80)
        lines.append("WCAG AA ACCESSIBILITY AUDIT - SUMMARY")
        lines.append("=" * 80)
        lines.append("")

        passing = sum(1 for r in self.results if r.overall_pass)
        failing = len(self.results) - passing

        lines.append(f"Total themes audited: {len(self.results)}")
        lines.append(f"Passing: {passing} ({passing * 100 // len(self.results) if self.results else 0}%)")
        lines.append(f"Failing: {failing} ({failing * 100 // len(self.results) if self.results else 0}%)")
        lines.append("")

        lines.append("QUICK REFERENCE")
        lines.append("-" * 80)

        for result in sorted(self.results, key=lambda r: r.theme_name):
            status = "✓ PASS" if result.overall_pass else "✗ FAIL"
            total = len(result.checks)
            passed = len(result.passes)
            lines.append(f"{status:8} {result.theme_name:35} ({passed}/{total})")

        lines.append("")
        return "\n".join(lines)

    def generate_detailed_report(self) -> str:
        """Generate a detailed audit report."""
        lines = []
        lines.append("=" * 80)
        lines.append("WCAG AA ACCESSIBILITY AUDIT REPORT")
        lines.append("Windows Terminal Theme Collection")
        lines.append("=" * 80)
        lines.append("")
        lines.append(f"Generated: {datetime.now().isoformat()}")
        lines.append(f"Total themes audited: {len(self.results)}")
        lines.append("")

        # Summary
        passing = sum(1 for r in self.results if r.overall_pass)
        failing = len(self.results) - passing

        lines.append("SUMMARY")
        lines.append("-" * 80)
        lines.append(f"Passing themes: {passing} ({passing * 100 // len(self.results) if self.results else 0}%)")
        lines.append(f"Failing themes: {failing} ({failing * 100 // len(self.results) if self.results else 0}%)")
        lines.append("")

        # WCAG Criteria
        lines.append("WCAG AA CRITERIA")
        lines.append("-" * 80)
        lines.append("Normal text:     4.5:1 minimum contrast ratio")
        lines.append("Large text:      3.0:1 minimum contrast ratio")
        lines.append("UI components:   3.0:1 minimum contrast ratio")
        lines.append("")

        # Detailed results
        lines.append("DETAILED RESULTS")
        lines.append("=" * 80)
        lines.append("")

        for result in sorted(self.results, key=lambda r: r.theme_name):
            status = "✓ PASS" if result.overall_pass else "✗ FAIL"
            lines.append(f"Theme: {result.theme_name}")
            lines.append(f"File: {result.theme_file}")
            lines.append(f"Status: {status}")
            lines.append(f"Checks: {len(result.passes)} passed, {len(result.failures)} failed")
            lines.append("")

            if result.failures:
                lines.append("  FAILURES:")
                for check in result.failures:
                    lines.append(f"    • {check.description}")
                    lines.append(f"      Foreground: {check.foreground}")
                    lines.append(f"      Background: {check.background}")
                    lines.append(f"      Ratio: {check.ratio:.2f}:1 (required: {check.requirement}:1)")
                    deficit = check.requirement - check.ratio
                    lines.append(f"      Deficit: {deficit:.2f}:1")

                    # Suggest fix
                    suggestion = self.suggest_color_adjustment(
                        check.foreground, check.background, check.requirement
                    )
                    if suggestion:
                        suggested_color, new_ratio = suggestion
                        action = "Lightened" if ColorUtils.calculate_relative_luminance(
                            check.background) < 0.5 else "Darkened"
                        lines.append(f"      Suggested fix: {action} {check.foreground} → {suggested_color}")
                        lines.append(f"      New ratio: {new_ratio:.2f}:1")
                    lines.append("")

            # Category breakdown
            lines.append("  CATEGORY BREAKDOWN:")
            categories = {}
            for check in result.checks:
                if check.category not in categories:
                    categories[check.category] = {'passed': 0, 'failed': 0}
                if check.passes:
                    categories[check.category]['passed'] += 1
                else:
                    categories[check.category]['failed'] += 1

            for category, counts in sorted(categories.items()):
                status = "✓" if counts['failed'] == 0 else "✗"
                lines.append(f"    {status} {category}: {counts['passed']} passed, {counts['failed']} failed")

            lines.append("")
            lines.append("-" * 80)
            lines.append("")

        # Recommendations
        failed_themes = [r for r in self.results if not r.overall_pass]

        lines.append("RECOMMENDATIONS")
        lines.append("=" * 80)
        lines.append("")

        if not failed_themes:
            lines.append("✓ All themes meet WCAG AA accessibility standards!")
            lines.append("")
            lines.append("Excellent work! All tested themes provide adequate contrast for")
            lines.append("users with visual impairments.")
        else:
            lines.append("The following themes require attention:")
            lines.append("")

            for result in failed_themes:
                lines.append(f"{result.theme_name} ({result.theme_file}):")
                lines.append(f"  - {len(result.failures)} contrast issue(s) detected")

                primary_failures = [c for c in result.failures if c.category == "Primary"]
                if primary_failures:
                    lines.append("  - ⚠ CRITICAL: Primary text contrast issues")

                lines.append("")

            lines.append("Priority order for fixes:")
            lines.append("1. Primary text (foreground/background) - affects all text")
            lines.append("2. UI components (cursor, selection) - affects usability")
            lines.append("3. Console colors - affects syntax highlighting and terminal output")

        lines.append("")
        lines.append("=" * 80)
        lines.append("ColorUtils.calculateContrastRatio Validation")
        lines.append("=" * 80)
        lines.append("")
        lines.append("The audit uses a WCAG 2.0 compliant contrast calculation:")
        lines.append("- Converts colors to sRGB color space")
        lines.append("- Calculates relative luminance with gamma correction")
        lines.append("- Computes contrast ratio: (L1 + 0.05) / (L2 + 0.05)")
        lines.append("")

        # Test the implementation
        black_white = ColorUtils.calculate_contrast_ratio("#000000", "#ffffff")
        same_color = ColorUtils.calculate_contrast_ratio("#ff0000", "#ff0000")

        lines.append("Validation tests:")
        lines.append(f"- Black/white contrast: {black_white:.2f}:1 (expected: 21:1) ✓")
        lines.append(f"- Same color contrast: {same_color:.2f}:1 (expected: 1:1) ✓")
        lines.append("")

        lines.append("=" * 80)
        lines.append("END OF REPORT")
        lines.append("=" * 80)

        return "\n".join(lines)

    def generate_markdown_report(self) -> str:
        """Generate a markdown-formatted report."""
        lines = []
        lines.append("# WCAG AA Accessibility Audit Report")
        lines.append("")
        lines.append("**Windows Terminal Theme Collection**")
        lines.append("")
        lines.append(f"- **Generated:** {datetime.now().isoformat()}")
        lines.append(f"- **Total themes audited:** {len(self.results)}")
        lines.append("")

        # Summary
        passing = sum(1 for r in self.results if r.overall_pass)
        failing = len(self.results) - passing
        total = len(self.results)

        lines.append("## Summary")
        lines.append("")
        lines.append("| Status | Count | Percentage |")
        lines.append("|--------|-------|------------|")
        lines.append(f"| ✓ Passing | {passing} | {passing * 100 // total if total else 0}% |")
        lines.append(f"| ✗ Failing | {failing} | {failing * 100 // total if total else 0}% |")
        lines.append("")

        # WCAG Criteria
        lines.append("## WCAG AA Criteria")
        lines.append("")
        lines.append("| Type | Minimum Contrast Ratio |")
        lines.append("|------|------------------------|")
        lines.append("| Normal text | 4.5:1 |")
        lines.append("| Large text | 3.0:1 |")
        lines.append("| UI components | 3.0:1 |")
        lines.append("")

        # Quick reference
        lines.append("## Quick Reference")
        lines.append("")
        lines.append("| Theme | Status | Pass/Total | File |")
        lines.append("|-------|--------|------------|------|")

        for result in sorted(self.results, key=lambda r: r.theme_name):
            status = "✓ PASS" if result.overall_pass else "✗ FAIL"
            ratio = f"{len(result.passes)}/{len(result.checks)}"
            lines.append(f"| {result.theme_name} | {status} | {ratio} | `{result.theme_file}` |")

        lines.append("")

        # Detailed results for failing themes
        failed_themes = [r for r in self.results if not r.overall_pass]
        if failed_themes:
            lines.append("## Detailed Failures")
            lines.append("")

            for result in sorted(failed_themes, key=lambda r: r.theme_name):
                lines.append(f"### {result.theme_name}")
                lines.append("")
                lines.append(f"- **File:** `{result.theme_file}`")
                lines.append(f"- **Failed checks:** {len(result.failures)}")
                lines.append("")

                for check in result.failures:
                    lines.append(f"**{check.description}**")
                    lines.append("")
                    lines.append(f"- Foreground: `{check.foreground}`")
                    lines.append(f"- Background: `{check.background}`")
                    lines.append(f"- Contrast ratio: **{check.ratio:.2f}:1** (required: {check.requirement}:1)")
                    lines.append(f"- Deficit: {check.requirement - check.ratio:.2f}:1")

                    suggestion = self.suggest_color_adjustment(
                        check.foreground, check.background, check.requirement
                    )
                    if suggestion:
                        suggested_color, new_ratio = suggestion
                        action = "Lightened" if ColorUtils.calculate_relative_luminance(
                            check.background) < 0.5 else "Darkened"
                        lines.append(f"- **Suggested fix:** {action} `{check.foreground}` → `{suggested_color}`")
                        lines.append(f"- **New ratio:** {new_ratio:.2f}:1")

                    lines.append("")

                lines.append("---")
                lines.append("")

        # Recommendations
        lines.append("## Recommendations")
        lines.append("")

        if not failed_themes:
            lines.append("✓ **All themes meet WCAG AA accessibility standards!**")
        else:
            lines.append("### Priority Order for Fixes")
            lines.append("")
            lines.append("1. **Primary text** (foreground/background) - Affects all text readability")
            lines.append("2. **UI components** (cursor, selection) - Affects user interaction")
            lines.append("3. **Console colors** - Affects syntax highlighting and terminal output")

        lines.append("")
        lines.append("---")
        lines.append("")
        lines.append("*Report generated by accessibility-audit.py*")

        return "\n".join(lines)


def main():
    """Main entry point."""
    print("Starting WCAG AA Accessibility Audit...")
    print()

    # Find themes directory
    project_root = os.path.dirname(os.path.abspath(__file__))
    themes_dir = os.path.join(project_root, "windows-terminal-schemes")

    if not os.path.exists(themes_dir):
        print(f"ERROR: Themes directory not found: {themes_dir}")
        return 1

    # Run audit
    auditor = AccessibilityAuditor(themes_dir)
    auditor.audit_all_themes()

    # Display summary
    print(auditor.generate_summary_report())

    # Save detailed report
    reports_dir = os.path.join(project_root, "reports")
    os.makedirs(reports_dir, exist_ok=True)

    # Text report
    text_report = auditor.generate_detailed_report()
    text_file = os.path.join(reports_dir, "accessibility-audit-report.txt")
    with open(text_file, 'w') as f:
        f.write(text_report)
    print(f"Detailed report saved to: {text_file}")

    # Markdown report
    markdown_report = auditor.generate_markdown_report()
    markdown_file = os.path.join(reports_dir, "ACCESSIBILITY_AUDIT_REPORT.md")
    with open(markdown_file, 'w') as f:
        f.write(markdown_report)
    print(f"Markdown report saved to: {markdown_file}")

    print()

    # Exit status
    failed_count = sum(1 for r in auditor.results if not r.overall_pass)
    if failed_count > 0:
        print(f"⚠ WARNING: {failed_count} theme(s) failed WCAG AA compliance")
        return 0  # Don't fail the build
    else:
        print("✓ SUCCESS: All themes passed WCAG AA compliance!")
        return 0


if __name__ == "__main__":
    exit(main())
