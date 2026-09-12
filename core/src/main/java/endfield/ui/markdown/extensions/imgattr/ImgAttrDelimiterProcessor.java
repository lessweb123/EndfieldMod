package endfield.ui.markdown.extensions.imgattr;

import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.ext.image.attributes.internal.ImageAttributesDelimiterProcessor;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImgAttrDelimiterProcessor extends ImageAttributesDelimiterProcessor {
	@Override
	public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
		if (openingRun.length() != 1) return 0;

		Text opener = openingRun.getOpener();
		Node nodeToStyle = opener.getPrevious();
		if (!(nodeToStyle instanceof Image)) return 0;

		List<Node> toUnlink = new ArrayList<>();
		StringBuilder content = new StringBuilder();

		for (Node node : Nodes.between(opener, closingRun.getCloser())) {
			if (node instanceof Text text) {
				content.append(text.getLiteral());
				toUnlink.add(node);
			} else {
				return 0;
			}
		}

		Map<String, String> attributesMap = new HashMap<>();
		String attributes = content.toString();
		for (String s : attributes.split("\\s+")) {
			if (s.isEmpty()) continue;

			String[] attribute = s.split("=");
			String key = attribute[0], value = attribute[1];
			if (key.isEmpty() && value.isEmpty()) {
				attributesMap.put(key, value);
			} else {
				return 0;
			}
		}

		for (Node node : toUnlink) node.unlink();

		if (!attributesMap.isEmpty()) {
			ImageAttributes imageAttributes = new ImageAttributes(attributesMap);

			nodeToStyle.appendChild(imageAttributes);
		}

		return 1;
	}
}
