const textDecoder = new TextDecoder();
const textEncoder = new TextEncoder();

const DEBUG = true;

const PLAYER_TYPE_INFECTED = 0;
const PLAYER_TYPE_SURVIVOR = 1;

const GAME_PHASE_WAITING = 0;
const GAME_PHASE_PLAYING = 1;
const GAME_PHASE_SCORE = 2;

let phase = 0;
let time = 0;

class UUID {
    constructor() {
        const buffer = new ArrayBuffer(16);
        this.view = new DataView(buffer);
    }

    read(dataView, offset) {
        this.view.setUint32(0, dataView.getUint32(offset));
        this.view.setUint32(4, dataView.getUint32(offset + 4));
        this.view.setUint32(8, dataView.getUint32(offset + 8));
        this.view.setUint32(12, dataView.getUint32(offset + 12));
    }
    
    write(dataView, offset) {
        dataView.setUint32(offset, this.view.getUint32(0));
        dataView.setUint32(offset + 4, this.view.getUint32(4));
        dataView.setUint32(offset + 8, this.view.getUint32(8));
        dataView.setUint32(offset + 12, this.view.getUint32(12));
    }
}

class ServerPacket {
    constructor() {}

    /**
	 * get a UTF8 string from a DataView
	 * 
	 * @param {DataView}
	 *            dataView the dataview to read
	 * @param {number}
	 *            offset the offset
	 * @returns string
	 */
    getUTF8String(dataView, offset) {
    	if (dataView.byteLength - offset < 4)
    		return false;
        let l = dataView.getUint32(offset);
    	if (dataView.byteLength - offset - 4 < l)
    		return false;
        let array = new Uint8Array(l);
        for (let i = 0; i < l; i++)
            array[i] = dataView.getUint8(offset + 4 + i);
        return textDecoder.decode(array);
    }

    getUUID(dataView, offset) {
    	if (dataView.byteLength - offset < 16)
    		return false;
        let uuid = new UUID();
        uuid.read(dataView, offset);
        return uuid;
    }
    /**
	 * read packet data from a DataView
	 * 
	 * @param {DataView}
	 *            dataView the dataview
	 */
    read(dataView) {}
}

class ClientPacket {
    constructor(id, size) {
        this.id = id;
        this.size = size;
    }
    /**
	 * prepare a UTF8 string to be send
	 * 
	 * @param {string}
	 *            str the string
	 * @returns Uint8Array
	 */
    prepareUTF8String(str) {
        let encodedString = textEncoder.encode(str);
        this.size += encodedString.length + 4;
        return encodedString;
    }
    /**
	 * set a prepared UTF8 string at an offset
	 * 
	 * @param {DataView}
	 *            dataView the dataview to set
	 * @param {number}
	 *            offset the offset to put the prepared string
	 * @param {Uint8Array}
	 *            preparedString the prepared string
	 */
    setUTF8String(dataView, offset, preparedString) {
        dataView.setUint32(offset, preparedString.length);
        for (let i = 0; i < preparedString.length; i++) 
            dataView.setUint8(offset + 4 + i, preparedString[i]);
    }
    /**
	 * write packet data to the DataView
	 * 
	 * @param {DataView}
	 *            dataView
	 */
    write(dataView){}
}

