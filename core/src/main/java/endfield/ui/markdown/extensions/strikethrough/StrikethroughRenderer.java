package endfield.ui.markdown.extensions.strikethrough;

import endfield.ui.markdown.LayoutNodeRenderer;
import endfield.ui.markdown.NoActionVisitor;
import endfield.ui.markdown.RendererContext;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;

import java.util.Set;

public class StrikethroughRenderer extends LayoutNodeRenderer<StrikethroughProvider> {
	Visitor visitorInst = new Visitor();

	public StrikethroughRenderer(RendererContext context, StrikethroughProvider provider) {
		super(context, provider);
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return Set.of(Strikethrough.class);
	}

	@Override
	public void render(Node node) {
		node.accept(visitorInst);
	}

	class Visitor implements NoActionVisitor {
		@Override
		public void visit(CustomNode customNode) {
			if (customNode instanceof Strikethrough strikethrough) provider.add(context, strikethrough);
		}
	}
}
