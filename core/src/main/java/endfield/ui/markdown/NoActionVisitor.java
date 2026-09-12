package endfield.ui.markdown;

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
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.node.Visitor;

public interface NoActionVisitor extends Visitor {
	@Override
	default void visit(BlockQuote blockQuote) {}

	@Override
	default void visit(BulletList bulletList) {}

	@Override
	default void visit(Code code) {}

	@Override
	default void visit(Document document) {}

	@Override
	default void visit(Emphasis emphasis) {}

	@Override
	default void visit(FencedCodeBlock fencedCodeBlock) {}

	@Override
	default void visit(HardLineBreak hardLineBreak) {}

	@Override
	default void visit(Heading heading) {}

	@Override
	default void visit(ThematicBreak thematicBreak) {}

	@Override
	default void visit(HtmlInline htmlInline) {}

	@Override
	default void visit(HtmlBlock htmlBlock) {}

	@Override
	default void visit(Image image) {}

	@Override
	default void visit(IndentedCodeBlock indentedCodeBlock) {}

	@Override
	default void visit(Link link) {}

	@Override
	default void visit(ListItem listItem) {}

	@Override
	default void visit(OrderedList orderedList) {}

	@Override
	default void visit(Paragraph paragraph) {}

	@Override
	default void visit(SoftLineBreak softLineBreak) {}

	@Override
	default void visit(StrongEmphasis strongEmphasis) {}

	@Override
	default void visit(Text text) {}

	@Override
	default void visit(LinkReferenceDefinition linkReferenceDefinition) {}

	@Override
	default void visit(CustomBlock customBlock) {}

	@Override
	default void visit(CustomNode customNode) {}
}
