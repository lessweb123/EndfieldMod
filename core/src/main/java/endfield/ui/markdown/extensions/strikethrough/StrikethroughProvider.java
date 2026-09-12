package endfield.ui.markdown.extensions.strikethrough;

import endfield.ui.markdown.MarkdownProvider;
import endfield.ui.markdown.RendererContext;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;

public interface StrikethroughProvider extends MarkdownProvider {
	void add(RendererContext context, Strikethrough node);
}
