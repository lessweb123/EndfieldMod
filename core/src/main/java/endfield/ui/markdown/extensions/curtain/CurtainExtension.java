package endfield.ui.markdown.extensions.curtain;

import endfield.ui.markdown.MDLayoutRenderer;
import endfield.ui.markdown.MDLayoutRenderer.DrawRendererExtension;
import org.commonmark.parser.Parser;
import org.commonmark.parser.Parser.ParserExtension;

public class CurtainExtension implements ParserExtension, DrawRendererExtension {
	@Override
	public void extend(Parser.Builder parserBuilder) {
		parserBuilder.customInlineContentParserFactory(new CurtainParser.Factory());
	}

	@Override
	public void extend(MDLayoutRenderer.Builder rendererBuilder) {
		rendererBuilder.nodeRendererFactory(CurtainRenderer::new);
	}
}
