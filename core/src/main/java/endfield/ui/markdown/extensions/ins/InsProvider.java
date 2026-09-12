package endfield.ui.markdown.extensions.ins;

import endfield.ui.markdown.MarkdownProvider;
import endfield.ui.markdown.RendererContext;
import org.commonmark.ext.ins.Ins;

public interface InsProvider extends MarkdownProvider {
	void add(RendererContext context, Ins node);
}
