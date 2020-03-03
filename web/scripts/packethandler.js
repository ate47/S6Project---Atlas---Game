const textDecoder = new TextDecoder();
const textEncoder = new TextEncoder();

class ServerPacket {
    constructor() {}

    getUTF8String(dataView, offset) {
        let l = dataView.getUint32(offset);
        let array = new Uint8Array(l);
        for (let i = 0; i < l; i++)
            array[i] = dataView.getUint8(offset + 4 + i);
        return textDecoder.decode(array);
    }
    read(dataView) {}
}

class ClientPacket {
    constructor(id, size) {
        this.id = id;
        this.size = size;
    }
    prepareUTF8String(str) {
        let encodedString = textEncoder.encode(str);
        this.size += encodedString.length + 4;
        return encodedString;
    }
    setUTF8String(dataView, offset, preparedString) {
        dataView.setUint32(offset, preparedString.length);
        for (let i = 0; i < preparedString.length; i++) 
            dataView.setUint8(offset + 4 + i, preparedString[i]);
    }
    write(dataView){}
}

class PacketC00HandShake extends ClientPacket {
    constructor(username) {
        super(0x00, 0);
        this.username = this.prepareUTF8String(username);
    }
    write(dataView) {
        this.setUTF8String(dataView, 0, this.username);
    }
}

class PacketC01KeepAlive extends ClientPacket {
    constructor() {
        super(0x01, 0);
    }
}

class PacketHandler {
    /**
     * 
     * @param {string} url the websocket url 'ws://' + window.location.host + '/game/screen' for screen
     */
    constructor(url) {
        // Create WebSocket connection.
        this.socket = new WebSocket(url);

        // Connection opened
        this.socket.onopen = this.webSocketOpen.bind(this);
        // Listen for messages
        this.socket.onmessage = this.webSocketMessage.bind(this);
        // Listen for messages
        this.socket.onclose = this.webSocketClose.bind(this);
        // Listen for messages
        this.socket.onerror = this.webSocketError.bind(this);
        
        this.socket.binaryType = "arraybuffer";
    }

    sendPacket(packet) {
        // create the buffer and set the packet ID
        const buffer = new ArrayBuffer(4 + packet.size);
        const viewint = new DataView(buffer);
        viewint.setUint32(0, packet.id);
        // load packet data
        const view = new DataView(buffer, 4);
        packet.write(view);
        // send
        this.socket.send(buffer);
    }

    webSocketOpen(ev) {
        console.log("WebSocket open");
        this.sendPacket(new PacketC00HandShake("xXPro_player_mlgXx"));
    }
    webSocketMessage(ev) {
        console.log(event.data);
    }
    webSocketClose(ev) {
        alert("Vous avez été exclu: " + ev.reason);
    }
    webSocketError(msg) {
        console.log(msg);
    }

}