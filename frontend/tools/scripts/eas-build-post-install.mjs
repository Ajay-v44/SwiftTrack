/**
 * This script is used to patch the '@nx/expo' package to work with EAS Build.
 * It is run as a eas-build-post-install script in the 'package.json' of expo app.
 * It is executed as 'node tools/scripts/eas-build-post-install.mjs <workspace root> <project root>'
 * It creates a symlink from the project's node_modules to the workspace's node_modules
 * and stages Firebase Android config into the native app for bare workflow builds.
 */

import { copyFileSync, existsSync, mkdirSync, writeFileSync, symlink } from 'fs';
import { dirname, join } from 'path';

const [workspaceRoot, projectRoot] = process.argv.slice(2);
const projectNodeModules = join(projectRoot, 'node_modules');
const workspaceNodeModules = join(workspaceRoot, 'node_modules');

if (!existsSync(workspaceNodeModules)) {
  symlink(projectNodeModules, workspaceNodeModules, 'dir', (err) => {
    if (err) {
      console.log(err);
    } else {
      console.log('Symlink created');
    }
  });
} else {
  console.log('Symlink already exists');
}

const googleServicesSource =
  process.env.GOOGLE_SERVICES_JSON || join(projectRoot, 'google-services.json');
const googleServicesTarget = join(projectRoot, 'android/app/google-services.json');

mkdirSync(dirname(googleServicesTarget), { recursive: true });

if (process.env.GOOGLE_SERVICES_BASE64) {
  writeFileSync(
    googleServicesTarget,
    Buffer.from(process.env.GOOGLE_SERVICES_BASE64, 'base64').toString('utf-8')
  );
  console.log(`Created google-services.json from base64 secret at ${googleServicesTarget}`);
} else if (existsSync(googleServicesSource)) {
  copyFileSync(googleServicesSource, googleServicesTarget);
  console.log(`Copied google-services.json to ${googleServicesTarget}`);
} else {
  console.log(
    'google-services.json not found. Set GOOGLE_SERVICES_BASE64 in EAS or provide apps/mobileapp/google-services.json locally.',
  );
}
