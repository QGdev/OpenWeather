const assert = require('assert');
const fs = require('fs');
const css = fs.readFileSync(__dirname + '/../assets/style.css', 'utf8');
const [light, dark] = css.split('/* dark skies */');
assert.ok(dark, 'marker "/* dark skies */" missing');

const lum = (hex) => {
	const c = [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16) / 255)
		.map((v) => (v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4)));
	return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
};
const ratio = (a, b) => { const [x, y] = [lum(a), lum(b)].sort((p, q) => q - p); return (x + 0.05) / (y + 0.05); };

const re = /\[data-sky=(\w+)\]\{--sky-start:(#[0-9A-Fa-f]{6});--sky-mid:(#[0-9A-Fa-f]{6})/g;
const text = { light: ['#0E1621', '#3B4756'], dark: ['#F5F8FB', '#C6CFD9'] };
let n = 0;
for (const [scheme, part] of [['light', light], ['dark', dark]]) {
	for (const m of part.matchAll(re)) {
		for (const fg of text[scheme]) for (const bg of [m[2], m[3]]) {
			assert.ok(ratio(fg, bg) >= 4.5, `${scheme} ${m[1]}: ${fg} on ${bg} = ${ratio(fg, bg).toFixed(2)}`);
		}
		n++;
	}
}
assert.strictEqual(n, 12, 'expected 6 skies x 2 schemes, found ' + n);
console.log('contrast.test.js ok');
