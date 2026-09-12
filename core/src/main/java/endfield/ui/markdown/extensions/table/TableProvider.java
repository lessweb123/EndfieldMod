package endfield.ui.markdown.extensions.table;

import endfield.ui.markdown.MarkdownProvider;
import endfield.ui.markdown.RendererContext;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;

public interface TableProvider extends MarkdownProvider {
	void add(RendererContext context, TableBlock node);

	void add(RendererContext context, TableHead node);

	void add(RendererContext context, TableBody node);

	void add(RendererContext context, TableRow node);

	void add(RendererContext context, TableCell node);

	void add(RendererContext context, CellShadowBox node);
}
