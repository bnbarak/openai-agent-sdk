#!/bin/bash
# Script to sync version across all documentation files from Maven Central

set -e

# Query Maven Central for latest non-SNAPSHOT version
echo "Querying Maven Central for latest release..."
LATEST_RELEASE=$(curl -s "https://search.maven.org/solrsearch/select?q=g:ai.acolite+AND+a:openai-agent-sdk&rows=1&wt=json" | jq -r '.response.docs[0].latestVersion // empty')

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

# Clean up backup files
rm -f README.md.bak mkdocs.yml.bak

echo "✅ Version synced to $VERSION in:"
echo "   - README.md (Maven Central badge and dependency)"
echo "   - mkdocs.yml (will be used in docs site)"
echo ""
echo "Note: Docs markdown files use {{ config.extra.sdk_version }} variable"
echo "      which will be automatically replaced during docs build"
