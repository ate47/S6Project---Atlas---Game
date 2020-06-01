let log = console.log;
let canvas;
let oldScale = 1;
const fps = 45;
const tps = 20;
let IMAGE_CONTROLER_LEFT;
let IMAGE_CONTROLER_RIGHT;
let IMAGE_DEAD;
let IMAGE_ROTATE;
let IMAGE_TUTO;
let IMAGE_MAP;
let IMAGE_PLAYER_S;
let IMAGE_PLAYER_I;
let IMAGE_CRATE;


let playerMap = [];
let shoots = [];
let crates = [];
let playerSizeX = 0.005;
let playerSizeY = 0.005;
let isSetup = false;

let playerUUID = false;

let left = false;
let right = false;

let handle = false;
let playerData = {
	health: 100,
	ammos: 0,
	type: PLAYER_TYPE_SURVIVOR,
	survivorSortId : 0,
	infectionSortId : 0,
	damageGiven : 0,
	damageTaken : 0,
	death : 0,
	infections : 0,
	kills : 0,
	timeAlive : 0,
	id: 0,
	x: 0,
	y: 0,
};

function checkSize(w,h) {
	handle = w > h;
}

const username = function() {
    let search = window.location.search;
	if (search.startsWith("?username=")) {
		let name = decodeURIComponent(search.substr("?username=".length).replace(/\+/g, ' '));
		return name.length > 15 ? false : name;
	} else {
		window.location.pathname="/index.html";
		return false;
	}
}();

log("Pseudo: " + username);

//Create WebSocket connection.
const packetHandler = new PacketHandler('ws://' + window.location.host + '/game');
// Create WebSocket connection.
const screenPacketHandler = new PacketHandler('ws://' + window.location.host + '/game');

packetHandler.openWebSocket(function() {
	if (left !== false)
		left.set00();
	if (right !== false)
		right.set00();
	if (playerUUID === false)
		packetHandler.sendPacket(new PacketC00ConnectPlayer(username));
	else
		packetHandler.sendPacket(new PacketC03ReconnectPlayer(playerUUID, username));
});

screenPacketHandler.openWebSocket(function() {
	playerMap = [];
	scoresSurvivor = [];
	scoresInfected = [];
	crates = [];
	if (isSetup)
		IMAGE_MAP = loadImage("images/map.png");
	screenPacketHandler.sendPacket(new PacketC02ConnectScreen());
});

class Shoot {
	constructor(x1, y1, x2, y2) {
		this.ticks = 2;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	tick() {
		return (--this.ticks) < 0;
	}
	draw() {
		line(this.x1 * windowWidth, this.y1 * windowHeight, this.x2 * windowWidth, this.y2 * windowHeight);
	}
}

class PlayerData {
	constructor() {}
	
	move(x, y, lookX, lookY) {
		this.x = x;
		this.y = y;
		let tsn = lookX * lookX + lookY * lookY;
		if (tsn != 0) {
			let ts = Math.sqrt(tsn);
			this.rotation = (Math.asin(lookX / ts) < 0 ? -1 : 1) * Math.acos(-lookY / ts);
		}

		this.score = {
			id: 0,
			survivorSortId: 0,
			infectionSortId: 0,
			damageGiven: 0,
			damageTaken: 0,
			death: 0,
			infections: 0,
			kills: 0,
			timeAlive: 0
		}
	}
}

class Crate {
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}
}

class PacketS08Shot extends ServerPacket{
	read(dataview){
		if(dataview.byteLength < 32)
			return false;
		this.x1 = dataview.getFloat64(0);
		this.y1 = dataview.getFloat64(8);
		this.x2 = dataview.getFloat64(16);
		this.y2 = dataview.getFloat64(24);
	}
	
	handle(){
		shoots.push(new Shoot(this.x1, this.y1, this.x2, this.y2));
	}
}

