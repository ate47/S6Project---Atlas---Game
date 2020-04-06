let log = console.log;
let canvas;
let playerMap = [];
const fps = 24;
const tps = 20;
let IMAGE_MAP;
let IMAGE_PLAYER_S;
let IMAGE_PLAYER_I;

const packetHandler = new PacketHandler('ws://' + window.location.host + '/game');

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
    	if (plr === undefined)
    		plr = (playerMap[this.id] = new PlayerData());
    	
    	plr.move(this.x, this.y, this.lookX, this.lookY);
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

packetHandler.registerPacketBuilder(0x03, () => new PacketS03PlayerSpawn());
packetHandler.registerPacketBuilder(0x04, () => new PacketS04PlayerMove());
packetHandler.registerPacketBuilder(0x05, () => new PacketS05PlayerDead());

packetHandler.openWebSocket(function() {
	packetHandler.sendPacket(new PacketC02ConnectScreen());
});

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);
	
	IMAGE_MAP = loadImage("images/map.png");

	IMAGE_PLAYER_S = loadImage("images/plr_survivant.png");
	IMAGE_PLAYER_I = loadImage("images/plr_zombie.png");

	frameRate(fps);
	setInterval(tick, 1000 / tps);
}

function tick() {
	
}

function draw() {
	image(IMAGE_MAP, 0, 0, windowWidth, windowHeight);
	
	playerMap.forEach(function (player) {
		let realX = player.x * windowWidth;
		let realY = player.y * windowHeight;

		translate(realX - 40, realY - 40);

		// TODO: check type
		let img = IMAGE_PLAYER_S;
		
		rotate(player.rotation);
		image(img, -40, -40, 80, 80);
		rotate(-player.rotation);

		translate(-realX + 40, -realY + 40);
	});
	
	noStroke();
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);
}