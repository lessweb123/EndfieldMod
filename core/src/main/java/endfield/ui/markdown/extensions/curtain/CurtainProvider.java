package endfield.ui.markdown.extensions.curtain;

import endfield.ui.markdown.MarkdownProvider;
import endfield.ui.markdown.RendererContext;

public interface CurtainProvider extends MarkdownProvider {
	void add(RendererContext context, Curtain node);
}
