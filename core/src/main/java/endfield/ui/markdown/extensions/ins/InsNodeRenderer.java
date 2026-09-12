package endfield.ui.markdown.extensions.ins;

import endfield.ui.markdown.LayoutNodeRenderer;
import endfield.ui.markdown.NoActionVisitor;
import endfield.ui.markdown.RendererContext;
import org.commonmark.ext.ins.Ins;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;

import java.util.Set;

public class InsNodeRenderer extends LayoutNodeRenderer<InsProvider> {
	Visitor visitorInst = new Visitor();

	public InsNodeRenderer(RendererContext context, InsProvider provider) {
		super(context, provider);
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return Set.of(Ins.class);
	}

	@Override
	public void render(Node node) {
		node.accept(visitorInst);
	}

	class Visitor implements NoActionVisitor {
		@Override
		public void visit(CustomNode customNode) {
			if (customNode instanceof Ins ins) provider.add(context, ins);
		}
	}
}
