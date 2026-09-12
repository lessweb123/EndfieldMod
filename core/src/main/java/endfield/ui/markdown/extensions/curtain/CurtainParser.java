package endfield.ui.markdown.extensions.curtain;

import kotlin.collections.SetsKt;
import org.commonmark.node.Text;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.beta.InlineContentParser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.InlineParserState;
import org.commonmark.parser.beta.ParsedInline;
import org.commonmark.parser.beta.Position;
import org.commonmark.parser.beta.Scanner;
import org.commonmark.text.Characters;

import java.util.Set;

public class CurtainParser implements InlineContentParser {
	static final char DELIMITER = '$';

	@Override
	public ParsedInline tryParse(InlineParserState inlineParserState) {
		Scanner scanner = inlineParserState.scanner();
		Position start = scanner.position();
		int openingTicks = scanner.matchMultiple(DELIMITER);
		Position afterOpening = scanner.position();

		while (scanner.find(DELIMITER) > 0) {
			Position beforeClosing = scanner.position();
			int count = scanner.matchMultiple(DELIMITER);
			if (count == openingTicks) {
				Curtain node = new Curtain();

				String content = scanner.getSource(afterOpening, beforeClosing).getContent();
				content = content.replace('\n', ' ');

				if (content.length() >= 3 && content.charAt(0) == ' ' && content.charAt(content.length() - 1) == ' ' &&
						Characters.hasNonSpace(content)
				) {
					content = content.substring(1, content.length() - 1);
				}

				node.literal = content;
				return ParsedInline.of(node, scanner.position());
			}
		}

		SourceLines source = scanner.getSource(start, afterOpening);
		Text text = new Text(source.getContent());
		return ParsedInline.of(text, afterOpening);
	}

	public static class Factory implements InlineContentParserFactory {
		@Override
		public Set<Character> getTriggerCharacters() {
			return SetsKt.mutableSetOf(DELIMITER);
		}

		@Override
		public InlineContentParser create() {
			return new CurtainParser();
		}
	}
}