class PacketC00ConnectPlayer extends ClientPacket {
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

class PacketC02ConnectScreen extends ClientPacket {
    constructor() {
        super(0x02, 0);
    }
}

class PacketC03ReconnectPlayer extends ClientPacket {
    /**
	 * create a reconnect packet
	 * 
	 * @param {UUID}
	 *            uuid the player uuid
	 */
    constructor(uuid, username) {
        super(0x03, 16)
        this.uuid = uuid;
        this.username = this.prepareUTF8String(username);
    }
    write(dataView) {
        this.uuid.write(dataView, 0);
        this.setUTF8String(dataView, 16, this.username);
    }
}

class PacketC04Move extends ClientPacket {
    constructor(deltaX, deltaY, lx, ly) {
        super(0x04, 32); // dx, dy, lx, ly
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.lx = lx;
        this.ly = ly;
    }
    write(dataView) {
        dataView.setFloat64(0, this.deltaX);
        dataView.setFloat64(8, this.deltaY);
        dataView.setFloat64(16, this.lx);
        dataView.setFloat64(24, this.ly);
    }
}
class PacketC05Shot extends ClientPacket {
    constructor() {
        super(0x05, 0);
    }
}
class PacketC06GuessPlayer extends ClientPacket {
	constructor(playerID) {
		super(0x06, 4);
		this.playerID = playerID;
	}
    write(dataView) {
    	dataView.setInt32(0, this.playerID);
    }
}

class PacketS0BSetGamePhase extends ServerPacket {
	read(dataview) {
		if (dataview.byteLength < 4)
			return false;
		this.phase = dataview.getInt32(0);
	}
	
	handle() {
		phase = this.phase;
	}
}

class PacketS0ETimeToWaitPing extends ServerPacket {
	read(dataview) {
		if (dataview.byteLength < 4)
			return false;
		this.time = dataview.getInt32(0);
	}

	handle() {
		time = this.time;
	}
}
class PacketHandler {
    /**
	 * 
	 * @param {string}
	 *            url the websocket url 'ws://' + window.location.host +
	 *            '/game/screen' for screen
	 */
    constructor(url) {
        // Create WebSocket connection.
        this.url = url;
        this.packetBuilder = [];
        this.registerPacketBuilder(0x0B, () => new PacketS0BSetGamePhase());
        this.registerPacketBuilder(0x0E, () => new PacketS0ETimeToWaitPing());
    }

    openWebSocket(callback = false) {
        this.socket = new WebSocket(this.url);

        // Connection opened
        this.socket.onopen = this.webSocketOpen.bind(this);
        // Listen for messages
        this.socket.onmessage = this.webSocketMessage.bind(this);
        // Listen for messages
        this.socket.onclose = this.webSocketClose.bind(this);
        // Listen for messages
        this.socket.onerror = this.webSocketError.bind(this);
        
        this.socket.binaryType = "arraybuffer";
        this.open = false;

        this.callback = callback;
    }

    registerPacketBuilder(packetId, builder) {
    	let old = this.packetBuilder[packetId];
    	if (old !== undefined) {
    		console.log("Overwriting already registered packet builder with id : " + packetId);
    	}
		this.packetBuilder[packetId] = builder;
    }
    
    /**
	 * send a packet to the server
	 * 
	 * @param {ClientPacket}
	 *            packet the packet to send
	 */
    sendPacket(packet) {
        if (DEBUG)
        	console.log(packet);
        
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
    	if (this.open)
    		return;
    	time = 0;
        this.open = true;
        console.log("WebSocket open");
        if (this.callback !== false)
            this.callback();
    }
    webSocketMessage(ev) {
    	const viewint = new DataView(event.data);
    	let packetId, builder, size;
    	
    	const count = viewint.getInt32(0);
    	let delta = 4; // data shift
    	
    	for (let i = 0; i < count; i++) {
    		packetId = viewint.getInt32(delta);
    		size = viewint.getInt32(delta + 4);
            if (DEBUG) {
            	log("Packet(packetId: "+packetId+", size: "+size+")")
            }
    		builder = this.packetBuilder[packetId];
        	if (builder != undefined) {
        		const view = new DataView(event.data, delta + 8, size);
        		const packet = builder();
        		
        		if (packet.read(view) === false)
        			return;
                if (DEBUG)
                	console.log(packet);
        		packet.handle();
        	} else {
        		console.log("Weird packet id: " + packetId);
        	}
        	delta += 8 + size;
    	}
    }
    webSocketClose(ev) {
        console.log(ev);
        this.open = false;
        setTimeout(function(packetHandler) {
        	packetHandler.openWebSocket(packetHandler.callback);
        }, 2000, this);
        
    }
    webSocketError(msg) {
        console.log(msg);
    }

}