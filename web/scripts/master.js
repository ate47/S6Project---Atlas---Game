let log = console.log;
let canvas;
let handle = false;
let confirmInfection = false;
let IMAGE_DEAD;
let IMAGE_BIOHAZARD;
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
class PacketC08LaunchPlayingPhase extends ClientPacket {
    constructor() {
        super(0x08, 0);
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
	confirmInfection = false;
	packetHandler.sendPacket(new PacketC07ConnectMaster(password));
});

function setup() {
	log("Create canvas("+windowWidth+", "+windowHeight+")");
	canvas = createCanvas(windowWidth, windowHeight);

	IMAGE_DEAD = loadImage("images/dead.png");
	IMAGE_BIOHAZARD = loadImage("images/logo_biohazard.png");
	
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

	background(200);
	
	if (phase == GAME_PHASE_WAITING) {

		let diameter = Math.min(windowHeight, windowWidth) * 2 / 3;

		translate(windowWidth / 2, windowHeight / 2);
		fill(confirmInfection ? color(200, 0, 0) : color(200, 200, 200));
		circle(0, 0, diameter);
		fill(confirmInfection ? color(255, 0, 0) : color(220, 220, 220));
		circle(0, 0, diameter * 4 / 5);
		
		image(IMAGE_BIOHAZARD, - diameter  / 6, - diameter / 6, diameter * 2 / 6, diameter * 2 / 6);

		textSize(diameter / 12);
		
		fill(0);
		text('Lancer l\'infection', 0, diameter / 2 + diameter / 12);
		
		
	} else if (phase == GAME_PHASE_PLAYING) {


	} else if (phase == GAME_PHASE_SCORE) {

		
	}
	
}

function mousePressed() {
	if (phase == GAME_PHASE_WAITING) {
		
		let dx = mouseX - windowWidth / 2;
		let dy = mouseY - windowHeight / 2;
		let d2 = Math.min(windowHeight, windowWidth) / 3;
		
		if ((dx * dx + dy * dy) <= d2 * d2) {
			if (confirmInfection) {
				log("Launching infection...");
				packetHandler.sendPacket(new PacketC08LaunchPlayingPhase());
			} else {
				confirmInfection = confirm("Activer le bouton ?");
			}
		}
		
	} else if (phase == GAME_PHASE_PLAYING) {


	} else if (phase == GAME_PHASE_SCORE) {

		
	}
	return false;
}

function windowResized() {
	log("Resize canvas("+windowWidth+", "+windowHeight+")");
	resizeCanvas(windowWidth, windowHeight);
}