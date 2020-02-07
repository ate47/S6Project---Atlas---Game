let log = console.log;
let canvas;
let FONT;
let IMAGE_CONTROLER_LEFT;
let IMAGE_CONTROLER_RIGHT;
let CONTROLER_COLOR;

let left = false;
let right = false;

class PressPoint {
	/**
	 * build a press point
	 * @param {number} x origin x
	 * @param {number} y origin y
	 * @param {number} idf touch identifier
	 * @param {number} radius the radius of the controller to draw
	 * @param {*} img the image object
	 */
	constructor(x, y, idf, radius, img) {
		this.x = x;
		this.y = y;
		this.idf = idf;
		this.vectorX = 0;
		this.vectorY = 0;
		this.radius = radius;
		this.img = img;
	}

	/**
	 * update the presspoint vector with a new (x,y) touch location
	 * @param {number} inputX x client input
	 * @param {number} inputY y client input
	 */
	updateVector(inputX, inputY) {
		this.vectorX = inputX - this.x;
		this.vectorY = inputY - this.y;
	}

	/**
	 * draw the press point
	 */
	draw() {
		fill(CONTROLER_COLOR);
		circle(this.x, this.y, this.radius * 3 / 2);
		circle(this.x, this.y, this.radius / 2);
		let d2 = this.vectorX * this.vectorX + this.vectorY * this.vectorY;
		let radius2 = this.radius * this.radius / 4;
		if (d2 > radius2) {
			let d = Math.sqrt(d2);	
			this.vectorX = this.radius / 2 * this.vectorX / d;
			this.vectorY = this.radius / 2 * this.vectorY / d;
		}
		circle(this.x + this.vectorX , this.y + this.vectorY, this.radius * 4 / 5);
		tint(CONTROLER_COLOR);
		image(this.img, this.x - this.radius / 4, this.y - this.radius / 4, this.radius / 2, this.radius / 2);
		noTint();
	}
}

/**
 * A class that represent a font
 */
class Font {
	/**
	 * Load a font image
	 * @param {string} img the font image
	 * @param {number} fontSize the font size when writing
	 * @param {number} dimension the dimension in the font image of a cell
	 */
	constructor(img, fontSize = 25, dimension = 16) {
		this.img = loadImage(img);
		this.fontSize = fontSize;
		this.dimension = dimension;
	}

	/**
	 * Draw a centered text on screen (without new line)
	 * @param {number} x the center x location to draw
	 * @param {number} y y location to draw
	 * @param {string} text the text to draw
	 * @param {number} color the text color
	 */
	drawCentered(x, y, text, color = 0xffffff) {
		this.drawText(x - text.length * this.fontSize / 2, y, text, color);
	}

	/**
	 * 
	 * @param {number} x 
	 * @param {number} y 
	 * @param {number} charCode 
	 */
	drawChar(x, y, charCode) {
		if (charCode < 0 || charCode > 128) {
			charCode = 0;
		}
		let rx = (charCode % this.dimension) * this.dimension;
		let ry = Math.floor(charCode / this.dimension) * this.dimension;
		image(this.img, x, y, this.fontSize, this.fontSize, rx, ry, this.dimension, this.dimension);
	}

	/**
	 * Draw a text on screen
	 * @param {number} x x location to draw
	 * @param {number} y y location to draw
	 * @param {string} text the text to draw
	 * @param {number} color the text color
	 */
	drawText(x, y, text, color = 0xffffff) {
		let rx = x;
		let ry = y;
		for (let i = 0; i < text.length; i++) {
			if (text[i] == "\n") {
				ry += this.fontSize;
				rx = x;
				continue;
			}
			tint(0, 0, 0);
			this.drawChar(rx + 1, ry + 1, text.charCodeAt(i));
			tint(color & 0xFF0000, color & 0x00FF00, color & 0x0000FF);
			this.drawChar(rx, ry, text.charCodeAt(i));
			rx += this.fontSize - 4;
		}
		noTint(); // Disable tint
	}

	/**
	 * set the font size and return this font
	 * @param {number} fontSize the new size
	 */
	size(fontSize = 25) {
		this.fontSize = fontSize;
		return this;
	}
}
function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);
	FONT = new Font('images/font.png', 20);
	IMAGE_CONTROLER_LEFT = loadImage("images/controler_view.png");
	IMAGE_CONTROLER_RIGHT = loadImage("images/controler_weapon.png");
	CONTROLER_COLOR = color(255, 100, 100, 100);

	frameRate(60);
	setInterval(tick, 20);
}

function tick() {
	
}

function draw() {
	fill(color(255, 255, 255));
	rect(0, 0, windowWidth, windowHeight);
	noStroke();
	if (left !== false)
		left.draw();
	if (right !== false)
		right.draw();
}

function touchStarted(ev) {
	if (ev instanceof TouchEvent) {
		let touch;
		let list = ev.changedTouches;
		for (let i = 0; i < list.length; i++) {
			touch = list[i];
			if (touch.clientX < windowWidth / 2) { // left
				left = new PressPoint(touch.clientX, touch.clientY, touch.identifier, windowWidth / 8, IMAGE_CONTROLER_LEFT);
			} else { // right
				right = new PressPoint(touch.clientX, touch.clientY, touch.identifier, windowWidth / 8, IMAGE_CONTROLER_RIGHT);
			}
		}
	}
}
function touchMoved(ev) {
	if (ev instanceof TouchEvent) {
		let touch;
		let list = ev.changedTouches;
		for (let i = 0; i < list.length; i++) {
			touch = list[i];
			if (left !== false && left.idf == touch.identifier) {
				left.updateVector(touch.clientX, touch.clientY);
			}
			if (right !== false && right.idf == touch.identifier) {
				right.updateVector(touch.clientX, touch.clientY);
			}
		}
	}
}
function touchEnded(ev) {
	if (ev instanceof TouchEvent) {
		let touch;
		let list = ev.changedTouches;
		for (let i = 0; i < list.length; i++) {
			touch = list[i];
			if (left !== false && left.idf == touch.identifier) {
				left = false;
			}
			if (right !== false && right.idf == touch.identifier) {
				right = false;
			}
		}
	}
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);
}