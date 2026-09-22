package endfield.ui.markdown.url;

import endfield.ui.markdown.UrlHandler;
import kotlin.text.StringsKt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataHandler implements UrlHandler {
	static final Pattern mimeTypePattern = Pattern.compile("\\w+(/\\w+)?;");
	static final Pattern dataTypePattern = Pattern.compile("\\w+,");

	@Override
	public List<String> matchedSchemes() {
		return List.of("data");
	}

	@Override
	public void openUrl(String url) {
		throw new UnsupportedOperationException("Cannot open a data url directly.");
	}

	@Override
	public ResourceHandle getResource(String url) {
		String noScheme = url.replaceFirst("data:", "");
		int comma = noScheme.indexOf(',');
		if (comma < 0) {
			return new StringHandle("");
		}

		String header = StringsKt.take(noScheme, comma);
		String payload = noScheme.substring(comma + 1);

		for (String it : header.split(";")) {
			if (it.equalsIgnoreCase("base64")) return new Base64Handle(payload);
		}

		return new StringHandle(payload);
	}

	public static class Base64Handle extends ResourceHandle {
		String payload;

		public Base64Handle(String pay) {
			payload = pay;
		}

		@Override
		public InputStream openStream() {
			return Base64.getDecoder().wrap(new ByteArrayInputStream(payload.trim().getBytes(StandardCharsets.UTF_8)));
		}
	}

	public static class StringHandle extends ResourceHandle {
		String string;
		Charset charset;

		public StringHandle(String s) {
			string = s;
			charset = StandardCharsets.UTF_8;
		}

		public StringHandle(String s, Charset c) {
			string = s;
			charset = c;
		}

		@Override
		public InputStream openStream() {
			return new ByteArrayInputStream(string.getBytes(charset));
		}
	}
}
