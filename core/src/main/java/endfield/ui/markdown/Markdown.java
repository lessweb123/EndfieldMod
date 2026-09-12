package endfield.ui.markdown;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.BaseDrawable;
import arc.scene.style.Drawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.pooling.Pool.Poolable;
import arc.util.pooling.Pools;
import endfield.ui.markdown.MDLayoutRenderer.DrawRendererExtension;
import mindustry.Vars;
import mindustry.ui.Fonts;
import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.parser.Parser.ParserExtension;

import java.util.ArrayList;
import java.util.List;

public class Markdown extends WidgetGroup {
	boolean wrapContent = true;

	Seq<MarkdownDraw> markdownDraws = new Seq<>(MarkdownDraw.class);
	List<MarkdownDraw> drawList = new ArrayList<>();

	Node node;
	MarkdownStyle style;

	boolean prefInvalid = true;
	boolean buildActObjs;

	float lastPrefHeight;
	float prefWidth;
	float prefHeight;

	Parser parser;
	MDLayoutRenderer renderer;

	MarkdownProvider provider;
	RendererContext rendererContext;

	public Markdown(String content, MarkdownStyle style) {
		this(content, style, new BaseProvider());
	}

	public Markdown(String content, MarkdownStyle s, MarkdownProvider prov) {
		List<Extension> extensions = prov.extensions();
		checkExtensions(extensions);

		provider = prov;
		style = s;

		parser = Parser.builder().extensions(extensions).build();
		renderer = MDLayoutRenderer.builder().extensions(extensions).build();

		rendererContext = renderer.createContext(this);

		node = parser.parse(content);
		touchable = Touchable.childrenOnly;
	}

	Markdown(Markdown parent, Node n) {
		provider = parent.provider;
		parser = null;
		renderer = MDLayoutRenderer.builder().extensions(provider.extensions()).build();
		rendererContext = renderer.createContext(this);

		node = n;
		touchable = Touchable.childrenOnly;

		style = parent.getStyle();
	}

	void checkExtensions(List<Extension> extensions) {
		for (Extension extension : extensions) {
			if (!(extension instanceof DrawRendererExtension) && !(extension instanceof ParserExtension)) {
				throw new IllegalArgumentException("extension must be a DrawRendererExtension or a ParserExtension");
			}
		}
	}

	public RendererContext getContext() {
		return rendererContext;
	}

	public void setProvider(MarkdownProvider prov) {
		provider = prov;
		rendererContext = renderer.createContext(this);
		invalidate();
	}

	public MarkdownProvider getProvider() {
		return provider;
	}

	public void setStyle(MarkdownStyle s) {
		style = s;
		invalidate();
	}

	public MarkdownStyle getStyle() {
		return style;
	}

	public void directlyOpenUrl() {
		urlClicked(url -> {
			try {
				rendererContext.openUrl(url);
			} catch (Exception e) {
				Log.err(e);
				Vars.ui.showException(e);
			}
		});
	}

	public void urlClicked(Cons<String> callback) {
		addListener(e -> {
			if (e instanceof UrlClickedEvent c) {
				callback.get(c.clickedUrl);
				return true;
			}

			return false;
		});
	}

	@Override
	public void layout() {
		if (wrapContent) {
			float prefHeight = getPrefHeight();
			if (prefHeight != lastPrefHeight) {
				lastPrefHeight = prefHeight;
				invalidateHierarchy();
			}
		}

		for (MarkdownDraw obj : markdownDraws) {
			obj.free();
		}
		markdownDraws.clear();

		try {
			renderer.renderLayout(node);
		} catch (Throwable e) {
			provider.handleLayoutException(e);
		}

		markdownDraws.addAll(rendererContext.renderResult());
		drawList = markdownDraws.retainAll(it -> it.drawTiming != DrawTiming.NEVER).sort(it -> it.drawTiming.ordinal()).list();

		buildActObjs = true;
		clearChildren();

		for (MarkdownDraw obj : markdownDraws) {
			if (obj.drawTiming != DrawTiming.NEVER && obj instanceof ActivityDrawer ad) {
				Element element = ad.activeElement();
				addChild(element);
				element.setBounds(
						obj.offsetX,
						height - obj.offsetY - obj.height,
						obj.width,
						obj.height
				);
				element.validate();
			}
		}
	}

	@Override
	protected void childrenChanged() {
		if (!buildActObjs) invalidateHierarchy();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		prefInvalid = true;
	}

	public void calculatePrefSize() {
		try {
			renderer.renderLayout(node);
		} catch (Throwable e) {
			provider.handleLayoutException(e);
		}

		prefInvalid = false;

		prefWidth = rendererContext.prefWidth();
		prefHeight = rendererContext.prefHeight();
	}

	@Override
	public float getPrefWidth() {
		if (prefInvalid) calculatePrefSize();
		return wrapContent ? 0f : prefWidth;
	}

	@Override
	public float getPrefHeight() {
		if (prefInvalid) calculatePrefSize();
		return prefHeight;
	}

