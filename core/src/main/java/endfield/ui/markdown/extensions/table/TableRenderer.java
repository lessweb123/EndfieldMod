package endfield.ui.markdown.extensions.table;

import arc.scene.ui.layout.Table;
import endfield.ui.markdown.LayoutNodeRenderer;
import endfield.ui.markdown.NoActionVisitor;
import endfield.ui.markdown.RendererContext;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;

import java.util.Set;

public class TableRenderer extends LayoutNodeRenderer<TableProvider> {
	Visitor visitorInst = new Visitor();

	Table currentTable;

	public TableRenderer(RendererContext context, TableProvider provider) {
		super(context, provider);
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return Set.of(
				TableBlock.class,
				TableHead.class,
				TableBody.class,
				TableRow.class,
				TableCell.class,
				CellShadowBox.class
		);
	}

	@Override
	public void render(Node node) {
		node.accept(visitorInst);
	}

	class Visitor implements NoActionVisitor {
		@Override
		public void visit(CustomBlock customBlock) {
			if (customBlock instanceof TableBlock tableBlock) provider.add(context, tableBlock);
		}

		@Override
		public void visit(CustomNode customNode) {
			if (customNode instanceof TableHead tableHead) provider.add(context, tableHead);
			if (customNode instanceof TableBody tableBody) provider.add(context, tableBody);
			if (customNode instanceof TableRow tableRow) provider.add(context, tableRow);
			if (customNode instanceof TableCell tableCell) provider.add(context, tableCell);
			if (customNode instanceof CellShadowBox cellShadowBox) provider.add(context, cellShadowBox);
		}
	}
}
