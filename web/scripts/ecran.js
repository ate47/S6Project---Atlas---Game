let log = console.log;
let canvas;
let playerMap = [];

class PlayerData {
	constructor() {}
	
	move(x,y,lookX,lookY) {
		this.x = x;
		this.y = y;
		this.lookX = lookX;
		this.lookY = lookY;
	}
}

class PacketS03PlayerSpawn extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4 + 8 * 4)
			return false;
		this.id = dateview.getInt32(0);
		this.x = dateview.getFloat64(4);
		this.y = dateview.getFloat64(12);
		this.lookX = dateview.getFloat64(20);
		this.lookY = dateview.getFloat64(28);
	}
    handle() {
    	let plr = playerMap[this.id];
    	if (plr === undefined)
    		plr = (playerMap[this.id] = new Player());
    	
    	plr.move(this.x, this.y, this.lookX, this.lookY);
    }
}

class PacketS04PlayerMove extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4 + 8 * 4)
			return false;
		this.id = dateview.getInt32(0);
		this.x = dateview.getFloat64(4);
		this.y = dateview.getFloat64(12);
		this.lookX = dateview.getFloat64(20);
		this.lookY = dateview.getFloat64(28);
	}
    handle() {
    	let plr = playerMap[this.id];
    	// create a player if the player doesn't exists (like with create?)
    	if (plr === undefined)
    		plr = (playerMap[this.id] = new Player());

    	// ...and move it (I like to move it move it...)
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

const packetHandler = new PacketHandler('ws://' + window.location.host + '/game');

packetHandler.registerPacketBuilder(0x03, () => new PacketS03PlayerSpawn());
packetHandler.registerPacketBuilder(0x04, () => new PacketS04PlayerMove());
packetHandler.registerPacketBuilder(0x05, () => new PacketS05PlayerDead());

packetHandler.openWebSocket(function() {
	packetHandler.sendPacket(new PacketC02ConnectScreen());
});

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);

	frameRate(60);
	setInterval(tick, 20);
}

function tick() {
	
}

function draw() {
	fill(color(220, 220, 220));
	rect(0, 0, windowWidth, windowHeight);
	noStroke();
	
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);
}