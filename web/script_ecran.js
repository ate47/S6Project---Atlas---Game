let log = console.log;
let canvas;
// Create WebSocket connection.
const socket = new WebSocket('ws://' + window.location.host + '/game/screen');

// Connection opened
socket.addEventListener('open', function (event) {
	socket.send('Hello Server!');
});

// Listen for messages
socket.addEventListener('message', function (event) {
	log('Message from server ', event.data);
});

function onSocketOpen(event) {
	log("Connection etablished");
}

function onSocketMessage(event) {
	log(event.message);
}
function onSocketError(msg) {
	log(msg);
}
function onSocketClose(event) {
	log(event);
}

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