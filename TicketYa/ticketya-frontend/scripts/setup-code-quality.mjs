import { execSync } from 'node:child_process';
import fs from 'node:fs';
import ts from 'typescript';

function run(command) {
  console.log(`\n> ${command}\n`);
  execSync(command, {
    stdio: 'inherit',
    shell: true
  });
}

function readJson(path) {
  const text = fs.readFileSync(path, 'utf8');

  if (path.startsWith('tsconfig')) {
    const result = ts.parseConfigFileTextToJson(path, text);

    if (result.error) {
      throw new Error(`No se pudo leer ${path}`);
    }

    return result.config;
  }

  return JSON.parse(text);
}

function writeJson(path, value) {
  fs.writeFileSync(path, `${JSON.stringify(value, null, 2)}\n`, 'utf8');
}

if (!fs.existsSync('package.json') || !fs.existsSync('angular.json')) {
  console.error('Ejecuta este script desde la raiz de ticketya-frontend');
  process.exit(1);
}

const originalPackage = readJson('package.json');

if (!originalPackage.devDependencies?.['angular-eslint']) {
  run('npx ng add angular-eslint@22.6.0 --skip-confirmation --defaults');
}

const packageJson = readJson('package.json');

packageJson.scripts = {
  ...packageJson.scripts,
  lint: 'ng lint',
  'lint:fix': 'ng lint --fix',
  format: 'prettier --write "src/**/*.{ts,html,scss}" "angular.json" "tsconfig*.json" "package.json"',
  'format:check': 'prettier --check "src/**/*.{ts,html,scss}" "angular.json" "tsconfig*.json" "package.json"',
  quality: 'npm run format:check && npm run lint && npm test -- --watch=false && npm run build'
};

writeJson('package.json', packageJson);

const tsconfig = readJson('tsconfig.json');
tsconfig.compilerOptions ??= {};
tsconfig.compilerOptions.strict = true;
writeJson('tsconfig.json', tsconfig);

const angularJson = readJson('angular.json');
const projectName = Object.keys(angularJson.projects ?? {})[0];

if (!projectName) {
  throw new Error('No encontramos el proyecto Angular');
}

const project = angularJson.projects[projectName];
const buildOptions = project.architect.build.options;

buildOptions.stylePreprocessorOptions ??= {};
buildOptions.stylePreprocessorOptions.sass ??= {};

const silenceDeprecations =
  buildOptions.stylePreprocessorOptions.sass.silenceDeprecations ?? [];

if (!silenceDeprecations.includes('import')) {
  silenceDeprecations.push('import');
}

buildOptions.stylePreprocessorOptions.sass.silenceDeprecations =
  silenceDeprecations;

buildOptions.allowedCommonJsDependencies ??= [];

if (!buildOptions.allowedCommonJsDependencies.includes('qrcode')) {
  buildOptions.allowedCommonJsDependencies.push('qrcode');
}

const productionBudgets =
  project.architect.build.configurations?.production?.budgets ?? [];

const initialBudget = productionBudgets.find(
  (budget) => budget.type === 'initial'
);

if (initialBudget) {
  initialBudget.maximumWarning = '550kB';
}

writeJson('angular.json', angularJson);

run('npx prettier --write "src/**/*.{ts,html,scss}" "angular.json" "tsconfig*.json" "package.json"');
