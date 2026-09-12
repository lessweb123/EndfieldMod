package endfield.ui.markdown.extensions.curtain;

import endfield.ui.markdown.LayoutNodeRenderer;
import endfield.ui.markdown.NoActionVisitor;
import endfield.ui.markdown.RendererContext;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;

import java.util.Set;

public class CurtainRenderer extends LayoutNodeRenderer<CurtainProvider> {
	Visitor visitorInst = new Visitor();

	public CurtainRenderer(RendererContext context, CurtainProvider provider) {
		super(context, provider);
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return Set.of(Curtain.class);
	}

	@Override
	public void render(Node node) {
		node.accept(visitorInst);
	}

	class Visitor implements NoActionVisitor {
		@Override
		public void visit(CustomNode customNode) {
			if (customNode instanceof Curtain curtain) provider.add(context, curtain);
		}
	}
}
