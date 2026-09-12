package endfield.ui.markdown;

import kotlin.collections.CollectionsKt;
import org.commonmark.Extension;
import org.commonmark.internal.renderer.NodeRendererMap;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.ArrayList;
import java.util.List;

public class MDLayoutRenderer {
	List<DrawRendererFactory<?>> nodeRendererFactories;
	RendererContextImpl context;

	MDLayoutRenderer(Builder builder) {
		List<DrawRendererFactory<?>> factories = new ArrayList<>(builder.nodeRendererFactories);
		factories.add((c, p) -> new BaseDrawRenderer(c, (BaseProvider) p));
		nodeRendererFactories = CollectionsKt.toList(factories);
	}

	public RendererContext createContext(Markdown element) {
		context = new RendererContextImpl(element);
		return context;
	}

	public void renderLayout(Node node) {
		if (context == null) throw new IllegalStateException("context must be created first");

		context.init();
		context.render(node);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		List<DrawRendererFactory<?>> nodeRendererFactories;

		public MDLayoutRenderer build() {
			return new MDLayoutRenderer(this);
		}

		public <P extends MarkdownProvider> Builder nodeRendererFactory(DrawRendererFactory<P> nodeRendererFactory) {
			nodeRendererFactories.add(nodeRendererFactory);
			return this;
		}

		public Builder extensions(Iterable<? extends Extension> extensions) {
			for (Extension extension : extensions) {
				if (extension instanceof DrawRendererExtension e) {
					e.extend(this);
				}
			}
			return this;
		}
	}

	public interface DrawRendererExtension extends Extension {
		void extend(Builder rendererBuilder);
	}

	class RendererContextImpl extends RendererContext {
		NodeRendererMap nodeRendererMap = new NodeRendererMap();

		@SuppressWarnings("unchecked")
		public RendererContextImpl(Markdown element) {
			super(element);

			for (int i = nodeRendererFactories.size() - 1; i >= 0; i--) {
				DrawRendererFactory<?> nodeRendererFactory = nodeRendererFactories.get(i);
				NodeRenderer nodeRenderer = ((DrawRendererFactory<MarkdownProvider>) nodeRendererFactory)
						.create(this, element.provider);
				nodeRendererMap.add(nodeRenderer);
			}
		}

		@Override
		public void render(Node node) {
			nodeRendererMap.render(node);
		}
	}
}
