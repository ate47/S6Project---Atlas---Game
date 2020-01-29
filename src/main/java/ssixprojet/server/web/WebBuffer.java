package ssixprojet.server.web;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

public abstract class WebBuffer {
	private String uri;
	private String type;

	public WebBuffer(String uri, String type) {
		this.uri = uri;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getUri() {
		return uri;
	}

	public abstract byte[] getBuffer(boolean reload) throws IOException;

	public FullHttpResponse buildResponse(boolean reload) throws IOException {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
				Unpooled.wrappedBuffer(getBuffer(reload)));
		response.headers().set(CONTENT_TYPE, type);
		response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
		return response;
	}
}
