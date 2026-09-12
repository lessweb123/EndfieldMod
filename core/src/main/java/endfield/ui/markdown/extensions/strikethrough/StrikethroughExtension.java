package endfield.ui.markdown.extensions.strikethrough;

import endfield.ui.markdown.MDLayoutRenderer;
import org.commonmark.ext.gfm.strikethrough.internal.StrikethroughDelimiterProcessor;
import org.commonmark.parser.Parser;

public class StrikethroughExtension implements Parser.ParserExtension, MDLayoutRenderer.DrawRendererExtension {
	@Override
	public void extend(Parser.Builder parserBuilder) {
		parserBuilder.customDelimiterProcessor(new StrikethroughDelimiterProcessor());
	}

	@Override
	public void extend(MDLayoutRenderer.Builder rendererBuilder) {
		rendererBuilder.nodeRendererFactory(StrikethroughRenderer::new);
	}
}
