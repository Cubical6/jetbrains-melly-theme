# Local Plugin Testing Guide

Complete guide voor het lokaal builden en testen van de JetBrains Melly Theme plugin.

---

## Methode 1: Clean Build + Installatie (Aanbevolen)

### Stap 1: Clean Build
```bash
# Volledige clean build
./gradlew clean buildPlugin

# Of met extra validatie
./gradlew clean build
```

**Output locatie:** `build/distributions/jetbrains-melly-theme-<version>.zip`

### Stap 2: Installeer in IntelliJ IDEA

#### Via UI (Gemakkelijkst):
1. Open IntelliJ IDEA
2. **Settings/Preferences** → **Plugins**
3. Klik op **⚙️ (tandwiel icoon)** → **Install Plugin from Disk...**
4. Navigeer naar: `build/distributions/jetbrains-melly-theme-<version>.zip`
5. Selecteer het ZIP bestand
6. Klik **OK**
7. **Restart IntelliJ** wanneer gevraagd

#### Via Command Line:
```bash
# Zoek je IntelliJ plugins directory
# Linux: ~/.local/share/JetBrains/IntelliJIdea<version>/
# macOS: ~/Library/Application Support/JetBrains/IntelliJIdea<version>/
# Windows: %APPDATA%\JetBrains\IntelliJIdea<version>\

# Unzip naar plugins directory
unzip build/distributions/jetbrains-melly-theme-*.zip -d ~/.local/share/JetBrains/IntelliJIdea2024.1/plugins/
```

### Stap 3: Activeer een Theme

1. **Settings/Preferences** → **Appearance & Behavior** → **Appearance**
2. In **Theme** dropdown, zoek naar "wt-" themes
3. Kies bijvoorbeeld:
   - **wt-lovelace-abd97252** (Lovelace standard)
   - **wt-lovelace-abd97252_rounded** (Lovelace met rounded corners)
   - **wt-dracula-ede07c70** (Dracula)
   - **wt-nord-49f8aa67** (Nord)
4. Klik **Apply** → **OK**

---

## Methode 2: Run IDE Instance (Development)

Voor snellere testing tijdens development:

### Stap 1: Start Development IDE
```bash
# Start een nieuwe IntelliJ instance met plugin geladen
./gradlew runIde
```

Dit opent een **sandbox IDE** met de plugin automatisch geïnstalleerd.

### Stap 2: Test in Sandbox
- Sandbox IDE opent automatisch
- Plugin is al geïnstalleerd
- Test themes zonder main IDE te beïnvloeden
- Wijzigingen vereisen rebuild + restart sandbox

### Voordelen:
- ✅ Geen installatie nodig
- ✅ Veilig testen (main IDE blijft onaangetast)
- ✅ Snelle iteratie

### Nadelen:
- ⚠️ Langzamer opstarten
- ⚠️ Extra IDE instance draait

---

## Methode 3: Build + Auto-Install (Advanced)

Voor frequente testing:

### Setup Script
Maak een `install-local.sh`:
```bash
#!/bin/bash

# Build plugin
echo "🔨 Building plugin..."
./gradlew clean buildPlugin

if [ $? -ne 0 ]; then
  echo "❌ Build failed!"
  exit 1
fi

# Find IntelliJ plugins directory
IDEA_PLUGINS="$HOME/.local/share/JetBrains/IntelliJIdea2024.1/plugins"

# Remove old version
echo "🗑️  Removing old plugin..."
rm -rf "$IDEA_PLUGINS/jetbrains-melly-theme"

# Install new version
echo "📦 Installing new plugin..."
unzip -q build/distributions/jetbrains-melly-theme-*.zip -d "$IDEA_PLUGINS/"

echo "✅ Plugin installed!"
echo "⚠️  Please restart IntelliJ IDEA to see changes"
```

### Gebruik:
```bash
chmod +x install-local.sh
./install-local.sh
```

---

## Testing Checklist

### 1. Visuele Checks

