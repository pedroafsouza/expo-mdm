#!/bin/bash

# A script to safely publish an npm package.
# It performs a dry run, asks for confirmation, bumps the version, and then publishes.
# Defaults to a 'patch' version bump if no argument is given.

# Exit immediately if a command exits with a non-zero status.
set -e

# --- 1. Set Version Type (Default to patch) ---
# If the first argument is empty, default to 'patch'. Otherwise, use the argument.
VERSION_TYPE=${1:-patch}

echo "ℹ️ Version bump type is set to '$VERSION_TYPE'."
echo

# --- 2. Check if logged into npm ---
echo "Verifying npm login status..."
if ! npm whoami > /dev/null; then
  echo "❌ You are not logged into npm. Please run 'npm login' first."
  exit 1
fi
echo "✅ Logged in as '$(npm whoami)'."
echo

# --- 3. Perform a Dry Run ---
echo "📦 Performing a dry run to see what will be published..."
# We wrap this command in an if statement to provide a better error message
# instead of letting 'set -e' exit silently.
if ! npm pack --dry-run; then
    echo
    echo "❌ 'npm pack' failed. The script cannot continue."
    echo "👉 Please check for errors above. Common causes are an invalid 'package.json' or running the script from the wrong directory."
    exit 1
fi
echo


# --- 5. Bump Version and Publish ---
echo "🚀 Bumping version and publishing..."

# Bump the version in package.json and create a git tag
npm version $VERSION_TYPE

# Publish the package to the npm registry
npm publish

echo "🎉 Successfully published!"