package ssixprojet.server.web;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class WebFileBuffer extends WebBuffer {
	private String url;
	private byte[] buffer;

	public WebFileBuffer(String uri, String type, String url) {
		super(uri, type);
		this.url = url;
	}

	@Override
	public synchronized byte[] getBuffer(boolean reload) throws IOException {
		if (reload || buffer == null)
			return (buffer = FileUtils.readFileToByteArray(new File(url)));
		else
			return buffer;
	}

}
