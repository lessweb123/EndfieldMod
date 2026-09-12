package endfield.ui.markdown;

import org.commonmark.renderer.NodeRenderer;

@FunctionalInterface
public interface DrawRendererFactory<P extends MarkdownProvider> {
	NodeRenderer create(RendererContext context, P provider);
}
