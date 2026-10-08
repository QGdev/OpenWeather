/*
 *  Copyright (c) 2019 - 2026
 *  QGdev - Quentin GOMES DOS REIS
 *
 *  This file is part of OpenWeather, released under the GNU General Public License v3 or later.
 */

/*
 * The sky of the page: one of the app's six condition skies. It comes from the URL fragment when it
 * names one, otherwise from the local time and an hourly seed. Nothing is stored on the device: the
 * choice lives in the address (#rain), which is never sent to the server.
 */
(function (root) {
	var SKIES = ['sun', 'cloud', 'rain', 'storm', 'snow', 'night'];

	/** A clear sky is night from 21:00 to 06:00, like isDaytime in the app. */
	function isDaytime(hour) {
		return hour >= 6 && hour < 21;
	}

	/** A deterministic number in [0, 1) for this hour: the sky holds for an hour, on every page. */
	function hourly(date) {
		var n = date.getFullYear() * 1000000 + (date.getMonth() + 1) * 10000 + date.getDate() * 100 + date.getHours();
		n = Math.imul(n ^ (n >>> 15), 2246822507);
		n = Math.imul(n ^ (n >>> 13), 3266489909);
		return ((n ^ (n >>> 16)) >>> 0) / 4294967296;
	}

	/** The weather of the hour; snow only from November to March. */
	function pickWeather(date) {
		var month = date.getMonth();
		var table = [['clear', 45], ['cloud', 30], ['rain', 20], ['storm', 3], ['snow', month >= 10 || month <= 2 ? 5 : 0]];
		var total = table.reduce(function (sum, row) { return sum + row[1]; }, 0);
		var r = hourly(date) * total;
		for (var i = 0; i < table.length; i++) {
			r -= table[i][1];
			if (r < 0) return table[i][0];
		}
		return 'clear';
	}

	function auto(date) {
		var weather = pickWeather(date);
		if (weather === 'clear') return isDaytime(date.getHours()) ? 'sun' : 'night';
		return weather;
	}

	function resolve(fragment, date) {
		var name = String(fragment).replace(/^#/, '');
		return SKIES.indexOf(name) >= 0 ? name : auto(date);
	}

	var api = { SKIES: SKIES, isDaytime: isDaytime, pickWeather: pickWeather, auto: auto, resolve: resolve };
	if (typeof module !== 'undefined') module.exports = api;
	if (typeof document === 'undefined') return;
	root.OWSky = api;

	var html = document.documentElement;

	/** Sets the sky, then brings the picker and the internal links in line with it. */
	function show(name) {
		html.setAttribute('data-sky', name);
		var chips = document.querySelectorAll('.sky-picker .chip');
		for (var i = 0; i < chips.length; i++) {
			chips[i].setAttribute('aria-pressed', String(chips[i].getAttribute('data-sky') === name));
		}
		var links = document.querySelectorAll('a[data-keep]');
		for (var j = 0; j < links.length; j++) links[j].hash = '#' + name;
	}

	function choose(name) {
		if (SKIES.indexOf(name) < 0) name = auto(new Date());
		history.replaceState(null, '', '#' + name);
		show(name);
	}

	// Before the first paint: only the attribute exists yet, the rest follows on DOMContentLoaded.
	var first = resolve(location.hash, new Date());
	html.setAttribute('data-sky', first);
	history.replaceState(null, '', '#' + first);

	document.addEventListener('DOMContentLoaded', function () {
		var picker = document.querySelector('.sky-picker');
		if (picker) {
			picker.hidden = false;
			picker.addEventListener('click', function (event) {
				var chip = event.target.closest('.chip');
				if (chip) choose(chip.getAttribute('data-sky'));
			});
		}
		show(html.getAttribute('data-sky'));
	});

	// A fragment typed by hand; an unknown one (an in-page anchor, garbage) leaves the sky alone.
	window.addEventListener('hashchange', function () {
		var name = location.hash.replace(/^#/, '');
		if (SKIES.indexOf(name) >= 0) show(name);
	});
})(typeof window !== 'undefined' ? window : this);
