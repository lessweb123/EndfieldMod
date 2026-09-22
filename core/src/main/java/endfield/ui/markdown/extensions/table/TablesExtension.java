package endfield.ui.markdown.extensions.table;

import endfield.ui.markdown.MDLayoutRenderer;
import org.commonmark.ext.gfm.tables.internal.TableBlockParser;
import org.commonmark.parser.Parser;

public class TablesExtension implements Parser.ParserExtension, MDLayoutRenderer.DrawRendererExtension {
	public static final int MAX_CELLS = 200;

	@Override
	public void extend(Parser.Builder parserBuilder) {
		parserBuilder.customBlockParserFactory(new TableBlockParser.Factory(MAX_CELLS));
	}

	@Override
	public void extend(MDLayoutRenderer.Builder rendererBuilder) {
		rendererBuilder.nodeRendererFactory(TableRenderer::new);
	}
}
