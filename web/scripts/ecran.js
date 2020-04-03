let log = console.log;
let canvas;
const packetHandler = new PacketHandler('ws://' + window.location.host + '/game');

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