let log = console.log;
let canvas;
let handle = false;
let IMAGE_DEAD;
const packetHandler = new PacketHandler('ws://' + window.location.host + '/game');


const password = function() {
    let search = window.location.search;
	if (search.startsWith("?password=")) {
		return search.substr("?password=".length);
	} else {
		window.location.pathname="/master.html";
		return false;
	}
}();

class PacketC07ConnectMaster extends ClientPacket {
    constructor(password) {
        super(0x07, 0);
        this.password = this.prepareUTF8String(password);
    }
    write(dataView) {
        this.setUTF8String(dataView, 0, this.password);
    }
}
class PacketS0CBadPassword extends ServerPacket {
	read(dataview){}

    handle() {
    	alert("Mauvais mot de passe !");
		window.location.pathname="/master.html";
    }
}
class PacketS0DMasterLogged extends ServerPacket {
	read(dataview){}

    handle() {
    	handle = true;
    }
}

packetHandler.registerPacketBuilder(0x0C, () => new PacketS0CBadPassword());
packetHandler.registerPacketBuilder(0x0D, () => new PacketS0DMasterLogged());

packetHandler.openWebSocket(function() {
	handle = false;
	packetHandler.sendPacket(new PacketC07ConnectMaster(password));
});

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);

	IMAGE_DEAD = loadImage("images/dead.png");
	
	textAlign(CENTER, CENTER);
	
	noSmooth();
}

function draw() {
	if (!(packetHandler.open && handle)) {
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

	fill(color(0x3A, 0x46, 0));
	rect(0, 0, windowWidth, windowHeight);
	
	if (phase == GAME_PHASE_WAITING) {
		
	} else if (phase == GAME_PHASE_PLAYING) {


	} else if (phase == GAME_PHASE_SCORE) {

		
	}
	
}

function mousePressed() {
	
	return false;
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);
}