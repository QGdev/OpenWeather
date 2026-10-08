/*
 *  Copyright (c) 2019 - 2026
 *  QGdev - Quentin GOMES DOS REIS
 *
 *  This file is part of OpenWeather, released under the GNU General Public License v3 or later.
 */

/*
 * The feature showcase: the phone of the header leaves its place, follows the visitor down to the
 * showcase, then slides from side to side while the screenshots cross-fade and the text swaps sides,
 * driven by how far the visitor has scrolled. The phone is fixed on screen; it follows two empty
 * boxes: the phone of the header (until the first step) and .showcase__slot (afterwards), which
 * CSS moves from side to side. Without this script the section is a plain list beside a sticky
 * first screenshot, and the header keeps its own phone.
 */
(function () {
	var el = document.querySelector('.showcase');
	var heroPhone = document.querySelector('.hero .phone');
	if (!el || !heroPhone || !window.requestAnimationFrame) return;

	var stage = el.querySelector('.showcase__stage');
	var phone = el.querySelector('.showcase__phone');
	var screen = el.querySelector('.screen');
	var steps = el.querySelectorAll('.step');
	var n = steps.length;

	// The header's screenshot becomes the first image of the flying phone.
	var heroShot = heroPhone.querySelector('img').cloneNode(true);
	heroShot.removeAttribute('loading');
	screen.insertBefore(heroShot, screen.firstChild);
	var shots = screen.querySelectorAll('img');

	var slot = document.createElement('div');
	slot.className = 'showcase__slot';
	slot.setAttribute('aria-hidden', 'true');
	stage.appendChild(slot);

	var base = phone.offsetWidth;
	var last = -2;
	var until = 0;
	var queued = false;

	el.style.setProperty('--n', n);
	el.setAttribute('data-live', '');
	el.setAttribute('data-side', 'right');
	document.documentElement.setAttribute('data-fly', '');

	/** The step on screen: -1 before the showcase reaches the top of the page, n once it scrolls away. */
	function stepNow() {
		var box = el.getBoundingClientRect();
		if (box.top > 0) return -1;
		if (box.bottom < window.innerHeight - 2) return n;
		var span = box.height - window.innerHeight;
		return Math.min(n - 1, Math.floor(-box.top / span * n));
	}

	function show(i) {
		var side = Math.min(i, n - 1);
		el.setAttribute('data-side', side > 0 && side % 2 ? 'left' : 'right');
		for (var k = 0; k < n; k++) steps[k].toggleAttribute('data-on', k === i);
		var shown = Math.min(i, n - 1) + 1;
		for (var j = 0; j < shots.length; j++) {
			shots[j].toggleAttribute('data-on', j === shown);
			shots[j].setAttribute('aria-hidden', String(j !== shown));
		}
	}

	/** Puts the phone between its place in the header and its place in the stage. */
	function place() {
		var from = heroPhone.getBoundingClientRect();
		var to = slot.getBoundingClientRect();
		var stuck = stage.getBoundingClientRect().top;
		var toTop = to.top - Math.max(stuck, 0);
		var travel = from.top + window.scrollY - toTop;
		var e;
		var y;
		if (travel > 0) {
			// The phone scrolls with the page, then stops where the stage holds it.
			e = Math.min(1, window.scrollY / travel);
			y = Math.max(from.top, toTop);
		} else {
			// The stage's place is below the header's: the phone descends to it.
			var p = Math.min(1, window.scrollY / (0.7 * window.innerHeight));
			e = p * p * (3 - 2 * p);
			y = from.top + (toTop - from.top) * e;
		}
		var x = from.left + (to.left - from.left) * e;
		var w = from.width + (to.width - from.width) * e;
		phone.style.transform = 'translate3d(' + x + 'px,' + y + 'px,0) scale(' + w / base + ')';
	}

	function frame() {
		queued = false;
		var i = stepNow();
		if (i !== last) {
			last = i;
			show(i);
			until = performance.now() + 1000;
		}
		place();
		if (performance.now() < until) queue();
	}

	function queue() {
		if (!queued) {
			queued = true;
			window.requestAnimationFrame(frame);
		}
	}

	window.addEventListener('scroll', queue, { passive: true });
	window.addEventListener('resize', queue);
	frame();
})();
