#!/bin/bash
# Script to sync version across all documentation files from Maven Central

set -e

# Query Maven Central metadata for latest release version
echo "Querying Maven Central for latest release..."
LATEST_RELEASE=$(curl -s "https://repo1.maven.org/maven2/ai/acolite/openai-agent-sdk/maven-metadata.xml" | sed -n 's/.*<release>\(.*\)<\/release>.*/\1/p')

if [ -z "$LATEST_RELEASE" ] || [[ "$LATEST_RELEASE" == *"SNAPSHOT"* ]]; then
    # Fallback to pom.xml version if no release found or if it's a SNAPSHOT
    VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)
    if [ -z "$VERSION" ]; then
        echo "Error: Could not extract version from pom.xml"
        exit 1
    fi
    echo "No stable release found on Maven Central, using pom.xml version: $VERSION"
else
    VERSION=$LATEST_RELEASE
    echo "Using latest release from Maven Central: $VERSION"
fi

echo "Syncing version: $VERSION"

# Update README.md
sed -i.bak "s/maven--central-[0-9A-Z._-]*-blue/maven--central-${VERSION}-blue/" README.md
sed -i.bak "s/<version>[0-9A-Z._-]*<\/version>/<version>${VERSION}<\/version>/" README.md

# Update mkdocs.yml
sed -i.bak "s/sdk_version: \"[^\"]*\"/sdk_version: \"${VERSION}\"/" mkdocs.yml

# Update markdown files (replace template variables with actual version)
find docs -name "*.md" -type f -exec sed -i.bak "s/{{ config\\.extra\\.sdk_version }}/${VERSION}/g" {} +

# Clean up backup files
rm -f README.md.bak mkdocs.yml.bak
find docs -name "*.md.bak" -type f -delete

echo "✅ Version synced to $VERSION in:"
echo "   - README.md (Maven Central badge and dependency)"
echo "   - mkdocs.yml (sdk_version variable)"
echo "   - docs/**/*.md (replaced template variables)"
