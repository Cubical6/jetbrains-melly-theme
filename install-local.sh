#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  JetBrains Melly Theme - Local Install${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Build plugin
echo -e "${YELLOW}🔨 Building plugin...${NC}"
./gradlew clean buildPlugin

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Build failed!${NC}"
  exit 1
fi

echo -e "${GREEN}✅ Build successful!${NC}"
echo ""

# Detect IntelliJ IDEA directory
echo -e "${YELLOW}🔍 Searching for IntelliJ IDEA installation...${NC}"

# Common locations
IDEA_DIRS=(
  "$HOME/.local/share/JetBrains/IntelliJIdea"*
  "$HOME/Library/Application Support/JetBrains/IntelliJIdea"*
  "$HOME/.config/JetBrains/IntelliJIdea"*
)

IDEA_PLUGINS=""
for dir in "${IDEA_DIRS[@]}"; do
  if [ -d "$dir/plugins" ]; then
    IDEA_PLUGINS="$dir/plugins"
    echo -e "${GREEN}✅ Found IntelliJ at: $dir${NC}"
    break
  fi
done

if [ -z "$IDEA_PLUGINS" ]; then
  echo -e "${RED}❌ Could not find IntelliJ IDEA plugins directory${NC}"
  echo -e "${YELLOW}Please install manually:${NC}"
  echo "  1. In IntelliJ: Settings → Plugins → ⚙️ → Install Plugin from Disk"
  echo "  2. Select: $(pwd)/build/distributions/jetbrains-melly-theme-*.zip"
  exit 1
fi

# Remove old version
if [ -d "$IDEA_PLUGINS/jetbrains-melly-theme" ]; then
  echo -e "${YELLOW}🗑️  Removing old plugin version...${NC}"
  rm -rf "$IDEA_PLUGINS/jetbrains-melly-theme"
fi

# Install new version
echo -e "${YELLOW}📦 Installing new plugin...${NC}"
unzip -q build/distributions/jetbrains-melly-theme-*.zip -d "$IDEA_PLUGINS/"

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Plugin installed successfully!${NC}"
  echo ""
  echo -e "${BLUE}========================================${NC}"
  echo -e "${YELLOW}⚠️  Please restart IntelliJ IDEA to activate the plugin${NC}"
  echo ""
  echo "To activate a theme:"
  echo "  1. Settings → Appearance & Behavior → Appearance"
  echo "  2. Choose a theme starting with 'wt-'"
  echo "  3. Apply and enjoy!"
  echo -e "${BLUE}========================================${NC}"
else
  echo -e "${RED}❌ Installation failed!${NC}"
  exit 1
fi
