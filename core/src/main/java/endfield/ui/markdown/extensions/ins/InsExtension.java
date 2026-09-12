package endfield.ui.markdown.extensions.ins;

import endfield.ui.markdown.MDLayoutRenderer;
import org.commonmark.ext.ins.internal.InsDelimiterProcessor;
import org.commonmark.parser.Parser;

public class InsExtension implements Parser.ParserExtension, MDLayoutRenderer.DrawRendererExtension {
	@Override
	public void extend(Parser.Builder parserBuilder) {
		parserBuilder.customDelimiterProcessor(new InsDelimiterProcessor());
	}

	@Override
	public void extend(MDLayoutRenderer.Builder rendererBuilder) {
		rendererBuilder.nodeRendererFactory(InsNodeRenderer::new);
	}
}
