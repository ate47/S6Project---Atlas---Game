package ssixprojet.server.web;

import java.io.IOException;
import java.util.List;

public class WebDirectoryBuffer extends WebBuffer {
	private byte[] data;

	public WebDirectoryBuffer(String context, List<String> dirs) {
		super(context, MimeTypeProvider.TEXT_HTML);HtmlBuilder bld = new HtmlBuilder(getUri());
		bld.body("<h1>" + getUri() + "</h1>");
		bld.body("<p><a href='..'>..</a></p>");
		for (String sub : dirs)
			bld.body("<p><a href='" + sub + "'>" + sub + "</a></p>");
		data = bld.buildToBytes();
	}

	@Override
	public byte[] getBuffer(boolean reload) throws IOException {
		return data;
	}

}
