package ssixprojet.server.web;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class WebFileBuffer extends WebBuffer {
	private File f;
	private byte[] buffer;

	public WebFileBuffer(String uri, String type, File f) {
		super(uri, type);
		this.f = f;
	}
	
	public WebFileBuffer(String uri, String type, String url) {
		this(url, type, new File(url));
	}

	@Override
	public synchronized byte[] getBuffer(boolean reload) throws IOException {
		if (reload || buffer == null)
			return (buffer = FileUtils.readFileToByteArray(f));
		else
			return buffer;
	}

}
