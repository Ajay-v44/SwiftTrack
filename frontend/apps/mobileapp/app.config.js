const fs = require('fs');
const path = require('path');
const appJson = require('./app.json');

const projectRoot = __dirname;
const localGoogleServicesPath = path.join(projectRoot, 'google-services.json');
const easGoogleServicesPath = process.env.GOOGLE_SERVICES_JSON;

const resolvedGoogleServicesPath =
  easGoogleServicesPath ||
  (fs.existsSync(localGoogleServicesPath) ? localGoogleServicesPath : undefined);

module.exports = () => ({
  ...appJson,
  expo: {
    ...appJson.expo,
    android: {
      ...appJson.expo.android,
      googleServicesFile: resolvedGoogleServicesPath,
    },
  },
});
