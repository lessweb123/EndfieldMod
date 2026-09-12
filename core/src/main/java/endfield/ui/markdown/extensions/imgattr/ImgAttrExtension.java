package endfield.ui.markdown.extensions.imgattr;

import org.commonmark.parser.Parser;
import org.commonmark.parser.Parser.Builder;

public class ImgAttrExtension implements Parser.ParserExtension {
	@Override
	public void extend(Builder builder) {
		builder.customDelimiterProcessor(new ImgAttrDelimiterProcessor());
	}
}
