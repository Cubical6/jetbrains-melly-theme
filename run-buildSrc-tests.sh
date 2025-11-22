#!/bin/bash
# Run buildSrc tests specifically

echo "Running buildSrc tests..."
cd buildSrc
../gradlew test --tests ITermColorSchemeTest
