let log = console.log;
let canvas;
let playerMap = [];
let shoots = [];
let crates = [];
let scoresSurvivor = [];
let scoresInfected = [];
let playerSizeX = 0.005;
let playerSizeY = 0.005;
const fps = 24;
const tps = 20;
let isSetup = false;
let IMAGE_LOGO;
let IMAGE_MAP;
let IMAGE_PLAYER_S;
let IMAGE_PLAYER_I;
let IMAGE_DEAD;
let IMAGE_CRATE;

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

class PacketS10ScoreScreen extends ServerPacket {
	read(dataview){
		if (dataview.byteLength < 4)
			return false;
		this.maxPlayer = dataview.getInt32(0);

		if (dataview.byteLength < 4/* maxPlayer */ + 
				this.maxPlayer * 4 * 8 * 2 /* id, sortid, scores(6) */)
			return false;
		
		this.infected = [];
		this.survivor = [];

		let shift = 0;
		
		for (let i = 0; i < this.maxPlayer; i++) {
			this.infected[i] = {
				id: dataview.getInt32(shift += 4),
				infectionSortId: dataview.getInt32(shift += 4),
				damageGiven: dataview.getInt32(shift += 4),
				damageTaken: dataview.getInt32(shift += 4),
				death: dataview.getInt32(shift += 4),
				infections: dataview.getInt32(shift += 4),
				kills: dataview.getInt32(shift += 4),
				timeAlive: dataview.getInt32(shift += 4)
			};
		}
		for (let i = 0; i < this.maxPlayer; i++) {
			this.survivor[i] = {
				id: dataview.getInt32(shift += 4),
				survivorSortId: dataview.getInt32(shift += 4),
				damageGiven: dataview.getInt32(shift += 4),
				damageTaken: dataview.getInt32(shift += 4),
				death: dataview.getInt32(shift += 4),
				infections: dataview.getInt32(shift += 4),
				kills: dataview.getInt32(shift += 4),
				timeAlive: dataview.getInt32(shift += 4)
			};
		}
		
	}

    handle() {
		scoresSurvivor = [];
		scoresInfected = [];
		
    	let ninf, nsur, linf, lsur;
		for (let i = 0; i < this.maxPlayer; i++) {
			ninf = this.infected[i];
			nsur = this.survivor[i];
			linf = playerMap[ninf.id].score;
			lsur = playerMap[nsur.id].score;
			
			if (linf != undefined) {
				linf.infectionSortId = ninf.infectionSortId;
				linf.damageGiven = ninf.damageGiven;
				linf.damageTaken = ninf.damageTaken;
				linf.death = ninf.death;
				linf.infections = ninf.infections;
				linf.kills = ninf.kills;
				linf.timeAlive = ninf.timeAlive;
				scoresSurvivor[i] = ninf.id;
			} else
				scoresInfected[i] = -1;
			if (lsur != undefined) {
				lsur.survivorSortId = nsur.survivorSortId;
				lsur.damageGiven = nsur.damageGiven;
				lsur.damageTaken = nsur.damageTaken;
				lsur.death = nsur.death;
				lsur.infections = nsur.infections;
				lsur.kills = nsur.kills;
				lsur.timeAlive = nsur.timeAlive;
				scoresInfected[i] = nsur.id;
			} else
				scoresInfected[i] = -1;
			
		}
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

packetHandler.registerPacketBuilder(0x03, () => new PacketS03PlayerSpawn());
packetHandler.registerPacketBuilder(0x04, () => new PacketS04PlayerMove());
packetHandler.registerPacketBuilder(0x05, () => new PacketS05PlayerDead());
packetHandler.registerPacketBuilder(0x06, () => new PacketS06PlayerType());
packetHandler.registerPacketBuilder(0x07, () => new PacketS07PlayerSize());
packetHandler.registerPacketBuilder(0x08, () => new PacketS08Shot());
packetHandler.registerPacketBuilder(0x10, () => new PacketS10ScoreScreen());
packetHandler.registerPacketBuilder(0x11, () => new PacketS11CrateSpawn());
packetHandler.registerPacketBuilder(0x12, () => new PacketS12CrateRemove());

packetHandler.openWebSocket(function() {
	playerMap = [];
	scoresSurvivor = [];
	scoresInfected = [];
	crates = [];
	if (isSetup)
		IMAGE_MAP = loadImage("images/map.png");
	packetHandler.sendPacket(new PacketC02ConnectScreen());
});

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);
	
	IMAGE_LOGO = loadImage("images/logo_atlas.png");
	
	IMAGE_PLAYER_S = loadImage("images/plr_survivant.png");
	IMAGE_PLAYER_I = loadImage("images/plr_zombie.png");
	IMAGE_DEAD = loadImage("images/dead.png");
	IMAGE_MAP = loadImage("images/map.png");
	IMAGE_CRATE = loadImage("images/bonus_ammos.png");
	
