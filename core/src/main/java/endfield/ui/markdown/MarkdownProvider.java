package endfield.ui.markdown;

import arc.func.Boolf;
import arc.func.Cons;
import org.commonmark.Extension;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;

import java.util.List;

public interface MarkdownProvider {
	default void renderChildren(RendererContext context, Node parent) {
		Node node = parent.getFirstChild();
		while (node != null) {
			context.render(node);
			node = node.getNext();
		}
	}

	default void eachChildren(Node node, Cons<Node> callback) {
		Node node1 = node.getFirstChild();
		while (node1 != null) {
			callback.get(node1);
			node1 = node1.getNext();
		}
	}

	default Node findChild(Node node, Boolf<Node> filter) {
		Node node1 = node.getFirstChild();
		while (node1 != null) {
			if (filter.get(node1)) return node1;
			node1 = node1.getNext();
		}

		return null;
	}

	default void eachDescendants(Node node, Cons<Node> callback) {
		Node node1 = node.getFirstChild();
		while (node1 != null) {
			callback.get(node1);
			eachDescendants(node1, callback);
			node1 = node1.getNext();
		}
	}

	default Node findDescendants(Node node, Boolf<Node> filter) {
		Node node1 = node.getFirstChild();
		while (node1 != null) {
			if (filter.get(node1)) return node1;
			else {
				Node des = findDescendants(node1, filter);
				if (des != null) return des;

				node1 = node1.getNext();
			}
		}

		return null;
	}

	List<Extension> extensions();

	List<UrlHandler> urlHandlers();

	UrlHandler defaultUrlHandler();

	void handleLayoutException(Throwable exception);

	void add(RendererContext context, Document node);

	void add(RendererContext context, Heading node);

	void add(RendererContext context, Paragraph node);

	void add(RendererContext context, BlockQuote node);

	void add(RendererContext context, Link node);

	void add(RendererContext context, Text node);

	void add(RendererContext context, Code node);

	void add(RendererContext context, BulletList node);

	void add(RendererContext context, OrderedList node);

	void add(RendererContext context, ListItem node);

	void add(RendererContext context, IndentedCodeBlock node);

	void add(RendererContext context, FencedCodeBlock node);

	void add(RendererContext context, ThematicBreak node);

	void add(RendererContext context, Image node);

	void add(RendererContext context, Emphasis node);

	void add(RendererContext context, StrongEmphasis node);

	void add(RendererContext context, SoftLineBreak node);

	void add(RendererContext context, HardLineBreak node);

	void add(RendererContext context, LinkReferenceDefinition node);

	default void add(RendererContext context, HtmlInline node) {
		throw new UnsupportedOperationException("Html was unsupported yet.");
	}

	default void add(RendererContext context, HtmlBlock node) {
		throw new UnsupportedOperationException("Html was unsupported yet.");
	}

	default void add(RendererContext context, CustomNode node) {
		throw new UnsupportedOperationException("node type " + node.getClass() + " not supported");
	}

	default void add(RendererContext context, CustomBlock node) {
		throw new UnsupportedOperationException("block type " + node.getClass() + " not supported");
	}
}
