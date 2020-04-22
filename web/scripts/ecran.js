let log = console.log;
let canvas;
let playerMap = [];
let shoots = [];
let playerSizeX = 0.005;
let playerSizeY = 0.005;
const fps = 24;
const tps = 20;
let IMAGE_MAP;
let IMAGE_PLAYER_S;
let IMAGE_PLAYER_I;
let IMAGE_DEAD;

const packetHandler = new PacketHandler('ws://' + window.location.host + '/game');

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
	}
    handle() {
    	let plr = playerMap[this.id];
    	if (plr === undefined)
    		plr = (playerMap[this.id] = new PlayerData());
    	
    	plr.move(this.x, this.y, this.lookX, this.lookY);
    	plr.type = this.type;
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
    		packetHandler.sendPacket(new PacketC06GuessPlayer(this.id));
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
class PacketS06PlayerType extends ServerPacket {
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
    		packetHandler.sendPacket(new PacketC06GuessPlayer(this.id));
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
packetHandler.registerPacketBuilder(0x03, () => new PacketS03PlayerSpawn());
packetHandler.registerPacketBuilder(0x04, () => new PacketS04PlayerMove());
packetHandler.registerPacketBuilder(0x05, () => new PacketS05PlayerDead());
packetHandler.registerPacketBuilder(0x06, () => new PacketS06PlayerType());
packetHandler.registerPacketBuilder(0x07, () => new PacketS07PlayerSize());
packetHandler.registerPacketBuilder(0x08, () => new PacketS08Shot ());

packetHandler.openWebSocket(function() {
	playerMap = [];
	packetHandler.sendPacket(new PacketC02ConnectScreen());
});

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);
	
	IMAGE_MAP = loadImage("images/map.png");

	IMAGE_PLAYER_S = loadImage("images/plr_survivant.png");
	IMAGE_PLAYER_I = loadImage("images/plr_zombie.png");
	IMAGE_DEAD = loadImage("images/dead.png");

	frameRate(fps);
	setInterval(tick, 1000 / tps);
	textAlign(CENTER, CENTER);
	
	noSmooth();
}

function tick() {
	Object.keys(shoots).forEach(key => {
		if (shoots[key].tick())
			delete shoots[key];
	});
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
		image(IMAGE_DEAD, -windowHeight / 12,-windowHeight / 12,windowHeight / 6,windowHeight / 6);
		return;
	}
	
	image(IMAGE_MAP, 0, 0, windowWidth, windowHeight);

	stroke(40);
	
	shoots.forEach(s => s.draw());
	
	let sizeX = playerSizeX * windowWidth;
	let sizeY = playerSizeY * windowHeight;
	
	playerMap.forEach(function (player) {
		let realX = player.x * windowWidth;
		let realY = player.y * windowHeight;

		translate(realX + sizeX / 2, realY + sizeY / 2);

		let img = player.type == PLAYER_TYPE_INFECTED ? IMAGE_PLAYER_I : IMAGE_PLAYER_S;
		
		rotate(player.rotation);
		image(img, - sizeX / 2, - sizeY / 2, sizeX, sizeY);
		rotate(-player.rotation);

		translate(-realX - sizeX / 2, -realY - sizeY / 2);
	});
	
	
	noStroke();
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);
}