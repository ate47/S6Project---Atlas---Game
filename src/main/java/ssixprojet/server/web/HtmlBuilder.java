package ssixprojet.server.web;

/**
 * a builder to build an html file
 */
public class HtmlBuilder {
	private String header = "";
	private String title;
	private String body = "";
	private String lang = "en";
	private String charset = "UTF-8";

	public HtmlBuilder(String title) {
		this.title = title;
	}

	/**
	 * add a body in the body part
	 * 
	 * @param body the html code to add
	 * @return the builder
	 */
	public HtmlBuilder body(String body) {
		this.body += body + "\n";
		return this;
	}

	/**
	 * @return the string of the builded file
	 */
	public String build() {
		return "<!DOCTYPE html>\n<html lang=\"" + lang + "\">\n" + "<head>\n" + "<meta charset=\"" + charset
				+ "\">\n<title>" + title + "</title>\n" + header + "</head>\n<body>\n" + body + "</body>\n</html>";
	}

	/**
	 * @return the bytes of the builded file
	 */
	public byte[] buildToBytes() {
		return build().getBytes();
	}

	/**
	 * set the charset of this file
	 * 
	 * @param charset the charset
	 * @return the builder
	 */
	public HtmlBuilder charset(String charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * add a header in the head part
	 * 
	 * @param header the header to add
	 * @return the builder
	 */
	public HtmlBuilder header(String header) {
		this.header += header + "\n";
		return this;
	}

	/**
	 * set the lang of this file
	 * 
	 * @param lang the lang
	 * @return the builder
	 */
	public HtmlBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * add JavaScript code to the file
	 * 
	 * @param js the code
	 * @return the builder
	 */
	public HtmlBuilder script(String js) {
		return header("<script>" + js + "</script>");
	}

	/**
	 * add a JavaScript file source to the file
	 * 
	 * @param file the file to import
	 * @return the builder
	 */
	public HtmlBuilder scriptFile(String file) {
		return header("<script src=\"" + file + "\"></script>");
	}

	/**
	 * add CSS code to the file
	 * 
	 * @param css the code
	 * @return the builder
	 */
	public HtmlBuilder style(String css) {
		return header("<style>" + css + "</style>");
	}

	/**
	 * add a CSS file source to the file
	 * 
	 * @param file the file to import
	 * @return the builder
	 */
	public HtmlBuilder styleFile(String file) {
		return header("<link href=\"" + file + "\" hrel=\"stylesheet\" />");
	}
}