	@Override
	protected void drawChildren() {
		for (var obj : drawList) {
			if (obj instanceof ActivityDrawer && cullingArea != null && !cullingArea.overlaps(
					obj.offsetX,
					height + obj.offsetY,
					obj.width,
					obj.height
			)) continue;

			Draw.reset();
			Draw.alpha(parentAlpha);
			obj.draw(x, y + height);
		}
		super.drawChildren();
	}

	public static class FontEntry {
		public Font fontModifier;
		public boolean isItalic;
		public Color colorModifier;
		public float scaleModifier;

		public FontEntry() {}

		public FontEntry(Font font, boolean isItal, Color colorMod, float scaleMod) {
			fontModifier = font;
			isItalic = isItal;
			colorModifier = colorMod;
			scaleModifier = scaleMod;
		}

		public FontEntry font(Font font) {
			fontModifier = font;
			return this;
		}

		public FontEntry color(Color color) {
			colorModifier = color;
			return this;
		}

		public FontEntry scale(float scale) {
			scaleModifier = scale;
			return this;
		}

		public FontEntry italic(boolean isItal) {
			isItalic = isItal;
			return this;
		}
	}

	public static class Box {
		public Drawable background;
		public float paddingLeft, paddingRight, paddingTop, paddingBottom;
		public float marginLeft, marginRight, marginTop, marginBottom;

		public Box() {}

		public Box(Drawable ground) {
			background = ground;
		}

		public Box(Drawable ground, float paddingX, float paddingY, float marginX, float marginY) {
			background = ground;
			paddingLeft = paddingX;
			paddingRight = paddingX;
			paddingTop = paddingY;
			paddingBottom = paddingY;
			marginLeft = marginX;
			marginRight = marginX;
			marginTop = marginY;
			marginBottom = marginY;
		}

		public Box(Drawable ground, float pl, float pr, float pt, float pb, float ml, float mr, float mt, float mb) {
			background = ground;
			paddingLeft = pl;
			paddingRight = pr;
			paddingTop = pt;
			paddingBottom = pb;
			marginLeft = ml;
			marginRight = mr;
			marginTop = mt;
			marginBottom = mb;
		}

		public Box margin(float marginX, float marginY) {
			marginLeft = marginX;
			marginRight = marginX;
			marginTop = marginY;
			marginBottom = marginY;
			return this;
		}

		public Box paddingLeft(float pl) {
			paddingLeft = pl;
			return this;
		}

		public Box paddingRight(float pr) {
			paddingRight = pr;
			return this;
		}
	}

	public static class MarkdownStyle {
		public static FontEntry defaultFont = new FontEntry(
				Fonts.def,
				false,
				Color.white,
				1f
		);
		public static Box defaultBox = new Box();
		public static BaseDrawable defaultDraw = new BaseDrawable();

		public Drawable loadingImg = defaultDraw;

		//globals
		public float linesPadding;
		public float paragraphPadding;

		public Color lineColor = Color.white;
		public float lineStroke;

		public ScrollPane.ScrollPaneStyle sliderStyle = new ScrollPane.ScrollPaneStyle();

		//normal
		public FontEntry textFont = defaultFont;
		public FontEntry subFont = defaultFont;
		public FontEntry emFont = defaultFont;
		public FontEntry strongFont = defaultFont;
		public FontEntry[] headFonts = {};
		public Box[] headBox = {};
		public Box quoteBox = defaultBox;
		public Box curtainBox = defaultBox;
		public Drawable underLine = defaultDraw;
		public Drawable strikethrough = defaultDraw;

		//code
		public FontEntry codeFont = defaultFont;
		public Box codeBox = defaultBox;
		public Box codeBlockBox = defaultBox;

		//link
		public FontEntry linkFont = defaultFont;
		public Color linkOverColor = Color.white;

		//list
		public FontEntry listOrderFont = defaultFont;
		public Box listItemBox = defaultBox;
		public Box listItemHeadBox = defaultBox;
		public Drawable[] bulletListMarks = {};
		public Format[] orderedListFormatters = new Format[0];

		//table
		public Box tableBack1 = defaultBox;
		public Box tableBack2 = defaultBox;
	}

	public static abstract class MarkdownDraw implements Poolable {
		protected static Color tmp1 = new Color(), tmp2 = new Color();

		public float offsetX, offsetY;

		public float width, height;

		public DrawTiming drawTiming = DrawTiming.MAIN;

		public abstract float prefWidth();

		public abstract float prefHeight();

		public abstract void setup(RendererContext.Scope scope);

		public abstract void draw(float x, float y);

		@Override
		public void reset() {
			offsetY = 0f;
			offsetX = 0f;
		}

		public void free() {
			Pools.free(this);
		}
	}

	public class ChapterEntry {
		public String title;
		public float offsetX, offsetY;
		public int level;

		public ChapterEntry(String t, float x, float y, int l) {
			title = t;
			offsetX = x;
			offsetY = y;
			level = l;
		}

		public float getDrawX() {
			return offsetX;
		}

		public float getDrawY() {
			return height - offsetY;
		}
	}

	public interface ActivityDrawer {
		Element activeElement();
	}

	public enum DrawTiming {
		PREVIOUSLY,
		MAIN,
		POST,
		NEVER
	}

	@FunctionalInterface
	public interface Format {
		String get(int param);
	}
}
