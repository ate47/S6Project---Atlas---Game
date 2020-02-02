let log = console.log;
let oldWindowWidth = 0;
let oldWindowHeight = 0;

function createGameCanvas() {
	oldWindowWidth = windowWidth;
	oldWindowHeight = windowHeight;
	log("Create canvas("+windowWidth+", "+win+")");
	createCanvas(windowWidth, windowHeight);
}

function setup() {
}
  
function draw() {
	if (oldWindowWidth != windowWidth
		|| oldWindowHeight != windowHeight) {
			createGameCanvas();
		}
		 
	ellipse(mouseX, mouseY, 80, 80);
}