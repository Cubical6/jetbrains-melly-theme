# Implementatieplan: Lovelace Kleurverschillen Minimaliseren

**Versie**: 1.0
**Datum**: 2025-11-23
**Status**: Planning (Living Document - evolueert tijdens implementatie)
**Eigenaar**: Theme Generation System

---

## Plan Changelog

### v1.0 - 2025-11-23 (Initieel Plan)
**Created:**
- 9-fase implementatie plan voor Lovelace color alignment
- Comprehensive analysis van 13 UI + 8 editor verschillen
- Post-fase subagent validatie protocol
- Plan update protocol (living document approach)
- Subagent usage triggers en guidelines

**Note:** Dit plan zal evolueren tijdens implementatie. Zie "Plan Update Protocol" sectie.

## Executive Summary

Dit plan beschrijft hoe we de geïdentificeerde kleurverschillen tussen het demo Lovelace thema en het gegenereerde Lovelace thema oplossen. We kiezen voor een algoritmische benadering die automatisering behoudt en alle thema's laat profiteren van de verbeteringen.

### Belangrijkste Bevindingen
- **13 UI thema kleurverschillen** gevonden tussen demo en gegenereerd thema
- **8+ editor syntax highlighting verschillen** gevonden, waaronder KRITIEKE bugs
- **Hoofdoorzaak**: Algoritmische kleurafleidingen vs handmatig gekozen design kleuren + verkeerde template mappings
- **Kritieke issues**:
  - UI: Verkeerde accent kleur (blauw ipv paars), te heldere borders
  - Editor: **Comments onzichtbaar** (gebruikt background kleur), verkeerde syntax kleuren
- **Impact scope**: Wijzigingen verbeteren alle gegenereerde thema's

### Aanpak
✅ **Gekozen**: Algoritmes aanpassen (behoud automatisering)
✅ **Scope**: Alle verschillen oplossen (UI + Editor + Templates)
✅ **Reikwijdte**: Alle thema's laten profiteren

### Implementatie Scope
**9 Fases:**
1. **Fase 1**: Accent Kleur Detectie (UI)
2. **Fase 2**: Border Kleuren Subtle Mode (UI)
3. **Fase 3**: Derived Color Percentages (UI)
4. **Fase 4**: Info Foreground Semantiek (UI)
5. **Fase 5**: Focus Kleur Verfijning (UI)
6. **Fase 6**: Editor Color Scheme Correcties (**KRITIEK**: invisible comments fix)
7. **Fase 7**: Testing & Validatie (UI + Editor)
8. **Fase 8**: Template Consistency Fix (Normal/Rounded variants)
9. **Fase 9**: Documentation & Template Updates

---

## Kleurverschillen Analyse

### Kritieke Verschillen (CRITICAL)

#### 1. Accent Kleur Familie Mismatch
| Property | Gegenereerd | Demo | Delta |
|----------|-------------|------|-------|
| Button.default.startBackground | #61AFEF (blauw) | #7A3A82 (paars) | **Andere kleur familie** |
| Button.default.focusedBorderColor | #61AFEF | #9f6dd1 | Accent mismatch |

**Root Cause**: Template gebruikt hardcoded `$wt_brightBlue$` als accent, maar Lovelace is paars-gebaseerd.

**Impact**: Default action buttons zijn blauw ipv paars - grootste visuele inconsistentie.

### Hoge Prioriteit (HIGH)

#### 2. Border Kleuren Te Helder
| Property | Gegenereerd | Demo | Delta |
|----------|-------------|------|-------|
| *.borderColor | #656977 | #32333A | -51 in value |

**Root Cause**: `ColorUtils.createVisibleBorderColor()` gebruikt HSV value=0.4 en 3:1 contrast ratio voor WCAG compliance.

**Impact**: Borders zijn te opdringend, maken UI visueel drukker.

#### 3. Focus Kleuren
| Property | Gegenereerd | Demo | Delta |
|----------|-------------|------|-------|
| *.focusColor | #d793ff | #C19CFF | Te licht |
| *.focusedBorderColor | #b06dd8 | #A77AE6 | Andere tint paars |

**Root Cause**: Gebruikt brightPurple uit Windows Terminal scheme, niet afgestemd op accent.

### Medium Prioriteit (MEDIUM)

#### 4. Info Foreground Semantiek
| Property | Gegenereerd | Demo | Delta |
|----------|-------------|------|-------|
| *.infoForeground | #4DD0E1 (cyan) | #B0B0B6 (grijs) | **Verkeerde semantiek** |

**Root Cause**: Template neemt aan dat info kleur cyaan moet zijn (terminal conventie), maar demo gebruikt subtiele grijs.

**Impact**: Info text te opvallend, niet subtiel genoeg.

#### 5. Button Achtergronden
| Property | Gegenereerd | Demo | Delta |
|----------|-------------|------|-------|
| Button.startBackground | #1D1F28 (= background) | #2b2836 | +8% lichter |

**Root Cause**: Template gebruikt `$wt_black$` (zelfde als background) ipv gelichte variant.

**Impact**: Normale buttons vallen weg tegen achtergrond, geen visuele scheiding.

### Lage Prioriteit (LOW)

#### 6-13. Subtiele Derived Color Verschillen
| Property | Gegenereerd | Demo | Delta |
|----------|-------------|------|-------|
| *.selectionInactiveBackground | #433a54 | #3d3952 | Klein verschil |
| ComboBox.nonEditableBackground | #3d404e | #2E2D37 | Subtiel |
| ActionButton.hoverBackground | #393b42 | #2b2836 | Hover state |
| EditorTabs.underlinedTabBackground | #353246 | #3a2c47 | Tab achtergrond |
| MainWindow.Tab.selectedBackground | #1b1d26 | #1D1F28 | Zeer subtiel |
| MainWindow.Tab.hoverBackground | #393b42 | #FFFFFF12 | Andere aanpak |

**Root Cause**: Vaste percentages in `darken()` / `lighten()` / `blend()` functies matchen niet met designer keuzes.

---

## Betrokken Code Componenten

### 1. ColorUtils.kt
**Bestand**: `buildSrc/src/main/kotlin/utils/ColorUtils.kt`

#### Functie: createVisibleBorderColor (lines 558-584)
**Huidig gedrag**:
```kotlin
fun createVisibleBorderColor(backgroundColor: Color, minContrast: Double = 3.0): Color {
    val hsv = backgroundColor.toHSV()
    val isDark = hsv.value < 0.5

    return if (isDark) {
        Color.fromHSV(hsv.hue, hsv.saturation * 0.5, 0.4) // <- PROBLEEM: value=0.4 te hoog
    } else {
        // ... lichte variant
    }.ensureContrast(backgroundColor, minContrast) // <- PROBLEEM: 3.0 te strikt
}
```

**Wijziging nodig**:
- Verlaag value van `0.4` → `0.22` voor subtielere borders
- Verlaag minContrast van `3.0` → `1.8` voor minder opdringerige borders
- Voeg optionele "subtle mode" parameter toe

### 2. WindowsTerminalColorScheme.kt
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

#### Functie: toColorPalette (lines 156-340)
**Huidige afgeleide kleuren**:
```kotlin
fun toColorPalette(): ColorPalette {
    return ColorPalette(
        // ... basis kleuren ...

        // PROBLEEM: Vaste percentages matchen niet met demo
        selectionInactiveBackground = ColorUtils.darken(selectionBackground, 0.40), // Demo: 0.48
        buttonBackground = black,  // Demo: lighten(background, 0.08)

        // PROBLEEM: Geen accent kleur detectie
        accentColor = brightBlue,  // Demo: brightPurple voor Lovelace

        // PROBLEEM: Info foreground hardcoded naar cyan
        infoForeground = cyan,  // Demo: muted gray
    )
}
```

**Wijzigingen nodig**:
1. Accent kleur detectie logica toevoegen
2. Derived color percentages tunen
3. Info foreground berekening toevoegen
4. Focus kleuren koppelen aan accent

### 3. windows-terminal.template.theme.json
**Bestand**: `buildSrc/templates/windows-terminal.template.theme.json`

**Huidige problematische mappings**:
```json
{
  "colors": {
    "*.borderColor": "$wt_border_color$",
    "*.focusColor": "$wt_brightPurple$",  // Niet gekoppeld aan accent
    "*.infoForeground": "$wt_cyan$",      // Hardcoded naar cyan
    "Button.startBackground": "$wt_black$",  // Zou surface moeten zijn
    "Button.default.startBackground": "$wt_brightBlue$",  // Zou accent moeten zijn
  }
}
```

**Wijzigingen nodig**:
- Introduceer `$wt_accent_color$` placeholder
- Introduceer `$wt_info_foreground$` placeholder
- Introduceer `$wt_button_background$` placeholder
- Koppel focus kleuren aan accent

### 4. TemplateProcessor.kt
**Bestand**: `buildSrc/src/main/kotlin/themes/TemplateProcessor.kt`

**Mogelijk nodig**: Nieuwe placeholder registratie voor nieuwe variabelen.

---

## Gedetailleerde Implementatie Fases

## **FASE 1: Accent Kleur Detectie Systeem**

### Doel
Automatisch detecteren welke kleur de primaire accent moet zijn (blue, purple, cyan, etc.) op basis van Windows Terminal color scheme, zodat het juiste accent gebruikt wordt voor buttons, focus, en andere UI elementen.

### Achtergrond
Lovelace is een paars-gebaseerd thema, maar de generator gebruikt altijd `brightBlue` als accent. Dit veroorzaakt blauwe buttons waar paarse verwacht worden.

### Implementatie Stappen

