package ssixprojet.server.web;

import java.io.IOException;

public class WebByteBuffer extends WebBuffer {
	private byte[] buffer;

	public WebByteBuffer(String uri, String type, byte[] buffer) {
		super(uri, type);
		this.buffer = buffer;
	}

	@Override
	public byte[] getBuffer(boolean reload) throws IOException {
		return buffer;
	}

}