#### UI Theme Testing:
- [ ] **Toolbar** - Correct background en borders
- [ ] **Editor tabs** - Tabs hebben juiste kleuren
- [ ] **Sidebar** - Project tree ziet er goed uit
- [ ] **Popups** - Autocomplete/dropdown styling
- [ ] **Buttons** - Hover en focus states
- [ ] **Status bar** - Onderkant van IDE

#### Rounded vs Standard:
- [ ] **Buttons** - Rounded heeft ronde hoeken (arc: 6)
- [ ] **Inputs** - Text fields hebben arc: 4
- [ ] **Popups** - Autocomplete heeft arc: 12
- [ ] **Tabs** - Editor tabs hebben arc: 8
- [ ] **Compare side-by-side** - Wissel tussen standard en _rounded variant

### 2. Editor Color Scheme Testing:

Test syntax highlighting:
```java
// Test verschillende syntax elementen
public class TestClass {
    private static final String CONSTANT = "test";

    /**
     * JavaDoc comment
     */
    public void testMethod() {
        int number = 42;
        String text = "Hello, World!";

        // Comment
        if (number > 0) {
            System.out.println(text);
        }
    }
}
```

Check of deze elementen duidelijk te onderscheiden zijn:
- [ ] Keywords (`public`, `class`, `if`)
- [ ] Strings (`"test"`)
- [ ] Numbers (`42`)
- [ ] Comments (groen/grijs)
- [ ] JavaDoc (speciale kleur)
- [ ] Constants (all caps)

### 3. Theme-Specific Features:

Test nieuwe features uit deze PR:
- [ ] **Islands** - Floating tool windows (als ondersteund)
- [ ] **MainToolbar** - Toolbar separator lines
- [ ] **MainWindow.Tab** - Tab styling in welcome screen
- [ ] **Editor underlined tabs** - Tabs met onderstreping
- [ ] **Icon colors** - Check of icons theme-kleuren gebruiken

### 4. Multiple Themes:

Test verschillende themes om variatie te zien:
- [ ] **Dark themes** - Lovelace, Dracula, Nord, Gruvbox Dark
- [ ] **Light themes** - Gruvbox Light, Ayu Light, Tomorrow
- [ ] **Rounded variants** - Test _rounded versies

---

## Troubleshooting

### Plugin niet zichtbaar in Plugins lijst

**Probleem:** Plugin verschijnt niet na installatie

**Oplossingen:**
1. Check of ZIP correct geïnstalleerd is:
   ```bash
   ls -la ~/.local/share/JetBrains/IntelliJIdea*/plugins/jetbrains-melly-theme/
   ```
2. Controleer `plugin.xml`:
   ```bash
   cat build/distributions/jetbrains-melly-theme-*/META-INF/plugin.xml
   ```
3. Check IntelliJ logs:
   ```bash
   tail -f ~/.local/share/JetBrains/IntelliJIdea*/log/idea.log
   ```

### Themes niet in dropdown

**Probleem:** Plugin geïnstalleerd maar themes verschijnen niet

**Oplossingen:**
1. Verifieer `plugin.xml` bevat themeProvider entries:
   ```bash
   grep -c "themeProvider" src/main/resources/META-INF/plugin.xml
   # Moet 116 zijn
   ```
2. Check dat theme files bestaan:
   ```bash
   ls src/main/resources/themes/*.theme.json | wc -l
   # Moet 116 zijn
   ```
3. Rebuild plugin volledig:
   ```bash
   ./gradlew clean createThemes buildPlugin
   ```

### Rounded corners niet zichtbaar

**Probleem:** Rounded variant ziet er hetzelfde uit als standard

**Mogelijke oorzaken:**
1. **IntelliJ versie te oud** - Minimaal 2021.3+ vereist voor arc support
2. **parentTheme ontbreekt** - Check in generated theme file:
   ```bash
   grep parentTheme src/main/resources/themes/wt-lovelace-abd97252_rounded.theme.json
   # Moet "ExperimentalDark" bevatten
   ```