#### Stap 1.1: Accent Detectie Algoritme Ontwikkelen
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**Nieuwe functie toevoegen**:
```kotlin
/**
 * Detecteert de primaire accent kleur op basis van color scheme analyse.
 *
 * Analyseert brightness, saturation, en gebruik van bright colors om te bepalen
 * welke kleur het meest dominant/geschikt is als accent.
 */
private fun detectAccentColor(): Color {
    val candidates = listOf(
        "brightBlue" to brightBlue,
        "brightPurple" to brightPurple,
        "brightCyan" to brightCyan,
        "brightGreen" to brightGreen,
        "brightRed" to brightRed
    )

    // Bereken score voor elke kandidaat op basis van:
    // 1. Saturatie (hoe levendig)
    // 2. Helderheid (brightness)
    // 3. Afstand tot andere kleuren (uniciteit)
    // 4. Geschiktheid voor UI (niet te fel)

    val scored = candidates.map { (name, color) ->
        val hsv = color.toHSV()
        val score = (hsv.saturation * 0.4) + (hsv.value * 0.3) + (uniquenessScore(color, candidates) * 0.3)
        name to score
    }

    val winner = scored.maxByOrNull { it.second }?.first ?: "brightBlue"

    return when(winner) {
        "brightBlue" -> brightBlue
        "brightPurple" -> brightPurple
        "brightCyan" -> brightCyan
        "brightGreen" -> brightGreen
        "brightRed" -> brightRed
        else -> brightBlue
    }
}

private fun uniquenessScore(color: Color, allColors: List<Pair<String, Color>>): Double {
    // Bereken gemiddelde color distance tot andere kleuren
    // Hoe hoger, hoe unieker de kleur
    return allColors
        .filter { it.second != color }
        .map { ColorUtils.colorDistance(color, it.second) }
        .average()
}
```

**Locatie**: Toevoegen na lijn 155, voor `toColorPalette()` functie.

#### Stap 1.2: Accent Kleur Integreren in ColorPalette
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**In `toColorPalette()` functie (lijn ~200)**:
```kotlin
fun toColorPalette(): ColorPalette {
    // Bestaande code...

    // NIEUW: Detecteer accent kleur ipv hardcoded brightBlue
    val detectedAccent = detectAccentColor()

    return ColorPalette(
        // ... bestaande mappings ...

        // GEWIJZIGD: gebruik detected accent
        accentColor = detectedAccent,

        // ... rest van mappings ...
    )
}
```

#### Stap 1.3: Template Updaten met Accent Placeholder
**Bestand**: `buildSrc/templates/windows-terminal.template.theme.json`

**Wijzig** (ongeveer lijn 8-12):
```json
{
  "colors": {
    // VOOR:
    "Button.default.startBackground": "$wt_brightBlue$",
    "Button.default.focusedBorderColor": "$wt_brightBlue$",

    // NA:
    "Button.default.startBackground": "$wt_accent_color$",
    "Button.default.focusedBorderColor": "$wt_accent_color$",
  }
}
```

#### Stap 1.4: ColorPaletteExpander Updaten
**Bestand**: `buildSrc/src/main/kotlin/mapping/ColorPaletteExpander.kt`

**Zoek de palette expansion code en voeg toe**:
```kotlin
fun expandPalette(palette: ColorPalette): Map<String, String> {
    return mapOf(
        // ... bestaande mappings ...

        // NIEUW: Accent color mapping
        "wt_accent_color" to palette.accentColor.toHex(),

        // ... rest van mappings ...
    )
}
```

