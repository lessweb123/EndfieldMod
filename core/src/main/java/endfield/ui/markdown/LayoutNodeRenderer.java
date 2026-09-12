package endfield.ui.markdown;

import org.commonmark.renderer.NodeRenderer;

public abstract class LayoutNodeRenderer<P extends MarkdownProvider> implements NodeRenderer {
	protected RendererContext context;
	protected P provider;

	public LayoutNodeRenderer(RendererContext c, P p) {
		context = c;
		provider = p;
	}
}
