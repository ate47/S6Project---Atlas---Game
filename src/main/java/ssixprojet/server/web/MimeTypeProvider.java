package ssixprojet.server.web;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeProvider {
	private static final Map<String, String> MIMES = new HashMap<>();
	private static final String MIME_DEFAULT = "text/plain";
	static {
		MIMES.put("html", "text/html");
		MIMES.put("htm", "text/html");
		MIMES.put("js", "text/javascript");
		MIMES.put("css", "text/css");

		MIMES.put("png", "image/png");
		MIMES.put("gif", "image/gif");
		MIMES.put("jpg", "image/jpeg");
		MIMES.put("jpeg", "image/jpeg");
		MIMES.put("svg", "image/svg+xml");
	}

	public static String getMime(String file) {
		int point = file.lastIndexOf(".");
		System.out.println(file + " / " + point);
		if (point == -1)
			return MIME_DEFAULT;
		return MIMES.getOrDefault(file.substring(point + 1), MIME_DEFAULT);
	}
}