### Verwachte Resultaat Fase 1
- ✅ Lovelace gebruikt automatisch paars accent (#A77AE6 richting)
- ✅ Default buttons zijn paars ipv blauw
- ✅ Focus borders gebruiken paarse tint
- ✅ Andere thema's detecteren ook automatisch hun optimale accent

### Test Strategie Fase 1
1. Genereer Lovelace → verwacht paars accent
2. Genereer One Half Dark → verwacht blauw accent (ter verificatie)
3. Visuele inspectie: buttons moeten juiste kleur hebben

---

## **FASE 2: Border Kleur Algoritme Verbeteren**

### Doel
Maak borders subtieler en minder opdringend, terwijl nog steeds voldoende contrast behouden blijft voor usability. Match de subtiele #32333A van demo ipv heldere #656977.

### Achtergrond
`createVisibleBorderColor()` is ontworpen met WCAG 3:1 contrast in gedachten, maar dit maakt borders te prominent. Demo thema's gebruiken subtielere borders die de UI rustiger maken.

### Implementatie Stappen

#### Stap 2.1: Voeg "Subtle Border Mode" Parameter Toe
**Bestand**: `buildSrc/src/main/kotlin/utils/ColorUtils.kt`

**Wijzig functie signature (lijn ~558)**:
```kotlin
// VOOR:
fun createVisibleBorderColor(backgroundColor: Color, minContrast: Double = 3.0): Color

// NA:
fun createVisibleBorderColor(
    backgroundColor: Color,
    minContrast: Double = 3.0,
    subtle: Boolean = false  // NIEUW: optie voor subtiele borders
): Color
```

#### Stap 2.2: Implementeer Subtiele Border Berekening
**Bestand**: `buildSrc/src/main/kotlin/utils/ColorUtils.kt`

**Vervang body van functie (lijn ~560-584)**:
```kotlin
fun createVisibleBorderColor(
    backgroundColor: Color,
    minContrast: Double = 3.0,
    subtle: Boolean = false
): Color {
    val hsv = backgroundColor.toHSV()
    val isDark = hsv.value < 0.5

    // Kies parameters op basis van mode
    val (targetValue, saturationMultiplier, contrast) = if (subtle) {
        // NIEUWE WAARDEN: voor subtiele, minder opdringerige borders
        if (isDark) Triple(0.22, 0.30, 1.8) else Triple(0.75, 0.30, 1.8)
    } else {
        // BESTAANDE WAARDEN: voor WCAG-compliant borders
        if (isDark) Triple(0.40, 0.50, 3.0) else Triple(0.60, 0.50, 3.0)
    }

    val borderColor = if (isDark) {
        Color.fromHSV(
            hsv.hue,
            hsv.saturation * saturationMultiplier,  // Minder saturatie voor neutralere kleur
            targetValue  // Lagere value voor subtielere kleur
        )
    } else {
        Color.fromHSV(
            hsv.hue,
            hsv.saturation * saturationMultiplier,
            targetValue
        )
    }

    // Pas contrast toe, maar met aangepaste minimum
    return borderColor.ensureContrast(backgroundColor, if (subtle) contrast else minContrast)
}
```

**Toelichting wijzigingen**:
- `targetValue`: 0.40 → 0.22 (veel donkerder voor dark themes)
- `saturationMultiplier`: 0.50 → 0.30 (neutraler grijs ipv gekleurd)
- `minContrast`: 3.0 → 1.8 (minder strenge contrast eis)

#### Stap 2.3: Update WindowsTerminalColorScheme om Subtle Mode Te Gebruiken
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**In `toColorPalette()` functie**:
```kotlin
fun toColorPalette(): ColorPalette {
    // ... bestaande code ...

    // GEWIJZIGD: gebruik subtle=true voor border color
    val borderColor = ColorUtils.createVisibleBorderColor(
        backgroundColor = background,
        subtle = true  // NIEUW: activeer subtle mode
    )

    return ColorPalette(
        // ... bestaande mappings ...
        borderColor = borderColor,
        // ...
    )
}
```

**Locatie**: Zoek waar `borderColor` wordt berekend (waarschijnlijk rond lijn 220-230).

#### Stap 2.4: Test met Meerdere Achtergrond Kleuren
**Test cases om te verifiëren**:

| Background | Verwachte Border (subtle) | Oude Border | Test |
|------------|---------------------------|-------------|------|
| #1D1F28 (Lovelace dark) | ~#32333A | #656977 | ✓ Donkerder |
| #FFFFFF (wit) | ~#CCCCCC | ~#999999 | ✓ Subtiel |
| #000000 (zwart) | ~#2A2A2A | ~#666666 | ✓ Bijna zwart |

### Verwachte Resultaat Fase 2
- ✅ Lovelace borders: #656977 → ~#32333A (match demo)
- ✅ Borders zijn subtiel maar nog steeds zichtbaar
- ✅ UI oogt rustiger en cleaner
- ✅ Geen usability issues (borders nog te onderscheiden)

### Risico's Fase 2
⚠️ **Accessibility**: Te lage contrast kan problemen geven voor gebruikers met slechtziend
- **Mitigatie**: Behoud oude functie beschikbaar met `subtle=false`
- **Mitigatie**: Test visueel met verschillende beeldscherminstellingen

⚠️ **Andere thema's**: Lichte thema's kunnen te donkere borders krijgen
- **Mitigatie**: Test met minimaal 3 lichte thema's
- **Mitigatie**: Mogelijk verschillende waarden voor light vs dark

### Test Strategie Fase 2
1. **Unit test**: Bereken border color voor bekende backgrounds
2. **Visual test**: Genereer Lovelace en vergelijk met demo
3. **Regression test**: Genereer 5 andere thema's (2 light, 3 dark)
4. **Accessibility test**: Verifieer borders zijn nog zichtbaar bij lage helderheid

---

## **FASE 3: Derived Color Percentages Tunen**

### Doel
Pas de vaste percentages aan in `darken()`, `lighten()`, en `blend()` operaties zodat derived colors nauwkeuriger matchen met demo thema's designer keuzes.

### Achtergrond
De huidige percentages (bijv. darken 0.40, lighten 0.05) zijn arbitrair gekozen. Door deze te vergelijken met demo thema's kunnen we betere defaults vinden.

### Implementatie Stappen

#### Stap 3.1: Button Background - Surface Color
**Probleem**: `Button.startBackground` gebruikt background color (te donker), demo gebruikt gelichte variant.

**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**In `toColorPalette()` functie**:
```kotlin
// VOOR:
buttonBackground = black,  // Zelfde als background, geen contrast

// NA:
buttonBackground = ColorUtils.lighten(background, 0.08),  // Licht gelicht voor subtle contrast
```

**Toelichting**:
- Demo: #1D1F28 → #2b2836 is ~8% lichter in HSV value
- Dit geeft buttons een subtle "verhoogd" effect zonder opdringerig te zijn

#### Stap 3.2: Selection Inactive Background - Donkerder Maken
**Probleem**: `selectionInactiveBackground` is niet donker genoeg (#433a54 vs #3d3952)

**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

```kotlin
// VOOR:
selectionInactiveBackground = ColorUtils.darken(selectionBackground, 0.40),

// NA:
selectionInactiveBackground = ColorUtils.darken(selectionBackground, 0.48),
```

**Berekening**:
- Demo: #70618D → #3d3952
- HSV value delta: ~48% donkerder
- Nieuwe percentage: 0.40 → 0.48

#### Stap 3.3: ComboBox Non-Editable Background
**Probleem**: `ComboBox.nonEditableBackground` is te licht (#3d404e vs #2E2D37)

**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**Huidige berekening vinden**:
```kotlin
// Huidige (geschatte locatie):
comboBoxNonEditableBackground = ColorUtils.lighten(background, 0.10),

// NA:
comboBoxNonEditableBackground = ColorUtils.lighten(background, 0.05),
```

**Toelichting**: Demo gebruikt subtielere achtergrond voor disabled/non-editable velden.

#### Stap 3.4: Editor Tabs - Paarse Tint Toevoegen
**Probleem**: `EditorTabs.underlinedTabBackground` heeft verkeerde kleur tint (#353246 vs #3a2c47)

**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

```kotlin
// VOOR (geschat):
editorTabsUnderlinedBackground = ColorUtils.lighten(background, 0.08),

// NA: Voeg purple tint toe voor paarse thema's
editorTabsUnderlinedBackground = if (isPurpleThemed()) {
    ColorUtils.blend(
        ColorUtils.lighten(background, 0.08),
        detectedAccent,  // Van Fase 1
        0.15  // 15% accent kleur
    )
} else {
    ColorUtils.lighten(background, 0.08)
},

// Helper functie toevoegen:
private fun isPurpleThemed(): Boolean {
    val accentHue = detectAccentColor().toHSV().hue
    return accentHue in 270.0..320.0  // Purple hue range
}
```

**Toelichting**: Demo heeft paarse ondertoon in tab backgrounds voor cohesie met accent kleur.

#### Stap 3.5: ActionButton Hover - Consistent met Button Background
**Probleem**: `ActionButton.hoverBackground` is inconsistent (#393b42 vs #2b2836)

**Bestand**: Template of ColorScheme

```kotlin
// NA: Gebruik zelfde berekening als button background
actionButtonHoverBackground = buttonBackground,  // Hergebruik button surface color
```

**Toelichting**: Demo gebruikt dezelfde surface color voor hover states als voor buttons.

#### Stap 3.6: MainWindow Tab Hover - Alpha Overlay
**Probleem**: Demo gebruikt transparante overlay (#FFFFFF12), generator gebruikt opaque kleur

**Bestand**: `buildSrc/templates/windows-terminal.template.theme.json`

```json
// VOOR:
"MainWindow.Tab.hoverBackground": "$wt_surface$",

// NA:
"MainWindow.Tab.hoverBackground": "#FFFFFF12",  // 12 is ~7% alpha in hex
```

**Toelichting**: Transparante overlays geven consistenter resultaat over verschillende backgrounds.

### Samenvattingstabel Wijzigingen

| Property | Oude Formule | Nieuwe Formule | Reden |
|----------|--------------|----------------|-------|
| buttonBackground | `black` | `lighten(bg, 0.08)` | Subtle contrast |
| selectionInactiveBackground | `darken(sel, 0.40)` | `darken(sel, 0.48)` | Match demo darkness |
| comboBoxNonEditableBackground | `lighten(bg, 0.10)` | `lighten(bg, 0.05)` | Subteler |
| editorTabsUnderlinedBackground | `lighten(bg, 0.08)` | `blend(lighten(bg, 0.08), accent, 0.15)` | Paarse tint |
| actionButtonHoverBackground | Eigen berekening | `buttonBackground` | Consistentie |
| mainWindowTabHoverBackground | Opaque kleur | `#FFFFFF12` alpha | Betere overlay |

### Verwachte Resultaat Fase 3
- ✅ Buttons hebben subtle contrast met achtergrond
- ✅ Selection states matchen demo darkness
- ✅ Editor tabs hebben cohesieve paarse tint
- ✅ Hover states zijn consistent

### Test Strategie Fase 3
1. **Voor elke wijziging**: bereken nieuwe kleur en vergelijk met demo
2. **Visual test**: Screenshot van buttons, tabs, selections
3. **Regression test**: Test met niet-paarse thema's (geen purple tint)

---

## **FASE 4: Info Foreground Semantiek**

### Doel
Wijzig `infoForeground` van opvallend cyaan naar subtiele muted gray, passend bij de "hint text" semantiek van het demo thema.

### Achtergrond
Windows Terminal conventie gebruikt cyaan voor info text (terminal tradition), maar moderne IDE's gebruiken muted foreground voor hints/info. Demo thema volgt IDE conventie.

### Implementatie Stappen

#### Stap 4.1: Info Foreground Berekening Toevoegen
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**In `toColorPalette()` functie**:
```kotlin
fun toColorPalette(): ColorPalette {
    // ... bestaande code ...

    // NIEUW: Bereken muted gray voor info text
    val infoForeground = ColorUtils.blend(
        foreground,      // #D0D0D9 (normale tekst kleur)
        background,      // #1D1F28 (achtergrond)
        0.35            // 35% richting achtergrond = muted
    )
    // Verwacht resultaat: ~#B0B0B6 (matches demo)

    return ColorPalette(
        // ... bestaande mappings ...

        // GEWIJZIGD: gebruik berekende muted gray ipv cyan
        infoForeground = infoForeground,
        // VOOR was: infoForeground = cyan,

        // ...
    )
}
```

**Toelichting berekening**:
- `foreground`: #D0D0D9 (normale tekst)
- `background`: #1D1F28 (achtergrond)
- `blend(..., 0.35)`: 65% foreground + 35% background
- Resultaat: ~#B0B0B6 (muted gray, matches demo)

#### Stap 4.2: Template Updaten met Info Placeholder
**Bestand**: `buildSrc/templates/windows-terminal.template.theme.json`

**Wijzig alle infoForeground referenties**:
```json
{
  "colors": {
    // VOOR:
    "*.infoForeground": "$wt_cyan$",
    "Label.infoForeground": "$wt_cyan$",
    "Component.infoForeground": "$wt_cyan$",

    // NA:
    "*.infoForeground": "$wt_info_foreground$",
    "Label.infoForeground": "$wt_info_foreground$",
    "Component.infoForeground": "$wt_info_foreground$",
  }
}
```

#### Stap 4.3: ColorPaletteExpander Updaten
**Bestand**: `buildSrc/src/main/kotlin/mapping/ColorPaletteExpander.kt`

**Voeg mapping toe**:
```kotlin
fun expandPalette(palette: ColorPalette): Map<String, String> {
    return mapOf(
        // ... bestaande mappings ...

        // NIEUW: Info foreground placeholder
        "wt_info_foreground" to palette.infoForeground.toHex(),

        // ...
    )
}
```

#### Stap 4.4: Fallback voor Terminal-Style Thema's (Optioneel)
Als we willen dat sommige thema's nog steeds cyaan kunnen gebruiken:

**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

```kotlin
// Optionele logica: detecteer of thema terminal-style is
private fun shouldUseTerminalSemantics(): Boolean {
    // Als thema sterke terminal kleuren heeft (hoge saturatie ANSI colors)
    val ansiColors = listOf(red, green, yellow, blue, magenta, cyan)
    val avgSaturation = ansiColors.map { it.toHSV().saturation }.average()
    return avgSaturation > 0.6  // High saturation = terminal theme
}

fun toColorPalette(): ColorPalette {
    // ...

    val infoForeground = if (shouldUseTerminalSemantics()) {
        cyan  // Terminal-style: gebruik cyaan
    } else {
        ColorUtils.blend(foreground, background, 0.35)  // IDE-style: muted gray
    }

    // ...
}
```

**Toelichting**: Dit behoudt backwards compatibility voor terminal-achtige thema's.

### Verwachte Resultaat Fase 4
- ✅ Lovelace info text: #4DD0E1 (cyaan) → ~#B0B0B6 (muted gray)
- ✅ Info text is subtiel en niet afleidend
- ✅ Matches demo semantiek
- ✅ (Optioneel) Terminal thema's kunnen nog steeds cyaan gebruiken

### Test Strategie Fase 4
1. **Visual test**: Inspecteer tooltips, hints, disabled labels
2. **Contrast check**: Verifieer muted gray nog steeds leesbaar is
3. **Regression test**: Test met terminal-style thema (One Half Dark) als fallback geïmplementeerd

---

## **FASE 5: Focus Kleur Verfijning**

### Doel
Koppel focus kleuren (`focusColor`, `focusedBorderColor`) aan de gedetecteerde accent kleur zodat focus indicators consistent zijn met het kleurenschema en voldoende opvallen.

### Achtergrond
Huidige implementatie gebruikt hardcoded `brightPurple` voor focus, wat niet matcht met accent kleur detectie uit Fase 1. Demo thema gebruikt specifieke paarse tinten afgestemd op het accent.

### Implementatie Stappen

#### Stap 5.1: Focus Color Afgeleid van Accent
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**In `toColorPalette()` functie**:
```kotlin
fun toColorPalette(): ColorPalette {
    // ... bestaande code ...

    // Van Fase 1: detected accent color
    val detectedAccent = detectAccentColor()

    // NIEUW: Bereken focus colors van accent
    val focusColor = ColorUtils.lighten(detectedAccent, 0.15)
    // Voor Lovelace: #A77AE6 → #C19CFF (15% lichter)

    val focusedBorderColor = detectedAccent
    // Gebruik accent direct voor border (sterkere indicator)

    return ColorPalette(
        // ... bestaande mappings ...

        accentColor = detectedAccent,

        // GEWIJZIGD: afgeleid van accent ipv hardcoded brightPurple
        focusColor = focusColor,
        focusedBorderColor = focusedBorderColor,
        // VOOR was:
        // focusColor = brightPurple,
        // focusedBorderColor = ColorUtils.darken(brightPurple, 0.10),

        // ...
    )
}
```

**Toelichting**:
- `focusColor`: Gelichte variant van accent voor glow/outline
- `focusedBorderColor`: Accent zelf voor sterke border
- Demo Lovelace: focusColor=#C19CFF, focusedBorderColor=#A77AE6

#### Stap 5.2: Contrast Check voor Focus Kleuren
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**Voeg accessibility check toe**:
```kotlin
fun toColorPalette(): ColorPalette {
    // ...

    val detectedAccent = detectAccentColor()

    // Bereken focus color met contrast check
    var focusColor = ColorUtils.lighten(detectedAccent, 0.15)

    // NIEUW: Zorg dat focus zichtbaar is tegen achtergrond
    val focusContrast = ColorUtils.contrastRatio(focusColor, background)
    if (focusContrast < 2.5) {
        // Te weinig contrast, maak lichter/donkerder
        focusColor = ColorUtils.ensureContrast(focusColor, background, 2.5)
    }

    // ...
}
```

**Toelichting**: Focus moet altijd zichtbaar zijn, minimum 2.5:1 contrast voor usability.

#### Stap 5.3: Template Updaten voor Focus Kleuren
**Bestand**: `buildSrc/templates/windows-terminal.template.theme.json`

**Wijzig focus color mappings**:
```json
{
  "colors": {
    // VOOR:
    "*.focusColor": "$wt_brightPurple$",
    "*.focusedBorderColor": "$wt_brightPurple$",
    "Component.focusColor": "$wt_brightPurple$",

    // NA:
    "*.focusColor": "$wt_focus_color$",
    "*.focusedBorderColor": "$wt_focused_border_color$",
    "Component.focusColor": "$wt_focus_color$",
  }
}
```

#### Stap 5.4: ColorPaletteExpander Updaten
**Bestand**: `buildSrc/src/main/kotlin/mapping/ColorPaletteExpander.kt`

**Voeg focus color mappings toe**:
```kotlin
fun expandPalette(palette: ColorPalette): Map<String, String> {
    return mapOf(
        // ... bestaande mappings ...

        // NIEUW: Focus color placeholders
        "wt_focus_color" to palette.focusColor.toHex(),
        "wt_focused_border_color" to palette.focusedBorderColor.toHex(),

        // ...
    )
}
```

### Verwachte Resultaat Fase 5
- ✅ Lovelace focus color: #d793ff → #C19CFF (matches demo)
- ✅ Lovelace focused border: #b06dd8 → #A77AE6 (matches demo)
- ✅ Focus kleuren zijn afgestemd op accent kleur
- ✅ Minimum contrast gegarandeerd voor accessibility

### Risico's Fase 5
⚠️ **Lage contrast accents**: Sommige thema's hebben donkere accents die slecht zichtbaar zijn als focus
- **Mitigatie**: `ensureContrast()` check voor minimum 2.5:1
- **Mitigatie**: Test met dark accent thema's

### Test Strategie Fase 5
1. **Keyboard navigation test**: Tab door UI, focus moet duidelijk zichtbaar zijn
2. **Contrast test**: Meet contrast ratio van focus tegen achtergrond
3. **Visual test**: Vergelijk focus appearance met demo thema

---

## **FASE 6: Editor Color Scheme Correcties**

### Doel
Corrigeer de kritieke en hoge prioriteit syntax highlighting verschillen in de editor color scheme template, met speciale aandacht voor onzichtbare comments en verkeerde syntax kleuren.

### Achtergrond
De editor color scheme template (`windows-terminal.template.xml`) bevat verkeerde placeholder mappings die resulteren in:
- **KRITIEK**: Comments zijn onzichtbaar (zwart op zwart)
- **HOOG**: Verkeerde kleuren voor numbers, classes, en functions
- **MEDIUM**: Enkele hardcoded kleuren ipv scheme-aware

### Gevonden Verschillen

#### KRITIEKE Issues

**1. Onzichtbare Comments**
| Element | Demo | Gegenereerd | Oorzaak |
|---------|------|-------------|---------|
| DEFAULT_LINE_COMMENT | `808080` (grijs) | `1d1f28` (zwart = background) | Template gebruikt `$wt_black$` |
| DEFAULT_BLOCK_COMMENT | `808080` (grijs) | `1d1f28` (zwart = background) | Template gebruikt `$wt_black$` |
| DEFAULT_DOC_COMMENT | `808080` (grijs) | `1d1f28` (zwart = background) | Template gebruikt `$wt_black$` |

**Impact**: Comments zijn volledig onzichtbaar, thema onbruikbaar voor development.

**Root Cause**: Template lijnen 595, 619, 688 gebruiken `$wt_black$` wat mapped naar background kleur `#1D1F28`.

#### HOGE Prioriteit Issues

**2. Verkeerde Number Kleur**
| Element | Demo | Gegenereerd | Oorzaak |
|---------|------|-------------|---------|
| DEFAULT_NUMBER | `5AA6FF` (blauw) | `ffc26f` (oranje) | Template gebruikt `$wt_bright_yellow$` ipv blue |

**Impact**: Numerieke literals vallen op als warnings/strings ipv literals.

**3. Verkeerde Class Reference Kleur**
| Element | Demo | Gegenereerd | Oorzaak |
|---------|------|-------------|---------|
| DEFAULT_CLASS_REFERENCE | `D081FF` (paars) | `ffd479` (geel) | Template gebruikt `$wt_yellow$` ipv purple |

**Impact**: Class names zien eruit als warnings/strings.

**4. Function Declaration Kleur Te Donker**
| Element | Demo | Gegenereerd | Oorzaak |
|---------|------|-------------|---------|
| DEFAULT_FUNCTION_DECLARATION | `61AFEF` (bright blue) | `5aa6ff` (blue) | Template gebruikt `$wt_blue$` ipv `$wt_bright_blue$` |

**Impact**: Functions minder opvallend dan bedoeld.

#### MEDIUM Prioriteit Issues

**5. Hardcoded Caret Color**
| Element | Demo | Gegenereerd | Oorzaak |
|---------|------|-------------|---------|
| CARET_COLOR | `FFFFFF` (wit) | `528bff` (blauw) | Hardcoded in template |

**Impact**: Cursor kleur niet afgestemd op thema.

**6. Andere Editor UI Verschillen**
| Element | Demo | Gegenereerd | Delta |
|---------|------|-------------|-------|
| SELECTION_BACKGROUND | `3A3C47` (subtiel grijs) | `70618d` (paars) | Demo gebruikt custom subtiele kleur |
| LINE_NUMBERS_COLOR | `7F808C` | `52545d` | Verschillende grijs tinten |
| ERROR_HINT | `FF6B6B` (fel rood) | `781732` (donker rood) | Hardcoded vs scheme |
| MODIFIED_LINES_COLOR | `87CEFA` (blauw) | `ffc26f` (oranje) | Demo custom vs scheme |

### Implementatie Stappen

#### Stap 6.1: Fix Onzichtbare Comments (KRITIEK)
**Bestand**: `buildSrc/templates/windows-terminal.template.xml`

**Lijn 595 - DEFAULT_LINE_COMMENT**:
```xml
<!-- VOOR: -->
<option name="FOREGROUND" value="$wt_black$"/>

<!-- NA: -->
<option name="FOREGROUND" value="$wt_bright_black$"/>
```

**Lijn 619 - DEFAULT_BLOCK_COMMENT**:
```xml
<!-- VOOR: -->
<option name="FOREGROUND" value="$wt_black$"/>

<!-- NA: -->
<option name="FOREGROUND" value="$wt_bright_black$"/>
```

**Lijn 688 - DEFAULT_DOC_COMMENT**:
```xml
<!-- VOOR: -->
<option name="FOREGROUND" value="$wt_black$"/>

<!-- NA: -->
<option name="FOREGROUND" value="$wt_bright_black$"/>
```

**Verwacht resultaat**:
- Lovelace: `#1D1F28` (onzichtbaar) → `#646078` (zichtbaar grijs)
- Comments zijn duidelijk leesbaar
- Alle thema's: brightBlack is altijd zichtbaar tegen background

#### Stap 6.2: Fix Number Kleur
**Bestand**: `buildSrc/templates/windows-terminal.template.xml`

**Lijn 698-701 - DEFAULT_NUMBER**:
```xml
<!-- VOOR: -->
<option name="DEFAULT_NUMBER">
  <value>
    <option name="FOREGROUND" value="$wt_bright_yellow$"/>
  </value>
</option>

<!-- NA: -->
<option name="DEFAULT_NUMBER">
  <value>
    <option name="FOREGROUND" value="$wt_blue$"/>
  </value>
</option>
```

**Verwacht resultaat**: Numbers zijn blauw (zoals typisch in syntax highlighting) ipv oranje/geel.

#### Stap 6.3: Fix Class Reference Kleur
**Bestand**: `buildSrc/templates/windows-terminal.template.xml`

**Lijn 604-607 - DEFAULT_CLASS_REFERENCE**:
```xml
<!-- VOOR: -->
<option name="DEFAULT_CLASS_REFERENCE">
  <value>
    <option name="FOREGROUND" value="$wt_yellow$"/>
  </value>
</option>

<!-- NA: -->
<option name="DEFAULT_CLASS_REFERENCE">
  <value>
    <option name="FOREGROUND" value="$wt_bright_magenta$"/>
  </value>
</option>
```

**Verwacht resultaat**: Class names zijn paars/magenta (semantisch correct voor types) ipv geel.

#### Stap 6.4: Fix Function Declaration Kleur
**Bestand**: `buildSrc/templates/windows-terminal.template.xml`

**Lijn 643-646 - DEFAULT_FUNCTION_DECLARATION**:
```xml
<!-- VOOR: -->
<option name="DEFAULT_FUNCTION_DECLARATION">
  <value>
    <option name="FOREGROUND" value="$wt_blue$"/>
  </value>
</option>

<!-- NA: -->
<option name="DEFAULT_FUNCTION_DECLARATION">
  <value>
    <option name="FOREGROUND" value="$wt_bright_blue$"/>
  </value>
</option>
```

**Verwacht resultaat**: Function declarations zijn bright blue (opvallender, zoals demo).

#### Stap 6.5: Fix Caret Color (Optioneel)
**Bestand**: `buildSrc/templates/windows-terminal.template.xml`

**Lijn 14 - CARET_COLOR**:
```xml
<!-- VOOR: -->
<option name="CARET_COLOR" value="528bff"/>

<!-- NA - Optie A: Gebruik cursor color uit scheme -->
<option name="CARET_COLOR" value="$wt_cursorColor$"/>

<!-- NA - Optie B: Gebruik foreground voor consistency -->
<option name="CARET_COLOR" value="$wt_foreground$"/>
```

**Toelichting**:
- Optie A: Als Windows Terminal scheme `cursorColor` definieert, gebruik die
- Optie B: Anders gebruik foreground (meestal wit/licht voor dark themes)
- Demo gebruikt wit (`FFFFFF`), wat typisch de foreground is

**Voorkeur**: Optie B (foreground) voor consistentie.

#### Stap 6.6: Overweeg Selection Background Aanpassing (Optioneel)
**Bestand**: `buildSrc/templates/windows-terminal.template.xml`

**Huidige**: Gebruikt `$wt_selectionBackground$` (correct volgens scheme)
**Demo**: Gebruikt custom subtle gray `3A3C47`

**Beslissing**:
- ✅ **Behoud huidige**: Scheme's `selectionBackground` is de bedoelde kleur
- ❌ **Wijzig naar grijs**: Zou scheme's intentie negeren

**Rationale**: De scheme definition (`lovelace.json`) expliciet definieert `selectionBackground: #70618D`. Dit is een design decision van het originele Lovelace thema. We moeten die respecteren.

**Alternatief**: Als we subtielere selection willen, pas de **scheme** aan, niet de template.

#### Stap 6.7: Update ColorPaletteExpander voor Cursor Color
**Bestand**: `buildSrc/src/main/kotlin/mapping/ColorPaletteExpander.kt`

**Als we Stap 6.5 Optie A kiezen**, voeg cursor color mapping toe:
```kotlin
fun expandPalette(palette: ColorPalette): Map<String, String> {
    return mapOf(
        // ... bestaande mappings ...

        // NIEUW: Cursor color (for caret)
        "wt_cursorColor" to (palette.cursorColor?.toHex() ?: palette.foreground.toHex()),

        // ...
    )
}
```

**Check**: Verifieer of `ColorPalette` data class al `cursorColor` property heeft.

### Verwachte Resultaten Fase 6
- ✅ **KRITIEK opgelost**: Comments zijn zichtbaar in alle thema's
- ✅ **Syntax kleuren correct**: Numbers blauw, classes paars, functions bright blue
- ✅ **Cursor kleur consistent**: Wit/foreground ipv hardcoded blauw
- ✅ **Geen regressies**: Alle thema's syntax highlighting werkt correct

### Risico's Fase 6
⚠️ **Breaking change**: Bestaande gebruikers zien andere syntax kleuren
- **Mitigatie**: Documenteer in changelog, toon voor/na screenshots

⚠️ **Scheme compatibility**: Niet alle schemes hebben brightBlack gedefinieerd
- **Mitigatie**: ColorPalette generator moet altijd brightBlack creëren (fallback naar lighter gray)

### Test Strategie Fase 6
1. **Visual test**: Open code file in gegenereerde thema
   - ☐ Comments zijn zichtbaar (grijs)
   - ☐ Numbers zijn blauw
   - ☐ Classes zijn paars
   - ☐ Functions zijn bright blue
   - ☐ Cursor is wit/zichtbaar

2. **Multi-language test**: Test syntax highlighting in:
   - Java (classes, methods, primitives)
   - Kotlin (functions, properties, classes)
   - Python (functions, numbers, strings)
   - JavaScript (functions, classes, numbers)

3. **Regression test**: Genereer 5 thema's, verifieer comments altijd zichtbaar

4. **Contrast check**: Verifieer comment color heeft minimum 2.5:1 contrast

### Acceptatie Criteria Fase 6
- ✅ Comments hebben zichtbare kleur in alle thema's (niet zwart)
- ✅ Syntax kleuren semantisch correct (numbers=blue, classes=purple, etc.)
- ✅ Cursor kleur zichtbaar en consistent
- ✅ Geen accessibility regressies
- ✅ Visual approval van syntax highlighting screenshots

---

## **FASE 7: Testing & Validatie**

### Doel
Systematisch verifiëren dat alle wijzigingen correct werken, geen regressies veroorzaken, en alle thema's (niet alleen Lovelace) er beter uitzien.

### Test Matrix

#### 6.1 Lovelace Color Delta Validatie
**Doel**: Meten hoe dicht we bij demo thema komen.

**Test Script**:
```kotlin
// Test bestand: buildSrc/src/test/kotlin/ColorDeltaTest.kt

@Test
fun `Lovelace generated matches demo within acceptable delta`() {
    val demo = loadDemoTheme("demo/Lovelace-Theme/resources/theme/LoveLaceTheme.theme.json")
    val generated = generateTheme("windows-terminal-schemes/lovelace.json")

    val criticalProperties = listOf(
        "Button.default.startBackground",
        "*.borderColor",
        "*.focusColor",
        "*.focusedBorderColor",
        "*.infoForeground",
        "Button.startBackground"
    )

    criticalProperties.forEach { property ->
        val demoColor = Color.fromHex(demo[property])
        val genColor = Color.fromHex(generated[property])
        val deltaE = ColorUtils.deltaE(demoColor, genColor)

        // Delta E < 3.0 is "just noticeable difference"
        assert(deltaE < 3.0) {
            "$property: Delta E = $deltaE (demo: ${demoColor.toHex()}, gen: ${genColor.toHex()})"
        }
    }
}
```

**Acceptatie criteria**:
- ✅ Delta E < 3.0 voor alle kritieke properties
- ✅ Delta E < 5.0 voor alle andere properties

#### 6.2 Multi-Theme Regression Test
**Doel**: Verifiëren dat wijzigingen andere thema's niet breken.

**Test Thema's**:
1. **Lovelace** (purple, dark) - target thema
2. **One Half Dark** (blue, dark) - blue accent test
3. **Nord** (blue/cyan, dark) - cyan accent test
4. **Solarized Light** (orange, light) - light theme test
5. **GitHub Dark** (blue, dark) - neutral theme test

**Test Procedure**:
```bash
# Genereer alle test thema's
./gradlew generateTheme -Pscheme=lovelace
./gradlew generateTheme -Pscheme=one-half-dark
./gradlew generateTheme -Pscheme=nord
./gradlew generateTheme -Pscheme=solarized-light
./gradlew generateTheme -Pscheme=github-dark

# Visual inspection checklist per thema:
# ☐ Accent color logisch (matches theme identity)
# ☐ Borders subtiel maar zichtbaar
# ☐ Buttons hebben contrast met achtergrond
# ☐ Focus indicators duidelijk zichtbaar
# ☐ Info text leesbaar
# ☐ Geen visuele glitches
```

**Regressie criteria**:
- ❌ **Blocker**: Focus niet zichtbaar, borders onzichtbaar, tekst onleesbaar
- ⚠️ **Warning**: Accent kleur minder mooi, kleuren minder levendig
- ✅ **OK**: Thema ziet er even goed of beter uit

#### 6.3 Accessibility Validatie
**Doel**: Verifiëren dat wijzigingen accessibility niet schaden.

**Contrast Checks**:
```kotlin
@Test
fun `All text has minimum contrast ratio`() {
    val generated = generateTheme("windows-terminal-schemes/lovelace.json")
    val background = Color.fromHex(generated["*.background"])

    val textProperties = listOf(
        "*.foreground",
        "*.infoForeground",
        "Label.foreground",
        "Button.foreground"
    )

    textProperties.forEach { property ->
        val textColor = Color.fromHex(generated[property])
        val contrast = ColorUtils.contrastRatio(textColor, background)

        // WCAG AA: minimum 4.5:1 voor normale tekst
        assert(contrast >= 4.5) {
            "$property: Contrast = $contrast (too low for AA)"
        }
    }
}

@Test
fun `Borders are visible`() {
    val generated = generateTheme("windows-terminal-schemes/lovelace.json")
    val background = Color.fromHex(generated["*.background"])
    val border = Color.fromHex(generated["*.borderColor"])

    val contrast = ColorUtils.contrastRatio(border, background)

    // Minimum 1.5:1 voor borders (subtiel maar zichtbaar)
    assert(contrast >= 1.5) {
        "Border contrast = $contrast (too low, invisible borders)"
    }
}
```

#### 6.4 Visual Regression Screenshots
**Doel**: Visueel vergelijken voor/na screenshots.

**Procedure**:
1. Screenshot VOOR wijzigingen:
   - Buttons (normal, default, disabled)
   - Borders (panels, dialogs, inputs)
   - Focus states (button, input, tree)
   - Selection (active, inactive)

2. Implementeer alle wijzigingen

3. Screenshot NA wijzigingen (zelfde UI elementen)

4. Side-by-side vergelijking in PR review

**Screenshot locaties**:
```
docs/testing/visual-regression/
├── lovelace-before/
│   ├── buttons.png
│   ├── borders.png
│   ├── focus.png
│   └── selection.png
└── lovelace-after/
    ├── buttons.png
    ├── borders.png
    ├── focus.png
    └── selection.png
```

#### 6.5 Performance Check
**Doel**: Verifiëren dat nieuwe algoritmes (accent detectie, etc.) niet significant trager zijn.

**Benchmark**:
```kotlin
@Test
fun `Theme generation performance acceptable`() {
    val iterations = 100

    val startTime = System.currentTimeMillis()
    repeat(iterations) {
        generateTheme("windows-terminal-schemes/lovelace.json")
    }
    val endTime = System.currentTimeMillis()

    val avgTime = (endTime - startTime) / iterations

    // Acceptabel: < 50ms per thema generatie
    assert(avgTime < 50) {
        "Average generation time: ${avgTime}ms (too slow)"
    }
}
```

### Test Deliverables
1. ✅ **Test rapport** met alle test resultaten
2. ✅ **Delta E metingen** voor Lovelace (voor/na tabel)
3. ✅ **Screenshots** van alle test thema's
4. ✅ **Regressie log** met eventuele issues gevonden
5. ✅ **Performance benchmark** resultaten

### Acceptatie Criteria Fase 6
- ✅ Lovelace Delta E < 3.0 voor alle kritieke kleuren
- ✅ Geen regressies in 5 test thema's
- ✅ Alle accessibility checks passed
- ✅ Visual approval van screenshots
- ✅ Performance < 50ms per thema

---

## **FASE 8: Template Consistency Fix - Normal/Rounded Variants**

### Doel
Corrigeer de template inconsistentie tussen de normale en rounded variant templates, waarbij Button.default kleuren onbedoeld verschillen terwijl alleen de arc waarden zouden moeten verschillen.

### Achtergrond
**VERIFICATIE**: De normale en rounded thema's worden correct gegenereerd met verschillende arc waarden (afgeronde hoeken). Echter, er is een template inconsistentie ontdekt waarbij de Button.default kleuren verschillen tussen beide templates, terwijl alleen geometrische eigenschappen (arc) zouden moeten verschillen.

**Huidige Situatie**:
- Normale variant: Button.default gebruikt `accentColor` en `$wt_brightWhite$`
- Rounded variant: Button.default gebruikt `$wt_blue$` en `$wt_background$`

Dit is een **onbedoelde divergentie** - kleurwaarden moeten consistent zijn tussen variants.

### Gevonden Verschil

**Locatie**: `buildSrc/templates/windows-terminal-rounded.template.theme.json` (lijnen 97-102)

**Huidige code (Rounded template)**:
```json
"default": {
  "startBackground": "$wt_blue$",
  "endBackground": "$wt_blue$",
  "foreground": "$wt_background$",
  "focusedBorderColor": "$wt_button_border_focused$",
  "borderColor": "$wt_button_border$"
}
```

**Normale template** (ter vergelijking):
```json
"default": {
  "foreground": "$wt_brightWhite$",
  "startBackground": "accentColor",
  "endBackground": "accentColor",
  "startBorderColor": "accentColor",
  "endBorderColor": "accentColor",
  "borderColor": "$wt_button_border$",
  "focusedBorderColor": "$wt_button_border_focused$",
  "focusColor": "$wt_blue$"
}
```

### Implementatie Stap

#### Stap 8.1: Harmoniseer Button.default Kleuren
**Bestand**: `buildSrc/templates/windows-terminal-rounded.template.theme.json`

**Wijzig lijnen 97-102**:
```json
"default": {
  "startBackground": "$wt_blue$",           // ← Verkeerd
  "endBackground": "$wt_blue$",             // ← Verkeerd
  "foreground": "$wt_background$",          // ← Verkeerd
  "focusedBorderColor": "$wt_button_border_focused$",
  "borderColor": "$wt_button_border$"
}
```

**Naar**:
```json
"default": {
  "foreground": "$wt_brightWhite$",
  "startBackground": "accentColor",
  "endBackground": "accentColor",
  "startBorderColor": "accentColor",
  "endBorderColor": "accentColor",
  "borderColor": "$wt_button_border$",
  "focusedBorderColor": "$wt_button_border_focused$",
  "focusColor": "$wt_blue$"
}
```

**Toelichting**:
- Dit maakt de kleuren identiek aan de normale template
- De rounded template behoudt nog steeds zijn `"arc": "$arc_button$"` property (lijn ~88)
- Na deze fix verschillen de variants ALLEEN in arc waarden, zoals bedoeld

### Verificatie Arc Waarden

Ter bevestiging dat de arc waarden correct zijn ingesteld:

| Component | Normal Arc | Rounded Arc | Status |
|-----------|------------|-------------|--------|
| Button | 0 | 6 | ✅ Correct |
| CheckBox | 0 | 3 | ✅ Correct |
| ComboBox | 0 | 4 | ✅ Correct |
| Component | 0 | 8 | ✅ Correct |
| Popup | 0 | 12 | ✅ Correct |
| ProgressBar | 0 | 4 | ✅ Correct |
| TabbedPane | 0 | 8 | ✅ Correct |
| TextField | 0 | 4 | ✅ Correct |
| Tree | 0 | 4 | ✅ Correct |

Deze waarden worden correct toegepast via `ThemeVariant.kt` en `variant.arcValues.toPlaceholders()`.

### Waarom User Dacht Ze Waren Identiek

**Mogelijke redenen**:
1. **Visuele subtiliteit**: Border radius van 4-12px is subtiel, vooral op kleinere componenten
2. **Verkeerde components getest**: Sommige components (zoals Table) hebben arc=0 in beide variants
3. **IDE restart nodig**: JetBrains IDEs vereisen soms volledige restart voor UI theme properties
4. **Kleur inconsistentie**: De Button kleur verschillen maakten ze juist MEER verschillend in sommige gebieden, wat verwarrend was

### Verwachte Resultaat Fase 8
- ✅ Button.default kleuren identiek tussen normal en rounded
- ✅ Alleen arc waarden (geometrie) verschillen tussen variants
- ✅ Template consistency hersteld
- ✅ Thema's blijven correct gegenereerd met hun beoogde verschillen

### Test Strategie Fase 8
1. **Regenereer beide variants** van Lovelace thema
2. **Visuele inspectie**:
   - Normal variant: Scherpe hoeken, paarse accent buttons
   - Rounded variant: Afgeronde hoeken, paarse accent buttons (zelfde kleur!)
3. **Diff check**: Alleen arc properties mogen verschillen in JSON
4. **Screenshot comparison**: Buttons in beide variants (zelfde kleur, verschillende hoeken)

### Acceptatie Criteria Fase 8
- ✅ Button.default kleuren identiek in beide template bestanden
- ✅ Gegenereerde themes hebben alleen arc verschillen (geen kleur verschillen)
- ✅ Visual confirmation: rounded variant heeft zichtbare afgeronde hoeken
- ✅ No regression: normal variant blijft scherpe hoeken hebben

---

## **FASE 9: Documentation & Template Updates**

### Doel
Documenteer alle wijzigingen, update templates met nieuwe placeholders, en voorzie maintainability voor de toekomst.

### Implementatie Stappen

#### Stap 7.1: Update Placeholder Conventions Documentatie
**Bestand**: `docs/placeholder-conventions.md`

**Toevoegen**:
```markdown
## Nieuwe Placeholders (v2.0)

### Accent & Focus
- `$wt_accent_color$` - Auto-detected accent color (replaces hardcoded $wt_brightBlue$)
  - **Algoritme**: `WindowsTerminalColorScheme.detectAccentColor()`
  - **Gebruikt voor**: Default buttons, primary actions, links
  - **Voorbeelden**:
    - Lovelace: #A77AE6 (purple)
    - One Half Dark: #61AFEF (blue)
    - Nord: #88C0D0 (cyan)

- `$wt_focus_color$` - Focus indicator color (lighter than accent)
  - **Formula**: `lighten(accentColor, 0.15)` + contrast check
  - **Minimum contrast**: 2.5:1 against background

- `$wt_focused_border_color$` - Focused element border
  - **Formula**: `accentColor` (direct)

### UI Surfaces
- `$wt_button_background$` - Normal button background
  - **Formula**: `lighten(background, 0.08)`
  - **Rationale**: Subtle elevation, replaces using background directly

- `$wt_info_foreground$` - Info/hint text color
  - **Formula**: `blend(foreground, background, 0.35)`
  - **Rationale**: Muted gray for subtle hints (replaces cyan)

### Border Colors
- `$wt_border_color$` - Updated with subtle mode
  - **Formula**: `createVisibleBorderColor(background, subtle=true)`
  - **Change**: Now uses value=0.22 instead of 0.40 for subtler borders
```

#### Stap 7.2: Update Theme Generation Documentatie
**Bestand**: `docs/theme-generation.md`

**Nieuwe sectie toevoegen**:
```markdown
## Color Derivation Algorithm Changes (v2.0)

### Accent Color Detection
Previous: Always used `brightBlue` as accent
Current: Auto-detects based on saturation, brightness, and uniqueness

**Algorithm**:
1. Analyze all bright colors (brightBlue, brightPurple, brightCyan, etc.)
2. Score each on: saturation (40%), brightness (30%), uniqueness (30%)
3. Select highest scoring color as accent
4. Used for: buttons, focus, links

**Benefit**: Themes with purple/cyan accents now use appropriate colors

### Border Color Improvements
Previous: HSV value=0.4, min contrast 3:1
Current: HSV value=0.22, min contrast 1.8:1 (subtle mode)

**Rationale**: WCAG-compliant borders were too prominent, subtle mode matches designer preferences while maintaining usability

### Derived Color Formula Updates
| Color | Old Formula | New Formula | Reason |
|-------|-------------|-------------|--------|
| buttonBackground | `black` | `lighten(bg, 0.08)` | Subtle elevation |
| selectionInactive | `darken(sel, 0.40)` | `darken(sel, 0.48)` | Match demo |
| infoForeground | `cyan` | `blend(fg, bg, 0.35)` | Muted hints |
```

#### Stap 7.3: Template Comments Toevoegen
**Bestand**: `buildSrc/templates/windows-terminal.template.theme.json`

**Voeg header comment toe**:
```json
{
  "_comment": "Windows Terminal Theme Template v2.0",
  "_description": "Generates IntelliJ themes from Windows Terminal color schemes",
  "_placeholders": {
    "$wt_accent_color$": "Auto-detected primary accent (see detectAccentColor())",
    "$wt_focus_color$": "Focus indicator (lighten(accent, 0.15) + contrast)",
    "$wt_button_background$": "Button surface (lighten(background, 0.08))",
    "$wt_info_foreground$": "Hint text (blend(foreground, background, 0.35))",
    "$wt_border_color$": "Subtle borders (createVisibleBorderColor subtle mode)"
  },

  "colors": {
    // ... actual mappings ...
  }
}
```

**Voeg inline comments toe voor complexe mappings**:
```json
{
  "colors": {
    // Accent-based colors (auto-detected from scheme)
    "Button.default.startBackground": "$wt_accent_color$",
    "*.focusColor": "$wt_focus_color$",
    "*.focusedBorderColor": "$wt_focused_border_color$",

    // Surface colors (subtle elevation)
    "Button.startBackground": "$wt_button_background$",
    "ActionButton.hoverBackground": "$wt_button_background$",

    // Info/hint text (muted)
    "*.infoForeground": "$wt_info_foreground$",
    "Label.infoForeground": "$wt_info_foreground$",

    // Subtle borders
    "*.borderColor": "$wt_border_color$",
  }
}
```

#### Stap 7.4: Create Migration Guide
**Nieuw bestand**: `docs/migration-guide-v2.md`

```markdown
# Migration Guide: v1 → v2 Theme Generation

## Breaking Changes

### 1. Accent Color No Longer Hardcoded
**Impact**: Themes may use different accent colors
- **Before**: All themes used `brightBlue` (#61AFEF-ish)
- **After**: Purple themes use purple, cyan themes use cyan, etc.

**Migration**: None needed (automatic detection)

### 2. Border Colors Subtler
**Impact**: Borders are less prominent
- **Before**: High contrast borders (3:1 ratio)
- **After**: Subtle borders (1.8:1 ratio)

**Migration**: If you need old borders, call `createVisibleBorderColor(bg, subtle=false)`

### 3. Info Foreground Changed
**Impact**: Info text no longer cyan
- **Before**: `infoForeground = cyan` (#4DD0E1)
- **After**: `infoForeground = muted gray` (~#B0B0B6)

**Migration**: Override in template if you want cyan

## New Features

- ✅ Automatic accent color detection
- ✅ Smarter focus colors (based on accent)
- ✅ Improved surface colors (buttons)
- ✅ Better derived color formulas

## Testing Your Theme

After upgrading, regenerate your theme and check:
1. ☐ Accent color is appropriate (not always blue)
2. ☐ Borders are visible but subtle
3. ☐ Buttons have contrast with background
4. ☐ Focus indicators work correctly
5. ☐ Info text is readable
```

#### Stap 7.5: Update README
**Bestand**: `docs/README.md`

**Wijzig "Supported Features" sectie**:
```markdown
## Color Generation Features

### Automatic Color Derivation
- ✅ **Smart Accent Detection** - Analyzes scheme to pick best accent color
- ✅ **Subtle Borders** - Visible but not distracting (v2.0)
- ✅ **Surface Colors** - Proper elevation hierarchy
- ✅ **Focus Indicators** - Accessible and accent-matched
- ✅ **Semantic Colors** - Muted hints, appropriate warnings/errors

### Algorithms
- **Border Colors**: `createVisibleBorderColor()` with subtle mode
- **Accent Detection**: Saturation + brightness + uniqueness scoring
- **Derived Colors**: Tuned percentages based on designer references
- **Contrast Checking**: Ensures accessibility (WCAG AA)
```

#### Stap 7.6: Code Comments in Core Functions
**Bestand**: `buildSrc/src/main/kotlin/colorschemes/WindowsTerminalColorScheme.kt`

**Voeg KDoc toe aan detectAccentColor()**:
```kotlin
/**
 * Detects the primary accent color from the Windows Terminal color scheme.
 *
 * Analyzes all bright colors (brightBlue, brightPurple, brightCyan, etc.) and scores them
 * based on:
 * - **Saturation** (40% weight): How vivid/colorful the color is
 * - **Brightness** (30% weight): How light the color is (for visibility)
 * - **Uniqueness** (30% weight): How distinct from other colors (for prominence)
 *
 * The highest-scoring color becomes the accent, used for:
 * - Default/primary buttons
 * - Focus indicators
 * - Links and primary actions
 *
 * **Examples**:
 * - Lovelace scheme → brightPurple (#A77AE6)
 * - One Half Dark → brightBlue (#61AFEF)
 * - Nord → brightCyan (#88C0D0)
 *
 * @return The detected accent color
 * @since 2.0
 */
private fun detectAccentColor(): Color {
    // ... implementation ...
}
```

**Voeg KDoc toe aan createVisibleBorderColor()**:
```kotlin
/**
 * Creates a border color that is visible against the background but not overly prominent.
 *
 * **Subtle Mode** (default for theme generation):
 * - HSV value: 0.22 (quite dark for dark backgrounds)
 * - Saturation: 30% of original (neutral gray-ish)
 * - Minimum contrast: 1.8:1 (visible but subtle)
 *
 * **Standard Mode** (for accessibility-critical borders):
 * - HSV value: 0.40 (lighter, more visible)
 * - Saturation: 50% of original
 * - Minimum contrast: 3:1 (WCAG compliant)
 *
 * @param backgroundColor The background to create border for
 * @param minContrast Minimum contrast ratio (default 3.0)
 * @param subtle Whether to use subtle mode (default false)
 * @return Border color with appropriate visibility
 * @since 1.0 (subtle mode added in 2.0)
 */
fun createVisibleBorderColor(
    backgroundColor: Color,
    minContrast: Double = 3.0,
    subtle: Boolean = false
): Color {
    // ... implementation ...
}
```

### Deliverables Fase 7
1. ✅ **Updated docs/placeholder-conventions.md** met nieuwe placeholders
2. ✅ **Updated docs/theme-generation.md** met algoritme wijzigingen
3. ✅ **New docs/migration-guide-v2.md** voor gebruikers
4. ✅ **Updated docs/README.md** met feature highlights
5. ✅ **Commented templates** voor maintainability
6. ✅ **KDoc comments** in core functions

### Acceptatie Criteria Fase 7
- ✅ Alle nieuwe placeholders gedocumenteerd
- ✅ Migratie guide compleet
- ✅ Code comments duidelijk en nuttig
- ✅ Future maintainers kunnen wijzigingen begrijpen

---

## **KRITIEK: Post-Fase Subagent Validatie Protocol**

**Na elke geïmplementeerde fase MOETEN parallel subagents zonder context worden ingezet om:**
1. Wijzigingen te analyseren op mogelijke bugs
2. Gerelateerde componenten te scannen op side effects
3. Edge cases te identificeren die gemist zijn
4. Code quality en architecture te valideren

### Subagent Validatie Workflow

**Per Fase:**
```
IMPLEMENTATIE FASE X
    ↓
SPAWN 3-5 PARALLEL SUBAGENTS (ZONDER CONTEXT)
    ↓
┌─────────────┬──────────────┬─────────────┬──────────────┐
│ Subagent 1  │ Subagent 2   │ Subagent 3  │ Subagent 4   │
│ Bug Hunter  │ Side Effects │ Edge Cases  │ Code Quality │
└─────────────┴──────────────┴─────────────┴──────────────┘
    ↓           ↓              ↓             ↓
AGGREGEER BEVINDINGEN
    ↓
FIX GEVONDEN ISSUES (indien nodig)
    ↓
VOLGENDE FASE
```

**Subagent Types:**

1. **Bug Hunter Agent**
   - Prompt: "Analyseer [gewijzigde bestanden] op potentiële bugs zonder context van de requirements. Zoek naar: null pointer exceptions, type mismatches, off-by-one errors, resource leaks, infinite loops."
   - Model: `sonnet` (grondige analyse)

2. **Side Effects Agent**
   - Prompt: "Zoek alle code die [gewijzigde functie/class] GEBRUIKT. Analyseer of de wijzigingen breaking changes introduceren of unexpected behavior kunnen veroorzaken."
   - Model: `sonnet`

3. **Edge Cases Agent**
   - Prompt: "Bedenk extreme edge cases voor [gewijzigde functionaliteit]: empty inputs, null values, extreme values (0, negative, MAX_INT), unicode, concurrent access. Test conceptueel of de implementatie deze handled."
   - Model: `haiku` (snelle brainstorm)

4. **Code Quality Agent**
   - Prompt: "Review [gewijzigde code] op: naming conventions, documentation, complexity (cyclomatic), SOLID principles, testability. Geen context van requirements."
   - Model: `haiku`

5. **Regression Risk Agent** (optional voor kritieke fases)
   - Prompt: "Analyseer [gewijzigde algoritme] en predict welke bestaande thema's (One Half Dark, Nord, Solarized, etc.) mogelijk negatief beïnvloed worden. Leg uit waarom."
   - Model: `sonnet`

**Voorbeeld Voor Fase 1 (Accent Detection):**

Na implementatie `detectAccentColor()`:
```bash
# Spawn 4 parallel agents in één message:
Task tool × 4 parallel calls:

1. "Analyseer WindowsTerminalColorScheme.kt:detectAccentColor() op bugs
    zonder requirements context. Focus: division by zero, null colors,
    empty candidates list, tie-breaking logic."

2. "Zoek alle usages van detectAccentColor() in codebase. Analyseer of
    return value correct gehandled wordt (null checks, type safety)."

3. "Edge cases voor accent detection: scheme met alle grijstinten, scheme
    met identieke bright colors, scheme met missing colors. Handled?"

4. "Review detectAccentColor() code quality: function length, magic numbers
    (0.4, 0.3, 0.3), variable naming, documentation."
```

**Aggregatie & Action:**
- Alle agent reports verzamelen
- Bugs met HIGH severity → immediate fix
- Side effects → add to test plan
- Edge cases → add unit tests
- Code quality → refactor indien nodig

**STOP CRITERIA:**
- ❌ Fase mag NIET doorgaan als:
  - HIGH severity bugs gevonden
  - Breaking changes zonder mitigatie
  - >3 kritieke edge cases niet gehandled

- ✅ Fase mag doorgaan als:
  - Geen HIGH severity issues
  - LOW/MEDIUM issues gedocumenteerd voor later
  - Edge cases covered OF bewust geaccepteerd

### **WHEN TO USE SUBAGENTS: Detailed Triggers**

**MANDATORY - Gebruik subagents in deze situaties:**

1. **Na elke fase implementatie** (zoals hierboven beschreven)
   - Bug Hunter, Side Effects, Edge Cases, Code Quality agents
   - 3-5 parallel agents zonder context

2. **Tijdens complexe algorithme ontwikkeling**
   - Trigger: Als je een nieuw algoritme schrijft (bijv. `detectAccentColor()`)
   - Spawn: 2-3 agents om verschillende implementatie benaderingen te evalueren
   - Vraag: "Implementeer accent detection met [approach A/B/C], vergelijk trade-offs"

3. **Bij onverwachte test failures**
   - Trigger: Test faalt die zou moeten slagen
   - Spawn: Debug agent zonder context van je aannames
   - Vraag: "Analyseer waarom test X faalt, negeer mijn verklaring, vind root cause"

4. **Bij multi-file refactoring**
   - Trigger: Wijzigingen raken >3 bestanden
   - Spawn: Dependency analysis agent
   - Vraag: "Map alle dependencies van [gewijzigde files], vind breaking changes"

5. **Voordat je een fase als 'compleet' markeert**
   - Trigger: Je denkt dat fase klaar is
   - Spawn: Skeptical reviewer agent
   - Vraag: "Review fase X implementatie, zoek wat er NIET werkt, niet wat wel werkt"

6. **Bij twijfel over approach**
   - Trigger: Je hebt 2+ mogelijke oplossingen
   - Spawn: Architecture comparison agents (1 per approach)
   - Vraag: "Implementeer [approach X], lijst alle nadelen en edge cases"

**OPTIONAL - Gebruik subagents wanneer nuttig:**

7. **Voor test case generatie**
   - Spawn: Test scenario brainstorm agent
   - Vraag: "Genereer 20 test cases voor [functionaliteit], focus op edge cases"

8. **Voor performance analyse**
   - Spawn: Performance profiling agent
   - Vraag: "Analyseer [code] op performance bottlenecks, bereken big-O complexity"

9. **Voor documentatie review**
   - Spawn: Documentation clarity agent
   - Vraag: "Lees [doc] zonder code context, lijst onduidelijkheden"

### **Plan Update Protocol - Living Document Approach**

**Dit plan is een LEVEND DOCUMENT dat evolueert tijdens implementatie.**

**Na elke fase MOET een "Plan Retrospective" uitgevoerd worden:**

#### Plan Retrospective Workflow

```
FASE COMPLEET + SUBAGENT VALIDATIE
    ↓
SPAWN PLAN IMPROVEMENT AGENT
    ↓
"Analyseer:
 1. Wat ging anders dan gepland in Fase X?
 2. Welke aannames bleken onjuist?
 3. Welke nieuwe inzichten hebben we?
 4. Hoe moeten volgende fases aangepast worden?
 5. Zijn de success metrics nog realistisch?"
    ↓
AGGREGEER BEVINDINGEN
    ↓
UPDATE PLAN (indien nodig)
    ↓
COMMIT PLAN v[X.Y] met changelog
    ↓
VOLGENDE FASE
```

#### Wanneer Plan Updaten?

**MUST UPDATE - Plan moet bijgewerkt worden als:**

1. **Algoritme aannames onjuist blijken**
   - Voorbeeld: "Border value 0.22 breekt lichte thema's"
   - Action: Update Fase 2 met nieuwe value, voeg light theme test toe

2. **Nieuwe dependencies ontdekt**
   - Voorbeeld: "Accent detection heeft ook cursor color nodig"
   - Action: Voeg cursor color mapping toe aan Fase 1 scope

3. **Success metrics niet haalbaar**
   - Voorbeeld: "Delta E <3.0 niet mogelijk voor Button.default"
   - Action: Update metrics naar realistic targets per component

4. **Scope creep gedetecteerd**
   - Voorbeeld: "Fase 3 probeert ook tab colors te fixen (not in scope)"
   - Action: Verplaats tab colors naar Fase 3b of documenteer als out-of-scope

5. **Blocking issues in volgende fases**
   - Voorbeeld: "Fase 5 kan niet zonder Fase 1 output"
   - Action: Wijzig volgorde of voeg dependency handling toe

**SHOULD UPDATE - Plan zou bijgewerkt moeten worden als:**

6. **Betere aanpak gevonden**
   - Voorbeeld: "Accent detection via color frequency ipv saturation"
   - Action: Documenteer alternatief, overweeg voor v3.0

7. **Performance bottleneck ontdekt**
   - Voorbeeld: "Subtiele borders 10x langzamer"
   - Action: Voeg caching strategy toe aan plan

8. **Edge cases gevonden die plan moet coveren**
   - Voorbeeld: "Grayscale thema's hebben geen accent"
   - Action: Voeg grayscale handling toe aan Fase 1

**MAY UPDATE - Optionele plan updates:**

9. **Code quality improvements**
   - Voorbeeld: "Extract color calculation naar utility class"
   - Action: Voeg refactoring task toe (low priority)

10. **Documentation gaps**
    - Voorbeeld: "KDoc mist examples"
    - Action: Verbeter Fase 9 documentatie requirements

#### Plan Versioning

**Gebruik semantic versioning voor plan updates:**

```
Plan v1.0 - Initieel plan (huidige versie)
Plan v1.1 - Minor update na Fase 1 (betere approach gevonden)
Plan v1.2 - Minor update na Fase 2 (edge case toegevoegd)
Plan v2.0 - Major update (fundamentele aanname onjuist, grote wijziging)
```

**Changelog format:**
```markdown
## Plan Changelog

### v1.2 - 2025-11-24 (Na Fase 2)
**Changed:**
- Border subtle mode: value 0.22 → 0.20 (light themes fix)
- Added light theme validation to Fase 7

**Added:**
- Fase 2b: Separate handling voor light vs dark borders

**Lessons Learned:**
- Border calculation is niet universeel, needs theme-type awareness
```

#### Voorbeeld: Plan Update Na Fase 1

**Scenario:** Fase 1 implementatie van `detectAccentColor()` onthult dat:
1. Lovelace's `#7A3A82` is inderdaad niet afleidbaar (zoals voorspeld)
2. Maar: We ontdekken dat `brightPurple` gemengd met `purple` het WEL benadert
3. Onverwacht: Nord theme kiest groene accent ipv blauwe

**Plan Update Actie:**
```markdown
## Plan v1.1 Update (Na Fase 1 Implementatie)

### Wijzigingen in Fase 1:
**VOOR:**
```kotlin
val detectedAccent = detectAccentColor()  // Simpel kiezen
```

**NA:**
```kotlin
val detectedAccent = detectAccentColor()
val refinedAccent = if (isPurpleThemed()) {
    ColorUtils.blend(brightPurple, purple, 0.60)  // Benadering van #7A3A82
} else {
    detectedAccent
}
```

**Impact op volgende fases:**
- Fase 5 (Focus colors): Moet `refinedAccent` gebruiken ipv `detectedAccent`
- Fase 7 (Testing): Voeg Nord accent test toe (verwacht green, krijgt blue)

**Updated Success Metrics:**
- Button.default Delta E: <3.0 → <8.0 (accepteer dat blend 60/40 niet perfect is)

**New Risks:**
- `isPurpleThemed()` detection kan falen voor edge case thema's
- Mitigatie: Manual override in config
```

**Commit:**
```bash
git commit -m "docs: update plan v1.1 after Fase 1 learnings

- Add purple accent blending strategy
- Update success metrics for Button.default (Delta E <8.0)
- Add Nord theme accent test to Fase 7
- Document isPurpleThemed() edge case risk"
```

### **Subagent Usage Summary**

| Timing | Type | Count | Purpose |
|--------|------|-------|---------|
| **After each phase** | Validation agents | 3-5 | Bug hunting, side effects, edge cases, quality |
| **During algo dev** | Implementation agents | 2-3 | Explore approaches, compare trade-offs |
| **On test failure** | Debug agent | 1 | Root cause without bias |
| **Multi-file refactor** | Dependency agent | 1 | Map breaking changes |
| **Before phase complete** | Skeptical reviewer | 1 | Find what doesn't work |
| **On approach doubt** | Comparison agents | 2-4 | Evaluate alternatives |
| **After phase (retrospective)** | Plan improvement agent | 1 | Analyze learnings, suggest updates |

**Total per phase:** ~6-12 subagent invocations (validation + retrospective)
**Total project:** 48-96 subagent analyses

---

## Samenvatting Implementatie Volgorde

### Week 1: Core UI Theme Algorithm Changes
- ✅ **Dag 1-2**: Fase 1 (Accent Detection) → **+ Subagent Validatie (3-4 agents)**
- ✅ **Dag 3-4**: Fase 2 (Border Colors - Subtle Mode) → **+ Subagent Validatie**
- ✅ **Dag 5**: Fase 3 (Derived Colors) → **+ Subagent Validatie**

### Week 2: UI Refinements & Editor Fixes
- ✅ **Dag 1**: Fase 4 (Info Foreground - Muted Gray) → **+ Subagent Validatie**
- ✅ **Dag 2**: Fase 5 (Focus Colors - Accent-based) → **+ Subagent Validatie**
- ✅ **Dag 3**: Fase 6 (Editor - Fix invisible comments) **KRITIEK** → **+ Subagent Validatie**
- ✅ **Dag 4**: Fase 6 (Editor - Fix syntax colors) → **+ Subagent Validatie**
- ✅ **Dag 5**: Fase 8 (Template Consistency) → **+ Subagent Validatie**

### Week 3: Testing, Validation & Documentation
- ✅ **Dag 1-2**: Fase 7 (Testing - unit tests + visual tests)
- ✅ **Dag 3**: Fase 7 (Testing - multi-theme regression + accessibility)
- ✅ **Dag 4**: Fase 7 (Testing - final validation + screenshots)
- ✅ **Dag 5**: Fase 9 (Documentation - placeholders, migration guide, KDoc)

### Week 4: Final Review & Release
- ✅ **Dag 1**: Code review + addressing feedback
- ✅ **Dag 2**: User testing + bug fixes
- ✅ **Dag 3**: Release notes + changelog
- ✅ **Dag 4**: Version bump (v2.0) + PR
- ✅ **Dag 5**: Merge + announcement

**Totaal Subagent Validaties: 8 fases × 3-5 agents = 24-40 parallel analyses**

---

## Risico's & Mitigaties

### Hoog Risico
| Risico | Impact | Mitigatie |
|--------|--------|-----------|
| Border contrast te laag → accessibility issues | Gebruikers kunnen UI elementen niet zien | Unit tests voor minimum 1.5:1 contrast, visual testing |
| Accent detection kiest verkeerde kleur | Thema ziet er raar uit | Fallback naar brightBlue, manual override mogelijkheid |
| Regressies in andere thema's | Bestaande thema's breken | Multi-theme regression test, screenshots |

### Medium Risico
| Risico | Impact | Mitigatie |
|--------|--------|-----------|
| Performance degradatie door nieuwe algoritmes | Langzamere theme generatie | Benchmark tests, optimalisatie indien nodig |
| Breaking changes voor gebruikers | Gebruikers moeten migreren | Duidelijke migratie guide, backwards compatibility waar mogelijk |

### Laag Risico
| Risico | Impact | Mitigatie |
|--------|--------|-----------|
| Documentatie onduidelijk | Toekomstige maintainers begrijpen code niet | Peer review van docs, code comments |
| Edge cases niet afgevangen | Rare thema's genereren niet goed | Comprehensive test matrix |

---

## Success Metrics

### Kwantitatief
**UI Theme:**
- ✅ **Delta E < 3.0** voor alle 13 Lovelace UI kleurverschillen
- ✅ **0 regressies** in 5 test thema's (UI)
- ✅ **100% accessibility tests** passed (borders, contrast)
- ✅ **< 50ms** theme generation tijd

**Editor Color Scheme:**
- ✅ **Comments zichtbaar** in alle thema's (niet zwart op zwart)
- ✅ **Syntax kleuren correct** (numbers=blauw, classes=paars, etc.)
- ✅ **0 regressies** in multi-language syntax highlighting
- ✅ **Minimum 2.5:1 contrast** voor comment colors

**Template Consistency:**
- ✅ **Normal/Rounded verschillen alleen in arc waarden** (geen kleur verschillen)
- ✅ **Button.default kleuren identiek** tussen variants

### Kwalitatief
- ✅ Lovelace thema is **visueel identiek** aan demo (bij visual comparison)
- ✅ **Editor bruikbaar**: Comments leesbaar, syntax highlighting logisch
- ✅ Code is **maintainable** (documentatie + comments)
- ✅ Wijzigingen zijn **reusable** (helpen alle thema's)
- ✅ Gebruikers zijn **tevreden** (feedback positief)

---

## Volgende Stappen Na Implementatie

1. **PR Review** - Code review met team
2. **User Testing** - Beta test met gebruikers
3. **Release Notes** - Changelog schrijven
4. **Version Bump** - v1.x → v2.0
5. **Communication** - Announce breaking changes

---

**Einde Implementatieplan**