3. **OS rendering** - Sommige OS'en renderen arcs niet perfect

### Kleuren zien er vreemd uit

**Probleem:** UI kleuren zijn niet zoals verwacht

**Debug stappen:**
1. Check of placeholders vervangen zijn:
   ```bash
   grep '\$wt_' src/main/resources/themes/wt-lovelace-abd97252.theme.json
   # Mag GEEN output geven (alle placeholders vervangen)
   ```
2. Verifieer bright colors:
   ```bash
   grep brightYellow src/main/resources/themes/wt-lovelace-abd97252.theme.json
   # Moet hex color code tonen, niet placeholder
   ```

### Build errors

**Probleem:** `./gradlew buildPlugin` faalt

**Oplossingen:**
1. Clean gradle cache:
   ```bash
   ./gradlew clean
   rm -rf .gradle build
   ./gradlew buildPlugin
   ```
2. Check Java versie:
   ```bash
   java -version
   # Moet Java 11 of hoger zijn
   ```
3. Gradle daemon herstarten:
   ```bash
   ./gradlew --stop
   ./gradlew buildPlugin
   ```

---

## Handige Gradle Tasks

```bash
# Theme generatie alleen
./gradlew createThemes

# Build zonder tests
./gradlew buildPlugin -x test

# Run IDE met plugin
./gradlew runIde

# Verifieer plugin configuratie
./gradlew verifyPluginConfiguration

# Clean alles
./gradlew clean

# Build en run in één keer
./gradlew clean buildPlugin runIde
```

---

## Visual Testing Tips

### Compare Themes Side-by-Side:

1. Maak een screenshot van UI met theme A
2. Switch naar theme B
3. Maak nog een screenshot
4. Gebruik `diff` tool om visueel te vergelijken

### Check Specific Components:

Open verschillende IDE windows:
- **Editor** - `Ctrl+Shift+N` voor file zoeken
- **Settings** - `Ctrl+Alt+S`
- **Find** - `Ctrl+Shift+F` voor global search
- **Terminal** - `Alt+F12`
- **Structure** - `Alt+7`

Elk component moet consistent themed zijn.

### Test in Different Lighting:

- Test dark themes in donkere omgeving
- Test light themes in lichte omgeving
- Check contrast en leesbaarheid

---

## Performance Check

Monitor plugin impact:

1. **Before plugin:**
   - Start IntelliJ
   - Note startup time
   - Check memory usage

2. **After plugin:**
   - Install plugin
   - Start IntelliJ
   - Compare startup time
   - Compare memory usage

**Expected:** Minimal impact (<100MB extra memory, <1s extra startup)

---

## Next Steps After Testing

1. **Test succesvol?**
   - ✅ Commit any final tweaks
   - ✅ Push to branch
   - ✅ Create/update pull request

2. **Bugs gevonden?**
   - 📝 Document in issue
   - 🔧 Fix bugs
   - 🔄 Rebuild en retest

3. **Ready voor release?**
   - 📦 Tag version
   - 🚀 Publish to JetBrains Marketplace
   - 📢 Announce release

---

## Useful Links

- **IntelliJ Platform SDK:** https://plugins.jetbrains.com/docs/intellij/
- **Theme Documentation:** https://plugins.jetbrains.com/docs/intellij/themes.html
- **Plugin DevKit:** https://plugins.jetbrains.com/docs/intellij/plugin-development.html

---

## Quick Reference

| Command | Purpose |
|---------|---------|
| `./gradlew buildPlugin` | Build ZIP bestand |
| `./gradlew runIde` | Test in sandbox IDE |
| `./gradlew createThemes` | Regenereer themes |
| `./gradlew clean` | Verwijder build artifacts |
| `./gradlew verifyPluginConfiguration` | Check plugin config |

**Plugin ZIP locatie:** `build/distributions/jetbrains-melly-theme-<version>.zip`

**Themes locatie:** `src/main/resources/themes/*.theme.json`

**Plugin descriptor:** `src/main/resources/META-INF/plugin.xml`
