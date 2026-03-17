# Deployment Guide

This document explains how to configure GitHub Actions for secure PyPI publishing using OpenID Connect (OIDC).

## Overview

The CI/CD pipeline uses **Trusted Publishing** (OIDC) instead of long-lived API tokens. This is the recommended secure method for publishing to PyPI.

## GitHub Environment Setup

You need to create two environments in your GitHub repository:

1. **`testpypi`** - For TestPyPI (staging)
2. **`pypi`** - For production PyPI

### Step 1: Create Environments

1. Go to **Settings** → **Environments** → **New environment**
2. Create `testpypi` environment
3. Create `pypi` environment

### Step 2: Configure Environment Protection Rules

#### For `pypi` environment (Production):

1. **Required reviewers**: Add maintainers who must approve production releases
2. **Wait timer** (optional): Set a delay before deployment
3. **Deployment branches**: Restrict to `main` branch and tags

#### For `testpypi` environment:

1. **Deployment branches**: Allow `main` and tags starting with `v`

### Step 3: Configure PyPI Trusted Publishing

#### TestPyPI Setup:

1. Go to https://test.pypi.org/manage/account/publishing/
2. Click **Add a new pending publisher**
3. Fill in:
   - **PyPI Project Name**: `swifttrack`
   - **Owner**: Your GitHub org/username (e.g., `swifttrack`)
   - **Repository name**: `swifttrack-python`
   - **Workflow name**: `ci-cd.yml`
   - **Environment name**: `testpypi`

#### PyPI Production Setup:

1. Go to https://pypi.org/manage/account/publishing/
2. Click **Add a new pending publisher**
3. Fill in:
   - **PyPI Project Name**: `swifttrack`
   - **Owner**: Your GitHub org/username
   - **Repository name**: `swifttrack-python`
   - **Workflow name**: `ci-cd.yml`
   - **Environment name**: `pypi`

## Workflow Triggers

| Event | Action |
|-------|--------|
| Push to `main` | Run CI (lint, test, build) |
| Pull request to `main` | Run CI |
| Tag push (`v*`) | Run CI + publish to TestPyPI |
| GitHub release published | Run CI + TestPyPI + PyPI |

## Security Features

### OIDC Token Exchange

```yaml
permissions:
  id-token: write  # Required for OIDC
  contents: read
```

The workflow uses short-lived tokens instead of stored secrets.

### Environment Isolation

- TestPyPI publishes happen automatically on tags
- PyPI publishes require:
  1. Successful TestPyPI publish
  2. Manual approval via environment protection
  3. Release event trigger

### Branch Protection

Only the `main` branch can trigger publishes. No other branches can publish packages.

## Manual Publishing

To manually trigger a publish:

1. Create and push a tag:
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

2. Or create a GitHub release:
   - Go to Releases → Draft new release
   - Tag version: `v0.1.0`
   - Target: `main`
   - Publish release

## Troubleshooting

### "Permission denied" errors

Ensure the GitHub environment name matches exactly what's configured in PyPI.

### "No matching issuer found"

Verify the repository path and workflow name in PyPI configuration.

### Workflow not triggered

Check that:
- The tag starts with `v` (e.g., `v0.1.0`)
- You're pushing to the correct branch (`main`)

## References

- [GitHub OIDC Documentation](https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-pypi)
- [PyPI Trusted Publishing](https://docs.pypi.org/trusted-publishers/)
- [GitHub Environments](https://docs.github.com/en/actions/deployment/targeting-different-environments/using-environments-for-deployment)
