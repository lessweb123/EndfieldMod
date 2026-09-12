package endfield.ui.markdown.extensions.table;

import endfield.ui.markdown.Markdown.Box;
import kotlin.collections.CollectionsKt;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;

public class CellShadowBox extends CustomNode {
	public TableCell shadowedCell;
	public Box cellBox;

	public CellShadowBox(TableCell cell, Box box) {
		shadowedCell = cell;
		cellBox = box;

		setSourceSpans(CollectionsKt.toList(cell.getSourceSpans()));
	}

	@Override
	public Node getParent() {
		return null;
	}

	@Override
	public Node getNext() {
		return null;
	}

	@Override
	public Node getPrevious() {
		return null;
	}

	@Override
	public Node getFirstChild() {
		return shadowedCell.getFirstChild();
	}

	@Override
	public Node getLastChild() {
		return shadowedCell.getLastChild();
	}
}
