const assert = require('assert');
const { SKIES, isDaytime, pickWeather, auto, resolve } = require('../assets/sky.js');

// hour boundaries
assert.strictEqual(isDaytime(5), false);
assert.strictEqual(isDaytime(6), true);
assert.strictEqual(isDaytime(20), true);
assert.strictEqual(isDaytime(21), false);

// same hour, same sky; clear turns to night after dark
const noon = new Date(2026, 6, 14, 12, 30);
assert.strictEqual(auto(noon), auto(new Date(2026, 6, 14, 12, 59)));
const seen = new Set();
for (let h = 0; h < 24; h++) seen.add(auto(new Date(2026, 6, 14, h)));
assert.ok([...seen].every((s) => SKIES.includes(s)));

// a clear sky is night by night and sun by day
for (let d = 1; d <= 28; d++) {
  for (let h = 0; h < 24; h++) {
    const date = new Date(2026, 5, d, h);
    if (pickWeather(date) === 'clear') assert.strictEqual(auto(date), isDaytime(h) ? 'sun' : 'night');
  }
}

// snow only from November to March; weights give a plausible spread
const counts = { clear: 0, cloud: 0, rain: 0, storm: 0, snow: 0 };
for (let m = 0; m < 12; m++) {
  for (let d = 1; d <= 28; d++) {
    for (let h = 0; h < 24; h++) {
      const w = pickWeather(new Date(2026, m, d, h));
      counts[w]++;
      if (w === 'snow') assert.ok(m >= 10 || m <= 2, 'snow in month ' + m);
    }
  }
}
assert.ok(counts.clear > 0 && counts.cloud > 0 && counts.rain > 0 && counts.snow > 0);
assert.ok(counts.storm < 0.08 * 12 * 28 * 24);

// the fragment wins when it names a sky, and is ignored otherwise
assert.strictEqual(resolve('#rain', noon), 'rain');
assert.strictEqual(resolve('rain', noon), 'rain');
for (const bad of ['', '#', '#features', '#__proto__', '#constructor', '#<script>', '#RAIN', '#rain/x']) {
  assert.strictEqual(resolve(bad, noon), auto(noon), 'fragment ' + bad);
}
console.log('sky.test.js ok');
