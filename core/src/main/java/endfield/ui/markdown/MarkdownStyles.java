package endfield.ui.markdown;

import arc.Core;
import arc.freetype.FreeTypeFontGenerator;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Lines;
import arc.scene.style.BaseDrawable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Scl;
import arc.util.Tmp;
import endfield.ui.Fonts2;
import endfield.ui.markdown.Markdown.Box;
import endfield.ui.markdown.Markdown.FontEntry;
import endfield.ui.markdown.Markdown.Format;
import endfield.ui.markdown.Markdown.MarkdownStyle;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

public final class MarkdownStyles {
	static final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
	static final String[] romeSymbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

	static Font strong;
	static Font defDistanced;

	private MarkdownStyles() {}

	public static void load() {
		strong = new FreeTypeFontGenerator(Core.files.internal("fonts/font.woff")).generateFont(new FreeTypeFontGenerator.FreeTypeFontParameter() {{
			size = (int) Scl.scl(18f);
			borderWidth = Scl.scl(0.5f);
			incremental = true;
			borderColor = color;
		}});
		defDistanced = new FontGenerator2(Core.files.internal("fonts/font.woff")).generateFont(new FontGenerator2.FontParameter2() {{
			size = (int) Scl.scl(36f);
			incremental = true;

			genMipMaps = true;
			minFilter = Texture.TextureFilter.linear;
			magFilter = Texture.TextureFilter.linear;

			distanceFieldDownscale = 1;
			distanceFieldSpread = 6;
		}});
	}

	public static MarkdownStyle makeDefault() {
		MarkdownStyle style = new MarkdownStyle();

		style.loadingImg = Tex.nomap;

		style.linesPadding = 16f;
		style.paragraphPadding = 32f;

		style.lineColor = Color.gray;
		style.lineStroke = 2f;

		style.textFont = new FontEntry().font(Fonts.def).color(Color.white).scale(1f);
		style.subFont = new FontEntry().color(Color.lightGray);
		style.strongFont = new FontEntry().font(strong);
		style.emFont = new FontEntry().italic(true);
		style.headFonts = new FontEntry[6];
		for (int i = 0; i < 6; i++) {
			style.headFonts[i] = new FontEntry().font(defDistanced).color(i == 5 ? Color.gray : Color.white).scale(i < 5 ? 5f / (i + 1) : 1f);
		}
		style.quoteBox = new Box(Tex.paneLeft).margin(16f, 16f);
		style.curtainBox = new Box(((TextureRegionDrawable) Tex.whiteui).tint(Pal.darkestestGray), 0f, -6f, 6f, 6f);
		style.underLine = new BaseDrawable() {
			@Override
			public void draw(float x, float y, float width, float height) {
				Lines.stroke(Scl.scl(2f));
				Lines.line(x, y + height / 2f, x + width, y + height / 2f);
			}
		};

		style.codeFont = new FontEntry().font(Fonts2.jetbrainsmonomedium).color(Color.lightGray);
		style.codeBox = new Box(((TextureRegionDrawable) Tex.whiteui).tint(Tmp.c1.set(Pal.darkestGray).a(0.7f)), 0f, -6f, 6f, 6f);
		style.codeBlockBox = new Box(((TextureRegionDrawable) Tex.whiteui).tint(Tmp.c1.set(Pal.darkestGray))).margin(16f, 16f);
		style.sliderStyle = Styles.noBarPane;

		style.linkFont = new FontEntry().font(Fonts.outline).color(Pal.place);
		style.linkOverColor = Pal.accent;

		style.listOrderFont = new FontEntry().font(Fonts.def).color(Color.white);
		style.listItemBox = new Box().paddingLeft(16f);
		style.listItemHeadBox = new Box().paddingLeft(16f).paddingRight(8f);
		style.bulletListMarks = new Drawable[]{new BaseDrawable() {{
			setMinWidth(12f);
			setMinHeight(12f);
		}
			@Override
			public void draw(float x, float y, float width, float height) {
				Fill.square(x + width / 2, y + height / 2, width / 2.2f, 45f);
			}
		}, new BaseDrawable() {{
			setMinWidth(12f);
			setMinHeight(12f);
		}
			@Override
			public void draw(float x, float y, float width, float height) {
				Fill.circle(x + width / 2, y + height / 2, width / 2f);
			}
		}, new BaseDrawable() {{
			setMinWidth(12f);
			setMinHeight(12f);
		}
			@Override
			public void draw(float x, float y, float width, float height) {
				Lines.stroke(Scl.scl(1f));
				Lines.circle(x + width / 2, y + height / 2, width / 2f);
			}
		}};
		style.orderedListFormatters = new Format[]{it -> Integer.toString(it), it -> String.valueOf((char) ('a' + (it - 1) % 26)), it -> romeDigitize(it)};

		style.tableBack1 = new Box(makeTableCellBack(Pal.darkestGray.cpy().a(0.7f), Color.lightGray, Scl.scl(2f))).margin(32f, 32f);
		style.tableBack2 = new Box(makeTableCellBack(Pal.darkerGray.cpy().a(0.7f), Color.lightGray, Scl.scl(2f))).margin(32f, 32f);

		return style;
	}

	public static Drawable makeTableCellBack(Color backColor, Color lineColor, float lineStroke) {
		return new BaseDrawable() {
			@Override
			public void draw(float x, float y, float width, float height) {
				Draw.color(backColor);
				Fill.rect(x + width / 2, y + height / 2, width, height);
				Lines.stroke(lineStroke, lineColor);
				Lines.line(x, y, x + width, y);
				Lines.line(x, y + height, x + width, y + height);
			}
		};
	}

	public static String romeDigitize(int n) {
		int num = n;
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < values.length; i++) {
			while (num >= values[i]) {
				result.append(romeSymbols[i]);
				num -= values[i];
			}
		}

		return result.toString();
	}
}
