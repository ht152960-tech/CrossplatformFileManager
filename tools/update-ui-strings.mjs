import fs from 'node:fs';
import path from 'node:path';

const root = 'D:/kotlinProject/Cross-platformFileManager';
const sourceFiles = [
  path.join(root, 'composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/App.kt'),
  path.join(root, 'composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/AppLocale.kt'),
  path.join(root, 'composeApp/src/commonMain/kotlin/com/example/cross_platformfilemanager/StartupScreens.kt'),
];

function decodeUnicodeEscapes(text) {
  return text
    .replace(/\\u([0-9a-fA-F]{4})/g, (_, hex) =>
      String.fromCharCode(Number.parseInt(hex, 16)),
    )
    .replace(/\\n/g, '\n')
    .replace(/\\r/g, '\r')
    .replace(/\\t/g, '\t')
    .replace(/\\"/g, '"')
    .replace(/\\\\/g, '\\');
}

function extractStringLiterals(text) {
  const values = [];
  const literalPattern = /"""([\s\S]*?)"""|"((?:[^"\\]|\\.)*)"/g;
  for (const match of text.matchAll(literalPattern)) {
    const raw = match[1] ?? match[2] ?? '';
    const decoded = decodeUnicodeEscapes(raw).trim();
    if (decoded) {
      values.push(decoded);
    }
  }
  return values;
}

const seen = new Set();
const ordered = [];

for (const file of sourceFiles) {
  const content = fs.readFileSync(file, 'utf8');
  for (const value of extractStringLiterals(content)) {
    if (!seen.has(value)) {
      seen.add(value);
      ordered.push(value);
    }
  }
}

const output = path.join(root, 'tools/ui-strings.txt');
fs.writeFileSync(output, `${ordered.join('\n')}\n`);
console.log(`wrote ${output} (${ordered.length} entries)`);