class PressPoint {
	/**
	 * build a press point
	 * 
	 * @param {number}
	 *            x origin x
	 * @param {number}
	 *            y origin y
	 * @param {number}
	 *            idf touch identifier
	 * @param {number}
	 *            radius the radius of the controller to draw
	 * @param {*}
	 *            img the image object
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
		this.color = color(255, 255, 255, 50);
	}

	/**
	 * update the presspoint vector with a new (x,y) touch location
	 * 
	 * @param {number}
	 *            inputX x client input
	 * @param {number}
	 *            inputY y client input
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
	 * 
	 * @param {number}
	 *            idf
	 */
	updateIDF(idf) {
		this.idf = idf;
		this.vectorX = 0;
		this.vectorY = 0;
		this.drawControler = true;
	}
	/**
	 * set a new location for the controller
	 * 
	 * @param {number}
	 *            x the new x location
	 * @param {number}
	 *            y the new y location
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

	is00() {
		return this.vectorX == 0 && this.vectorY == 0;
	}
	
	set00() {
		this.vectorX = 0;
		this.vectorY = 0;
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

class PacketS02PlayerRegister extends ServerPacket {
	read(dataview){
		this.uuid = this.getUUID(dataview, 0);
		if (this.uuid === false)
			return false;
		if (dataview.byteLength < 20)
			return false;
		this.id = dataview.getInt32(16);
	}

    handle() {
    	playerUUID = this.uuid;
    	playerData.id = this.id;
    	log(this.uuid);
    }
}

class PacketS03PlayerSpawn extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4 * 2 + 8 * 4)
			return false;
		this.id = dataview.getInt32(0);
		this.x = dataview.getFloat64(4);
		this.y = dataview.getFloat64(12);
		this.lookX = dataview.getFloat64(20);
		this.lookY = dataview.getFloat64(28);
		this.type = dataview.getInt32(36);
		this.name = this.getUTF8String(dataview, 40);
		if (this.name === false)
			return false;
	}
    handle() {
    	let plr = playerMap[this.id];
    	if (plr === undefined)
    		plr = (playerMap[this.id] = new PlayerData());
    	
    	plr.move(this.x, this.y, this.lookX, this.lookY);
    	plr.type = this.type;
    	plr.name = this.name;
    }
}

class PacketS04PlayerMove extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4 + 8 * 4)
			return false;
		this.id = dataview.getInt32(0);
		this.x = dataview.getFloat64(4);
		this.y = dataview.getFloat64(12);
		this.lookX = dataview.getFloat64(20);
		this.lookY = dataview.getFloat64(28);
	}
    handle() {
    	let plr = playerMap[this.id];
    	
    	// send a player spawn guess if the player doesn't exists
    	if (plr === undefined) {
    		screenPacketHandler.sendPacket(new PacketC06GuessPlayer(this.id));
    	} else
    		plr.move(this.x, this.y, this.lookX, this.lookY);
    }
}

class PacketS05PlayerDead extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4)
			return false;
		this.id = dataview.getInt32(0);
	}

    handle() {
    	// we remove the player from the map
    	delete playerMap[this.id];
    }
}
class PacketScreenS06PlayerType extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 8)
			return false;
		this.type = dataview.getInt32(0);
		this.id = dataview.getInt32(4);
	}

    handle() {
    	let plr = playerMap[this.id];
    	// send a player spawn guess if the player doesn't exists
    	if (plr === undefined) {
    		screenPacketHandler.sendPacket(new PacketC06GuessPlayer(this.id));
    	} else
    		plr.type = this.type;
    }
}

class PacketS07PlayerSize extends ServerPacket {
	read(dataview) {
		if (dataview.byteLength < 16)
			return false;
		this.sizeX = dataview.getFloat64(0);
		this.sizeY = dataview.getFloat64(8);
	}
	
	handle() {
		playerSizeX = this.sizeX;
		playerSizeY = this.sizeY;
		oldScale = 1 / (this.sizeX) * 16;
	}
}

class PacketS06PlayerType extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4)
			return false;
		this.type = dataview.getInt32(0);
	}

    handle() {
    	playerData.type = this.type;
    	log("new type: " + playerData.type);
    }
}
class PacketS09ChangeHealth extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4)
			return false;
		this.health = dataview.getInt32(0);
	}

    handle() {
    	playerData.health = this.health;
    }
}
class PacketS0AChangeAmmos extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4)
			return false;
		this.ammos = dataview.getInt32(0);
	}

    handle() {
    	playerData.ammos = this.ammos;
    }
}
class PacketS0FScorePlayer extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 8 * 4)
			return false;
		this.survivorSortId = dataview.getInt32(0);
		this.infectionSortId = dataview.getInt32(4);
		this.damageGiven = dataview.getInt32(8);
		this.damageTaken = dataview.getInt32(12);
		this.death = dataview.getInt32(16);
		this.infections = dataview.getInt32(20);
		this.kills = dataview.getInt32(24);
		this.timeAlive = dataview.getInt32(28);
		
	}

    handle() {
    	playerData.survivorSortId = this.survivorSortId;
    	playerData.infectionSortId = this.infectionSortId;
    	playerData.damageGiven = this.damageGiven;
    	playerData.damageTaken = this.damageTaken;
    	playerData.death = this.death;
    	playerData.infections = this.infections;
    	playerData.kills = this.kills;
    	playerData.timeAlive = this.timeAlive;
    }
}

class PacketS11CrateSpawn extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4 + 8 * 2)
			return false;
		this.id = dataview.getInt32(0);
		this.x = dataview.getFloat64(4);
		this.y = dataview.getFloat64(12);
	}
    handle() {
    	let crate = new Crate(this.x, this.y);
    	crates[this.id] = crate;
    }
}

class PacketS12CrateRemove extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4)
			return false;
		this.id = dataview.getInt32(0);
	}

    handle() {
    	// we remove the player from the map
    	delete crates[this.id];
    }
}

packetHandler.registerPacketBuilder(0x02, () => new PacketS02PlayerRegister());
packetHandler.registerPacketBuilder(0x06, () => new PacketS06PlayerType());
packetHandler.registerPacketBuilder(0x09, () => new PacketS09ChangeHealth());
packetHandler.registerPacketBuilder(0x0A, () => new PacketS0AChangeAmmos());
packetHandler.registerPacketBuilder(0x0F, () => new PacketS0FScorePlayer());

screenPacketHandler.registerPacketBuilder(0x03, () => new PacketS03PlayerSpawn());
screenPacketHandler.registerPacketBuilder(0x04, () => new PacketS04PlayerMove());
screenPacketHandler.registerPacketBuilder(0x05, () => new PacketS05PlayerDead());
screenPacketHandler.registerPacketBuilder(0x06, () => new PacketScreenS06PlayerType());
screenPacketHandler.registerPacketBuilder(0x07, () => new PacketS07PlayerSize());
screenPacketHandler.registerPacketBuilder(0x08, () => new PacketS08Shot());
screenPacketHandler.registerPacketBuilder(0x11, () => new PacketS11CrateSpawn());
screenPacketHandler.registerPacketBuilder(0x12, () => new PacketS12CrateRemove());

function getPlayer() {
	return playerMap[playerData.id];
}

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);
	IMAGE_CONTROLER_LEFT = loadImage("images/controler_view.png");
	IMAGE_CONTROLER_RIGHT = loadImage("images/controler_weapon.png");
	IMAGE_DEAD = loadImage("images/dead.png");
	IMAGE_ROTATE = loadImage("images/rotate.png");
	IMAGE_TUTO = loadImage("images/tuto.png");
	IMAGE_PLAYER_S = loadImage("images/plr_survivant.png");
	IMAGE_PLAYER_I = loadImage("images/plr_zombie.png");
	IMAGE_MAP = loadImage("images/map.png");
	IMAGE_CRATE = loadImage("images/bonus_ammos.png");
	
	isSetup = true;
	
	left = new PressPoint(windowWidth / 6, windowHeight * 3 / 5, -1, windowWidth / 8, IMAGE_CONTROLER_LEFT);
	right = new PressPoint(windowWidth * 5 / 6, windowHeight * 3 / 5, -1, windowWidth / 6, IMAGE_CONTROLER_RIGHT);
	
	frameRate(fps);
	setInterval(tick, 1000 / tps);
	textAlign(CENTER, CENTER);

	checkSize(windowWidth, windowHeight);
	
	noSmooth();
}

function tick() {
	if (phase != GAME_PHASE_PLAYING)
		return;
	
	if (!left.is00() || !right.is00()) {
		// send move packet
		if (right.is00())
			packetHandler.sendPacket(new PacketC04Move(left.getMoveX(), left.getMoveY(), left.getMoveX(), left.getMoveY()));
		else
			packetHandler.sendPacket(new PacketC04Move(left.getMoveX(), left.getMoveY(), right.getMoveX(), right.getMoveY()));
	}

	let r2 = right.radius * right.radius;
	if (right.d2 > r2 / 4) {
		// send shot packet
		packetHandler.sendPacket(new PacketC05Shot());
	}
	Object.keys(shoots).forEach(key => {
		if (shoots[key].tick())
			delete shoots[key];
	});
}

function beautifulMillis(time) {
	let s = Math.floor(time / 1000);
	let min = Math.floor(s / 60);
	if (min != 0) {
		let h = Math.floor(min / 60);
		if (min != 0)
			return h + "h " + (min % 60) + "min " + (s % 60) + "s";
		
		return min + "min " + (s % 60) + "s";
	}
	
	return s + "s";
}

function draw() {
	if (!packetHandler.open) {
		background(200);
		fill(0);
		let time = millis();
		textSize(Math.min(windowWidth, windowHeight) / 8);
		translate(windowWidth / 2, windowHeight / 3);
		text('Reconnexion...', 0, 0);
		translate(0, windowHeight / 3);
		rotate(time / 1000);
		image(IMAGE_DEAD, -windowHeight / 12,-windowHeight / 12, windowHeight / 6, windowHeight / 6);
		return;
	}
	
	if (!handle) {
		background(200);
		fill(0);
		textSize(windowWidth / 15);
		translate(windowWidth / 2, windowHeight / 3);
		text('Tournez votre téléphone', 0, 0);
		translate(0, windowHeight / 3);
		image(IMAGE_ROTATE, -windowHeight / 6,-windowHeight / 6, windowHeight / 3, windowHeight / 3)
		return;
	}

	if (phase == GAME_PHASE_WAITING) {
		image(IMAGE_TUTO, 0,0, windowWidth, windowHeight);
		/*
		fill(color(0x3A, 0x46, 0));
		rect(0, 0, windowWidth, windowHeight);

		fill(255);
		translate(windowWidth / 2, windowHeight / 2);
		textSize(windowHeight / 12);
		text('En attente du lancement...', 0, 0);*/
		
	} else if (phase == GAME_PHASE_PLAYING) {
		
		const plr = getPlayer();
		
		if (plr != undefined) {
			const tx = -plr.x * windowWidth + windowWidth / 2;
			const ty = -plr.y * windowHeight + windowHeight / 2;
			fill(40);
			rect(0, 0, windowWidth, windowHeight);
			translate(tx, ty);
			
			image(IMAGE_MAP, 0, 0, windowWidth, windowHeight);

			stroke(40);
			
			shoots.forEach(s => s.draw());
			
			let sizeX = playerSizeX * windowWidth;
			let sizeY = playerSizeY * windowHeight;

			textSize(sizeX / 2);
			fill(0);
			
			playerMap.forEach(function (player, id) {
				let realX = player.x * windowWidth;
				let realY = player.y * windowHeight;
		
				translate(realX + sizeX / 2, realY + sizeY / 2);
		
				let img = player.type == PLAYER_TYPE_INFECTED ? IMAGE_PLAYER_I : IMAGE_PLAYER_S;
				
				rotate(player.rotation);
				image(img, - sizeX / 2, - sizeY / 2, sizeX, sizeY);
				
				rotate(-player.rotation);
		
				text(id, 0, - sizeY / 2 - sizeX / 4);
				
				translate(-realX - sizeX / 2, -realY - sizeY / 2);
			});
			crates.forEach(function (crate) {
				let realX = crate.x * windowWidth;
				let realY = crate.y * windowHeight;
		
				translate(realX + sizeX / 2, realY + sizeY / 2);
		
				image(IMAGE_CRATE, - sizeX / 2, - sizeY / 2, sizeX, sizeY);
		
				translate(-realX - sizeX / 2, -realY - sizeY / 2);
			});
			
			translate(-tx, -ty);
		}

		textSize(windowHeight / 10);
		noStroke();
		
		switch (playerData.type) {
		case PLAYER_TYPE_INFECTED:
			// display background
//			fill(color(0, 80, 0));
//			rect(0, 0, windowWidth, windowHeight);
	
			translate(windowWidth / 2, windowHeight / 5);
			fill(127);
			rect(-windowWidth/4, -windowHeight / 20, windowWidth/2, windowHeight / 10);
			
			if (playerData.health < 25)
				fill(color(255, 0, 0));
			else if(playerData.health < 50)
				fill(color(255, 255, 0));
			else
				fill(color(0, 255, 0));
	
			rect(-windowWidth/4, -windowHeight / 20, playerData.health * windowWidth / 200, windowHeight / 10);
			
	
			fill(0);
			text("Vie: "+playerData.health, 0, 0);
	
			break;
		case PLAYER_TYPE_SURVIVOR:
			// display background
//			fill(color(0, 0, 80));
//			rect(0, 0, windowWidth, windowHeight);
	
			translate(windowWidth / 2, windowHeight / 5);
			fill(255);
			text("Munitions: " + playerData.ammos, 0, 0);
			
			break;
		}
	
		translate(-windowWidth / 2, -windowHeight / 5);
		

		if (time > 0) {
			textSize(windowHeight / 10);
			let txt = "Infection dans " + time + "s";
			let tw = textWidth(txt) * 1.25;
			fill(0);
			rect(windowWidth / 2 - tw / 2, 0, tw, windowHeight / 10);
			fill(255);
			text(txt, windowWidth / 2, windowHeight / 20);
		}
	
		left.draw();
		right.draw();
		textSize(windowWidth / 10);
		textAlign(RIGHT, TOP);
		text(playerData.id, windowWidth, 0);
		textAlign(CENTER, CENTER);

	} else if (phase == GAME_PHASE_SCORE) {

		switch (playerData.type) {
		case PLAYER_TYPE_INFECTED:
			// display background
			fill(color(0, 80, 0));
			break;
		case PLAYER_TYPE_SURVIVOR:
			// display background
			fill(color(0, 0, 80));
			break;
		}
		rect(0, 0, windowWidth, windowHeight);
		fill(255);
		
		textSize(windowWidth / 40);
		text("SCORE", windowWidth / 2, 2 * windowHeight / 10);
		line(windowWidth / 2, 3.5 * windowHeight / 10, windowWidth / 2, 9 * windowHeight / 10);
		
		text("Temps", 1.2 * windowWidth / 16, 7 * windowHeight / 20);
		text("Elimination", 3 * windowWidth / 16, 7 * windowHeight / 20);
		text("Degats Donnés", 6 * windowWidth / 16, 7 * windowHeight / 20);
		
		text("Infections", 9 * windowWidth / 16, 7 * windowHeight / 20);
		text("Morts", 11 * windowWidth / 16, 7 * windowHeight / 20);
		text("Degats Pris", 13 * windowWidth / 16, 7 * windowHeight / 20);
		
		text(beautifulMillis(playerData.timeAlive),  1.2 * windowWidth / 16, windowHeight * 3 / 5);
		text(playerData.kills, 3 * windowWidth / 16,  windowHeight * 3 / 5);
		text(playerData.damageGiven, 6 * windowWidth / 16,  windowHeight * 3 / 5);
		
		text(playerData.infections, 9 * windowWidth / 16, windowHeight * 3 / 5);
		text(playerData.death, 11 * windowWidth / 16, windowHeight * 3 / 5);
		text(playerData.damageTaken, 13 * windowWidth / 16, windowHeight * 3 / 5);
	}
}


function touchStarted(ev) {
	if (phase != GAME_PHASE_PLAYING)
		return false;
	if (handle && ev instanceof TouchEvent) {
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
	if (phase != GAME_PHASE_PLAYING)
		return false;
	if (handle && ev instanceof TouchEvent) {
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
	if (phase != GAME_PHASE_PLAYING)
		return false;
	if (handle && ev instanceof TouchEvent) {
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
	right.updateLocation(windowWidth * 5 / 6, windowHeight * 3 / 5, -1, windowWidth / 6);
	
	checkSize(windowWidth, windowHeight);
}