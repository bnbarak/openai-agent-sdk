# Publishing to Maven Central

This guide explains how to publish the OpenAI Agent SDK to Maven Central.

## Prerequisites

Before you can publish, you need to set up the following:

### 1. Sonatype OSSRH Account

1. Create an account at [Sonatype JIRA](https://issues.sonatype.org/)
2. Create a ticket to claim the `ai.acolite` groupId (if not already done)
3. Wait for approval from Sonatype staff

### 2. GPG Key for Signing

Maven Central requires all artifacts to be signed with GPG:

```bash
# Generate a GPG key
gpg --gen-key

# List your keys
gpg --list-secret-keys --keyid-format LONG

# Export your private key (use the key ID from previous command)
gpg --armor --export-secret-keys YOUR_KEY_ID
```

### 3. GitHub Secrets

Add the following secrets to your GitHub repository (Settings → Secrets and variables → Actions):

| Secret Name | Description |
|-------------|-------------|
| `OSSRH_USERNAME` | Your Sonatype JIRA username |
| `OSSRH_PASSWORD` | Your Sonatype JIRA password or token |
| `GPG_PRIVATE_KEY` | Your GPG private key (entire output from export command) |
| `GPG_PASSPHRASE` | The passphrase for your GPG key |

## Publishing Process

### Option 1: Manual Workflow Trigger (Recommended)

**Step 1: Bump the version in pom.xml**

```bash
# Update version from 0.1.0-SNAPSHOT to 0.1.0
mvn versions:set -DnewVersion=0.1.0 -DgenerateBackupPoms=false

# Commit the version bump
git add pom.xml
git commit -m "Bump version to 0.1.0 for release"
git push origin main
```

**Step 2: Trigger the publish workflow**

1. Go to **Actions** → **Publish to Maven Central**
2. Click **Run workflow**
3. Choose whether to do a dry run (recommended first time)
4. Click **Run workflow**

The workflow will:
- ✅ Extract version from `pom.xml`
- ✅ Validate it's not a SNAPSHOT
- ✅ Run code quality checks (Spotless)
- ✅ Run unit tests
- ✅ Build and sign artifacts
- ✅ Deploy to Maven Central (OSSRH)
- ✅ Create a Git tag for the release

**Step 3: Bump to next SNAPSHOT version**

After publishing, bump to the next development version:

```bash
# Bump to next SNAPSHOT
mvn versions:set -DnewVersion=0.2.0-SNAPSHOT -DgenerateBackupPoms=false

# Commit
git add pom.xml
git commit -m "Bump version to 0.2.0-SNAPSHOT for development"
git push origin main
```

### Option 2: Local Publishing

```bash
# Set version
mvn versions:set -DnewVersion=0.1.0

# Deploy to Maven Central
mvn clean deploy -P release

# Create and push tag
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0
```

## Dry Run Testing

Before publishing for real, you should test with a dry run:

1. Bump the version to a release version (e.g., `0.1.0`) in `pom.xml`
2. Commit and push to main
3. Go to **Actions** → **Publish to Maven Central**
4. Set **Dry run** to `true`
5. Run the workflow

This will build everything and show what would be published without actually deploying. If successful, run again with dry run off to publish for real.

## After Publishing

1. **Bump to next SNAPSHOT** - Update `pom.xml` to next development version (see Step 3 above)
2. **Wait for sync** - It can take 2-4 hours for artifacts to sync to Maven Central
3. **Verify on Maven Central** - Check https://search.maven.org/ for your artifact
4. **Update documentation** - Update version numbers in README and docs if needed
5. **Create GitHub Release** - Create a release with release notes on GitHub
6. **Announce** - Share the release in relevant channels

## Troubleshooting

### GPG Signing Fails

- Ensure your GPG key is not expired
- Verify the passphrase is correct
- Make sure the private key secret includes the full key (including `-----BEGIN PGP PRIVATE KEY BLOCK-----`)

### Authentication Fails

- Verify your Sonatype credentials are correct
- If using a token, ensure it has the correct permissions
- Check that the `serverId` in `pom.xml` matches the GitHub secret name

### Artifacts Not Appearing

- Check the workflow logs for errors
- Verify the version is correct (not a snapshot)
- Wait 2-4 hours for Maven Central sync
- Check https://s01.oss.sonatype.org/ for staging status

## Version Guidelines

- **Snapshots**: `0.1.0-SNAPSHOT` - Development versions
- **Releases**: `0.1.0` - Stable releases
- Follow [Semantic Versioning](https://semver.org/):
  - MAJOR version for incompatible API changes
  - MINOR version for backwards-compatible functionality
  - PATCH version for backwards-compatible bug fixes

## Security

- Never commit secrets to the repository
- Keep your GPG key secure and backed up
- Rotate credentials regularly
- Use GitHub's secret scanning to detect accidental commits
