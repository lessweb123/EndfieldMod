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
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.node.Visitor;

import java.util.Set;

public class BaseDrawRenderer extends LayoutNodeRenderer<BaseProvider> {
	public static Set<Class<? extends Node>> typeSet = Set.of(
			Document.class,
			Heading.class,
			Paragraph.class,
			BlockQuote.class,
			BulletList.class,
			FencedCodeBlock.class,
			HtmlBlock.class,
			ThematicBreak.class,
			IndentedCodeBlock.class,
			Link.class,
			ListItem.class,
			OrderedList.class,
			Image.class,
			Emphasis.class,
			StrongEmphasis.class,
			Text.class,
			Code.class,
			HtmlInline.class,
			SoftLineBreak.class,
			HardLineBreak.class
	);

	Visitor visitorInst = new BaseVisitor();

	public BaseDrawRenderer(RendererContext context, BaseProvider provider) {
		super(context, provider);
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return typeSet;
	}

	@Override
	public void render(Node node) {
		node.accept(visitorInst);
	}

	class BaseVisitor implements Visitor {
		@Override
		public void visit(BlockQuote blockQuote) {
			provider.add(context, blockQuote);
		}

		@Override
		public void visit(BulletList bulletList) {
			provider.add(context, bulletList);
		}

		@Override
		public void visit(Code code) {
			provider.add(context, code);
		}

		@Override
		public void visit(Document document) {
			provider.add(context, document);
		}

		@Override
		public void visit(Emphasis emphasis) {
			provider.add(context, emphasis);
		}

		@Override
		public void visit(FencedCodeBlock fencedCodeBlock) {
			provider.add(context, fencedCodeBlock);
		}

		@Override
		public void visit(HardLineBreak hardLineBreak) {
			provider.add(context, hardLineBreak);
		}

		@Override
		public void visit(Heading heading) {
			provider.add(context, heading);
		}

		@Override
		public void visit(ThematicBreak thematicBreak) {
			provider.add(context, thematicBreak);
		}

		@Override
		public void visit(HtmlInline htmlInline) {
			provider.add(context, htmlInline);
		}

		@Override
		public void visit(HtmlBlock htmlBlock) {
			provider.add(context, htmlBlock);
		}

		@Override
		public void visit(Image image) {
			provider.add(context, image);
		}

		@Override
		public void visit(IndentedCodeBlock indentedCodeBlock) {
			provider.add(context, indentedCodeBlock);
		}

		@Override
		public void visit(Link link) {
			provider.add(context, link);
		}

		@Override
		public void visit(ListItem listItem) {
			provider.add(context, listItem);
		}

		@Override
		public void visit(OrderedList orderedList) {
			provider.add(context, orderedList);
		}

		@Override
		public void visit(Paragraph paragraph) {
			provider.add(context, paragraph);
		}

		@Override
		public void visit(SoftLineBreak softLineBreak) {
			provider.add(context, softLineBreak);
		}

		@Override
		public void visit(StrongEmphasis strongEmphasis) {
			provider.add(context, strongEmphasis);
		}

		@Override
		public void visit(Text text) {
			provider.add(context, text);
		}

		@Override
		public void visit(LinkReferenceDefinition linkReferenceDefinition) {
			provider.add(context, linkReferenceDefinition);
		}

		@Override
		public void visit(CustomBlock customBlock) {
			provider.add(context, customBlock);
		}

		@Override
		public void visit(CustomNode customNode) {
			provider.add(context, customNode);
		}
	}
}