	isSetup = true;

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
		image(IMAGE_DEAD, -windowHeight / 12,-windowHeight / 12,windowHeight / 6,windowHeight / 6);
		return;
	}

	if (phase == GAME_PHASE_WAITING) {
		fill(color(0x3A, 0x46, 0));
		rect(0, 0, windowWidth, windowHeight);
		
		translate(windowWidth / 2, 0);

		image(IMAGE_LOGO, -windowHeight / 4, 0, windowHeight / 2, windowHeight / 4);

		fill(255);
		translate(0, windowHeight / 16 + windowHeight / 6);

		translate(-windowWidth / 2, 0);
		textSize(windowHeight / 40);
		

		stroke(255);
		
		let x;
		for (let i = 0; i < 4; i++) {
			x = (2 * i + 3) * windowWidth / 12;
			line(x, -windowHeight / 80, x, 11 * windowHeight / 16);
		}
		
		noStroke();
			
		let i = 0;
		
		
		playerMap.forEach(function (player) {
			text(player.name, (i + 1) * windowWidth / 6, 0);
			if ((i = (i + 1) % 5) == 0)
				translate(0, windowHeight / 40);
		});
		
		
	} else if (phase == GAME_PHASE_PLAYING) {

		image(IMAGE_MAP, 0, 0, windowWidth, windowHeight);

		if (time > 0) {
			textSize(windowHeight / 20);
			let txt = "Infection dans " + time + "s";
			let tw = textWidth(txt) * 1.25;
			fill(0);
			rect(windowWidth / 2 - tw / 2, 0, tw, windowHeight / 20);
			fill(255);
			text(txt, windowWidth / 2, windowHeight / 40);
		}
		else{
			if(time < 0){
				textSize(windowHeight / 20);
				let timeN = - time;
				let txt = "Temps restant avant la fin " + timeN + "s";
				let tw = textWidth(txt) * 1.25;
				fill(0);
				rect(windowWidth / 2 - tw / 2, 0, tw, windowHeight / 20);
				fill(255);
				text(txt, windowWidth / 2, windowHeight / 40);
			}
		}
		
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

	} else if (phase == GAME_PHASE_SCORE) {

		fill(color(0, 0, 0x44));
		rect(0, 0, windowWidth, windowHeight);

		stroke(255);

		fill(255);
		textSize(windowHeight / 15);
		text("Tableau des scores", windowWidth / 2, windowHeight / 10);
		
		textSize(windowHeight / 30);
		text("Survivants", windowWidth / 4, 2 * windowHeight / 10);
		text("Infectés", 3 * windowWidth / 4, 2 * windowHeight / 10);
		line(windowWidth / 2, 3 * windowHeight / 10, windowWidth / 2, 9 * windowHeight / 10);

		stroke(16);
		
		textSize(windowHeight / 60);
		text("#", windowWidth / 16, 7 * windowHeight / 20);
		text("Nom", 3 * windowWidth / 16, 7 * windowHeight / 20);
		text("Temps", 5 * windowWidth / 16, 7 * windowHeight / 20);
		text("Eliminations", 6 * windowWidth / 16, 7 * windowHeight / 20);
		text("Degats Donnés", 7 * windowWidth / 16, 7 * windowHeight / 20);
		
		text("#", 9 * windowWidth / 16, 7 * windowHeight / 20);
		text("Nom", 11 * windowWidth / 16, 7 * windowHeight / 20);
		text("Infections", 13 * windowWidth / 16, 7 * windowHeight / 20);
		text("Morts", 14 * windowWidth / 16, 7 * windowHeight / 20);
		text("Degats Pris", 15 * windowWidth / 16, 7 * windowHeight / 20);
		
		let y, number, p;
		for (const i in scoresSurvivor) {
			number = parseInt(i, 10);
			if (number == -1)
				continue;
			if (number == 0) {
				fill(color(255,255,0));
			} else if (number == 1)
				fill(color(255,255,255));
			else if (number == 2)
				fill(color(0xff, 0x77, 0x55));
			else
				fill(color(155,155,155));
			y = (number + 8) * windowHeight / 20;
			text((number + 1), windowWidth / 16, y);
			p = playerMap[scoresSurvivor[i]];
			text(p.name, 3 * windowWidth / 16, y);
			text(beautifulMillis(p.score.timeAlive), 5 * windowWidth / 16, y);
			text(p.score.kills, 6 * windowWidth / 16, y);
			text(p.score.damageGiven, 7 * windowWidth / 16, y);
		}
		for (const i in scoresInfected) {
			number = parseInt(i, 10);
			if (number == -1)
				continue;
			if (number == 0) {
				fill(color(255,255,0));
			} else if (number == 1)
				fill(color(255,255,255));
			else if (number == 2)
				fill(color(0xff, 0x77, 0x55));
			else
				fill(color(155,155,155));
			y = (number + 8) * windowHeight / 20;
			text((number + 1), 9 * windowWidth / 16, y);
			p = playerMap[scoresInfected[i]];
			text(p.name, 11 * windowWidth / 16, y);

			text(p.score.infections, 13 * windowWidth / 16, y);
			text(p.score.death, 14 * windowWidth / 16, y);
			text(p.score.damageTaken, 15 * windowWidth / 16, y);
		}
	}
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);
}