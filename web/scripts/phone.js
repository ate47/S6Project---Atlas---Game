let log = console.log;
let canvas;
let IMAGE_CONTROLER_LEFT;
let IMAGE_CONTROLER_RIGHT;

let left;
let right;

const username = function() {
    let search = window.location.search;
	if (search.startsWith("?username=")) {
		return search.substr("?username=".length);
	} else {
		window.location.pathname="/index.html";
		return false;
	}
}();

log("Pseudo: " + username);

// Create WebSocket connection.
const packetHandler = new PacketHandler('ws://' + window.location.host + '/game');

packetHandler.openWebSocket(function() {
	packetHandler.sendPacket(new PacketC00ConnectPlayer(username));
});

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
		this.d2 = 0;
		this.idf = idf;
		this.vectorX = 0;
		this.vectorY = 0;
		this.radius = radius;
		this.img = img;
		this.drawControler = false;
		this.color = color(100, 100, 255, 50);
	}

	/**
	 * update the presspoint vector with a new (x,y) touch location
	 * @param {number} inputX x client input
	 * @param {number} inputY y client input
	 */
	updateVector(inputX, inputY) {
		this.vectorX = inputX - this.x;
		this.vectorY = inputY - this.y;
		
		// maximize the move vector
		this.d2 = this.vectorX * this.vectorX + this.vectorY * this.vectorY;
		let radius2 = this.radius * this.radius / 4;
		if (this.d2 > radius2) {
			let d = Math.sqrt(this.d2);	
			this.vectorX = this.radius / 2 * this.vectorX / d;
			this.vectorY = this.radius / 2 * this.vectorY / d;
		}
	}
	/**
	 * update the identifier of this controller
	 * @param {number} idf 
	 */
	updateIDF(idf) {
		this.idf = idf;
		this.vectorX = 0;
		this.vectorY = 0;
		this.drawControler = true;
	}
	/**
	 * set a new location for the controller
	 * @param {number} x the new x location
	 * @param {number} y the new y location
	 */
	updateLocation(x, y, idf, radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.updateIDF(idf);
	}

	getMoveX() {
		return this.vectorX / this.radius;
	}
	getMoveY() {
		return this.vectorY / this.radius;
	}

	/**
	 * end the controller input
	 */
	end() {
		this.vectorX = 0;
		this.vectorY = 0;
		this.idf = -1;
		this.d2 = 0;
		this.drawControler = false;
	}

	/**
	 * draw the press point
	 */
	draw() {
		if (!this.drawControler) {
			if (this.color._getAlpha() > 50)
				this.color.setAlpha(this.color._getAlpha() - 10);
			else if (this.color._getAlpha() < 50)
				this.color.setAlpha(50);
		} else if (this.color._getAlpha() < 100) {
			this.color.setAlpha(this.color._getAlpha() + 10);
		} else if (this.color._getAlpha() > 100)
			this.color.setAlpha(100);
		// draw the controller
		fill(this.color);
		circle(this.x, this.y, this.radius * 3 / 2);
		circle(this.x, this.y, this.radius / 2);
		// draw the stick
		circle(this.x + this.vectorX , this.y + this.vectorY, this.radius * 4 / 5);
		tint(this.color);
		image(this.img, this.x - this.radius / 4, this.y - this.radius / 4, this.radius / 2, this.radius / 2);
		noTint();
	}
}

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);
	IMAGE_CONTROLER_LEFT = loadImage("images/controler_view.png");
	IMAGE_CONTROLER_RIGHT = loadImage("images/controler_weapon.png");

	left = new PressPoint(windowWidth / 6, windowHeight * 3 / 5, -1, windowWidth / 8, IMAGE_CONTROLER_LEFT);
	right = new PressPoint(windowWidth * 5 / 6, windowHeight * 3 / 5, -1, windowWidth / 8, IMAGE_CONTROLER_RIGHT);

	frameRate(24);
	setInterval(tick, 20);
}

function tick() {
	// left.getMoveX() * 5;
	// left.getMoveY() * 5;
	// move

	let r2 = right.radius * right.radius;
	if (right.d2 > r2 / 4) {
		// shot
	}
}

function draw() {
	fill(color(220, 220, 220));
	rect(0, 0, windowWidth, windowHeight);
	noStroke();
	
	//fill(color(255, 0, 0));
	//text("LEFT: (x: " + left.vectorX + ", y: " + left.vectorX + ", norme: "+(Math.sqrt(left.vectorX * left.vectorX + left.vectorY * left.vectorY))+")", 0, textSize());
	//text("RIGHT: (x: " + right.vectorX + ", y: " + right.vectorX + ", norme: "+(Math.sqrt(right.vectorX * right.vectorX + right.vectorY * right.vectorY))+")", 0, 2 * textSize() + 2);

	left.draw();
	right.draw();
}

function touchStarted(ev) {
	if (ev instanceof TouchEvent) {
		let touch;
		let list = ev.changedTouches;
		for (let i = 0; i < list.length; i++) {
			touch = list[i];
			if (touch.clientX < windowWidth / 2) { // left
				left.updateIDF(touch.identifier);
			} else { // right
				right.updateIDF(touch.identifier);
			}
		}
	}
	return false;
}
function touchMoved(ev) {
	if (ev instanceof TouchEvent) {
		let touch;
		let list = ev.changedTouches;
		for (let i = 0; i < list.length; i++) {
			touch = list[i];
			if (left.idf == touch.identifier) {
				left.updateVector(touch.clientX, touch.clientY);
			}
			if (right.idf == touch.identifier) {
				right.updateVector(touch.clientX, touch.clientY);
			}
		}
	}
	return false;
}
function touchEnded(ev) {
	if (ev instanceof TouchEvent) {
		let touch;
		let list = ev.changedTouches;
		for (let i = 0; i < list.length; i++) {
			touch = list[i];
			if (left.idf == touch.identifier) {
				left.end();
			} else if (right.idf == touch.identifier) {
				right.end();
			}
		}
	}
	return false;
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);

	left.updateLocation(windowWidth / 6, windowHeight * 3 / 5, -1, windowWidth / 8);
	right.updateLocation(windowWidth * 5 / 6, windowHeight * 3 / 5, -1, windowWidth / 8);
}