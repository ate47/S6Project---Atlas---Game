class PacketHandler {
    /**
     * 
     * @param {string} url the websocket url 'ws://' + window.location.host + '/game/screen' for screen
     */
    constructor(url) {
        // Create WebSocket connection.
        this.socket = new WebSocket(url);
        this.socket.binaryType = "arraybuffer";

        // Connection opened
        this.socket.addEventListener('open', this.webSocketOpen);
        
        // Listen for messages
        this.socket.addEventListener('message', this.webSocketMessage);
        // Listen for messages
        this.socket.addEventListener('close', this.webSocketClose);
        // Listen for messages
        this.socket.addEventListener('error', this.webSocketError);
    }

    webSocketOpen(ev) {
        socket.send('Hello Server!');
    }
    webSocketMessage(ev) {
        console.log('Message from server ', event.data);
    }
    webSocketClose(ev) {
        console.log(ev);
    }
    webSocketError(msg) {
        console.log(msg);
    }

    sendPacket(packet) {

    }

}