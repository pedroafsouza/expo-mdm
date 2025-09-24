#!/usr/bin/env node

const { execSync } = require('child_process');
const process = require('process');

/**
 * Executes a shell command and streams its output.
 * @param {string} command The command to execute.
 */
function runCommand(command) {
  try {
    console.log(`\n> ${command}`);
    execSync(command, { stdio: 'inherit' });
  } catch (error) {
    console.error(`\n❌ Command failed: ${command}`);
    // The error from execSync already includes stderr, so we just exit.
    process.exit(1);
  }
}

function main() {
  // --- 1. Set Version Type (Default to patch) ---
  const versionType = process.argv[2] || 'patch';
  if (!['patch', 'minor', 'major', 'prepatch', 'preminor', 'premajor', 'prerelease'].includes(versionType)) {
    console.error(`❌ Invalid version type '${versionType}'. Must be one of: patch, minor, major, etc.`);
    process.exit(1);
  }
  console.log(`ℹ️  Version bump type is set to '${versionType}'.`);

  // --- 2. Check if logged into npm ---
  console.log('\nVerifying npm login status...');
  try {
    const user = execSync('npm whoami').toString().trim();
    console.log(`✅ Logged in as '${user}'.`);
  } catch (error) {
    console.error('❌ You are not logged into npm. Please run \'npm login\' first.');
    process.exit(1);
  }

  // --- 3. Perform a Dry Run ---
  console.log('\n📦 Performing a dry run to see what will be published...');
  runCommand('npm pack --dry-run');
  console.log('✅ Dry run successful.');

  // --- 4. Bump Version and Publish ---
  console.log(`\n🚀 Bumping version and publishing...`);

  // Bump the version in package.json and create a git tag
  runCommand(`npm version ${versionType}`);

  // Publish the package to the npm registry
  runCommand('npm publish');

  console.log('\n🎉 Successfully published!');
}

main();
