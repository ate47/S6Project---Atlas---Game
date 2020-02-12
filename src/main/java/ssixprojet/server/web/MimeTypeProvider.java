package ssixprojet.server.web;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeProvider {
	private static final Map<String, String> MIMES = new HashMap<>();
	public static final String TEXT_PLAIN = "text/plain";
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_JAVASCRIPT = "text/javascript";
	public static final String TEXT_CSS = "text/css";

	public static final String IMAGE_PNG = "image/png";
	public static final String IMAGE_GIF = "image/gif";
	public static final String IMAGE_JPEG = "image/jpeg";
	public static final String IMAGE_SVG = "image/svg+xml";
	static {
		MIMES.put("html", TEXT_HTML);
		MIMES.put("htm", TEXT_HTML);
		MIMES.put("js", TEXT_JAVASCRIPT);
		MIMES.put("css", TEXT_CSS);

		MIMES.put("png", IMAGE_PNG);
		MIMES.put("gif", IMAGE_GIF);
		MIMES.put("jpg", IMAGE_JPEG);
		MIMES.put("jpeg", IMAGE_JPEG);
		MIMES.put("svg", IMAGE_SVG);
	}

	public static String getMime(String endOfName) {
		return MIMES.getOrDefault(endOfName, TEXT_PLAIN);
	}
}
