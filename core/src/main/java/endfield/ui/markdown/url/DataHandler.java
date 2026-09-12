package endfield.ui.markdown.url;

import endfield.ui.markdown.UrlHandler;

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
		url = url.replaceFirst("data:", "");

		Matcher dataMatch = dataTypePattern.matcher(url);

		if (dataMatch.lookingAt() && dataMatch.group().equals("base64,")) {
			return new Base64Handle(url.substring(dataMatch.end()));
		} else {
			Matcher mimeMatch = mimeTypePattern.matcher(url);

			return new StringHandle(url.substring(mimeMatch.lookingAt() ? mimeMatch.end() : 0));
		}
	}

	public static class Base64Handle extends ResourceHandle {
		String base64;

		public Base64Handle(String s) {
			base64 = s;
		}

		@Override
		public InputStream openStream() {
			return Base64.getDecoder().wrap(new ByteArrayInputStream(base64.getBytes(StandardCharsets.UTF_8)));
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
