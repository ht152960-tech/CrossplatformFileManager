import fs from 'node:fs';
import path from 'node:path';
import subsetFont from 'subset-font';

const root = 'D:/kotlinProject/Cross-platformFileManager';
const input = path.join(root, 'composeApp/src/commonMain/composeResources/font/noto_sans_sc.ttf');
const output = path.join(root, 'composeApp/src/commonMain/composeResources/font/noto_sans_sc_ui.woff2');

const ascii = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ .,:;!?()[]{}<>+-*/=_#%&@|~`"\'\\\n\t';
const uiStrings = fs.readFileSync(path.join(root, 'tools/ui-strings.txt'), 'utf8');
const text = `${ascii}\n${uiStrings}`;

const subset = await subsetFont(fs.readFileSync(input), text, { targetFormat: 'woff2' });
fs.writeFileSync(output, subset);
console.log(`wrote ${output} (${subset.length} bytes)`);
