package endfield.ui.markdown;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Func;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.ui.layout.Scl;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import endfield.ui.markdown.Markdown.ChapterEntry;
import endfield.ui.markdown.Markdown.MarkdownDraw;
import endfield.ui.markdown.Markdown.MarkdownStyle;
import endfield.ui.markdown.UrlHandler.ResourceHandle;
import endfield.ui.markdown.elemdraw.DrawImg;
import endfield.util.Strings2;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.text.StringsKt;
import mindustry.ui.Fonts;
import org.commonmark.node.Node;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public abstract class RendererContext {
	static Pattern schemeTypePattern = Pattern.compile("\\w+:");

	Markdown element;

	Seq<MarkdownDraw> markdownDraws = new Seq<>(MarkdownDraw.class);
	ObjectMap<String, Object> attachedVars = new ObjectMap<>();
	Seq<ChapterEntry> chapterEntries = new Seq<>(ChapterEntry.class);

	@Nullable Scope currentScope;
	@Nullable Scope rootScope;

	Map<String, Object> resourceCache = new HashMap<>();
	Map<String, UrlHandler> urlHandlers;
	UrlHandler defaultUrlHandler;

	public RendererContext(Markdown e) {
		element = e;
		urlHandlers = MapsKt.toMap(CollectionsKt.flatMap(element.provider.urlHandlers(), h -> CollectionsKt.map(h.matchedSchemes(), it -> new Pair<>(it, h))));
		defaultUrlHandler = element.provider.defaultUrlHandler();
	}

	public float prefWidth() {
		return rootScope == null ? 0f : rootScope.width + rootScope.paddingLeft + rootScope.paddingRight;
	}

	public float prefHeight() {
		return rootScope == null ? 0f : rootScope.height + rootScope.paddingTop + rootScope.paddingBottom;
	}

	public MarkdownStyle mdStyle() {
		return element.getStyle();
	}

	public float mdWidth() {
		return element.getWidth();
	}

	public float mdHeight() {
		return element.getHeight();
	}

	public boolean mdShouldWrap() {
		return element.wrapContent;
	}

	public void mdInvalidate() {
		element.invalidate();
	}

	public Markdown createSubMarkdown(Node node) {
		return new Markdown(element, node);
	}

	public abstract void render(Node node);

	public void init() {
		rootScope = null;
		currentScope = null;
		markdownDraws.clear();
		attachedVars.clear();
		chapterEntries.clear();
	}

	UrlHandler resolveUrlHandler(String url) {
		MatchResult schemeMatch = Strings2.matchAt(schemeTypePattern, url, 0);
		if (schemeMatch == null) return defaultUrlHandler;
		String scheme = StringsKt.trimEnd(schemeMatch.group(), ':');

		UrlHandler urlHandler = urlHandlers.get(scheme);

		if (urlHandler == null) throw new IllegalArgumentException("Unknown scheme type: " + scheme);

		return urlHandler;
	}

	public void openUrl(String url) {
		resolveUrlHandler(url).openUrl(url);
	}

	ResourceHandle getUrlResource(String url) {
		return resolveUrlHandler(url).getResource(url);
	}

	@SuppressWarnings("unchecked")
	public <T> T resolveResource(String url, Func<ResourceHandle, T> resourceResolver) {
		return (T) resourceCache.computeIfAbsent(url, u -> {
			ResourceHandle handle = getUrlResource(u);
			return resourceResolver.get(handle);
		});
	}

	public void invalidResource(String url) {
		resourceCache.remove(url);
	}

	public void putVar(String name, Object value) {
		attachedVars.put(name, value);
	}

	public void invalidVar(String name) {
		attachedVars.remove(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getVar(String name) {
		return (T) attachedVars.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getVar(String name, T def) {
		T res = (T) attachedVars.get(name);
		if (res == null) {
			attachedVars.put(name, def);
			return def;
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public <T> T getVar(String name, Prov<T> def) {
		return (T) attachedVars.get(name, (Prov<Object>) def);
	}

	public Scope getScope() {
		return currentScope == null ? insertScope() : currentScope;
	}

	public Scope insertScope(Markdown.Box box, Prov<MarkdownDraw> drawProvider, float boundX, boolean fillX, boolean inlineBreak) {
		return insertScope(drawProvider, Scl.scl(box.paddingLeft), Scl.scl(box.paddingRight), Scl.scl(box.paddingTop), Scl.scl(box.paddingBottom), Scl.scl(box.marginLeft), Scl.scl(box.marginRight), Scl.scl(box.marginTop), Scl.scl(box.marginBottom), boundX, fillX, inlineBreak);
	}

	public Scope insertScope(Markdown.Box box, Prov<MarkdownDraw> drawProvider) {
		return insertScope(box, drawProvider, (currentScope == null ? 0f : currentScope.boundX) - box.paddingRight, false, false);
	}

	public Scope insertScope(Markdown.Box box) {
		return insertScope(box, box.background == null ? null : () -> {
			DrawImg drawImg = DrawImg.get(box.background);
			drawImg.drawTiming = Markdown.DrawTiming.PREVIOUSLY;
			return drawImg;
		});
	}

	public Scope insertScope() {
		return insertScope(null, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, false, false);
	}

	public Scope insertScope(Prov<MarkdownDraw> drawProvider, float paddingLeft, float paddingRight, float paddingTop, float paddingBottom, float marginLeft, float marginRight, float marginTop, float marginBottom, float boundX) {
		return insertScope(drawProvider, paddingLeft, paddingRight, paddingTop, paddingBottom, marginLeft, marginRight, marginTop, marginBottom, boundX, false, false);
	}

	public Scope insertScope(Prov<MarkdownDraw> drawProvider, float paddingLeft, float paddingRight, float paddingTop, float paddingBottom, float marginLeft, float marginRight, float marginTop, float marginBottom, float boundX, boolean fillX, boolean inlineBreak) {
		Scope scope = new Scope(drawProvider, currentScope, paddingLeft, paddingRight, paddingTop, paddingBottom, marginLeft, marginRight, marginTop, marginBottom, boundX, fillX, inlineBreak);

		if (currentScope == null) {
			rootScope = scope;
		}

		if (scope.scopeDraw != null) {
			scope.scopeDraw.setup(scope);
			markdownDraws.add(scope.scopeDraw);
		}

		currentScope = scope;

		return scope;
	}

	public Scope popScope() {
		Scope curr = currentScope;

		if (curr == null) throw new IllegalStateException("Current has no scope be set.");

		Scope last = curr.parent;

		if (last != null) {
			last.width = Math.max(last.width, curr.offsetX + curr.width - last.offsetX + curr.paddingRight + last.marginRight);
			last.height = Math.max(last.height, curr.offsetY + curr.height - last.offsetY + curr.paddingBottom + last.marginBottom);
			last.rowHeight = Math.max(last.rowHeight, curr.height + curr.paddingTop + curr.paddingBottom);
			last.currOffsetX += curr.width + curr.paddingLeft + curr.paddingRight;
		}

		currentScope = last;

		if (curr.scopeDraw != null) {
			curr.scopeDraw.offsetX = curr.offsetX;
			curr.scopeDraw.offsetY = curr.offsetY;
			curr.scopeDraw.width = curr.width;
			curr.scopeDraw.height = curr.height;
		}

		return curr;
	}

	public @Nullable ChapterEntry findChapter(String title) {
		return findChapter(title, -1);
	}

	public @Nullable ChapterEntry findChapter(String title, int level) {
		for (ChapterEntry it : chapterEntries) {
			if ((level == -1 || level == it.level) && title.equals(it.title)) return it;
		}
		return null;
	}

	public @Nullable ChapterEntry findChapter(Pattern pattern) {
		return findChapter(pattern, -1);
	}

	public @Nullable ChapterEntry findChapter(Pattern pattern, int level) {
		for (ChapterEntry it : chapterEntries) {
			if ((level == -1 || level == it.level) && pattern.matcher(it.title).matches()) return it;
		}
		return null;
	}

	public List<ChapterEntry> filterChapter(Pattern pattern, int level) {
		List<ChapterEntry> result = new ArrayList<>();
		for (ChapterEntry it : chapterEntries) {
			if ((level == -1 || level == it.level) && pattern.matcher(it.title).matches()) result.add(it);
		}
		return result;
	}

	public void pushChapterEntry(String title, float offsetX, float offsetY, int level) {
		chapterEntries.add(element.new ChapterEntry(title, offsetX, offsetY, level));
	}

	public int captureCount() {
		return chapterEntries.size;
	}

	public void draw(MarkdownDraw markdownDraw) {
		Scope curr = getScope();

		markdownDraw.offsetX = curr.currOffsetX;
		markdownDraw.offsetY = curr.currOffsetY;

		markdownDraw.setup(curr);
		markdownDraw.width = markdownDraw.prefWidth();
		markdownDraw.height = markdownDraw.prefHeight();

		float boundX = curr.boundX - curr.marginRight;

		if (curr.boundX > 0 && markdownDraw.offsetX + markdownDraw.width > boundX) {
			float srkW = boundX - markdownDraw.offsetX;
			float ratio = srkW / markdownDraw.width;

			markdownDraw.width = srkW;
			markdownDraw.height *= ratio;
		}

		markdownDraws.add(markdownDraw);

		curr.currOffsetX += markdownDraw.width;
		curr.width = Math.max(curr.width, markdownDraw.offsetX + markdownDraw.width - curr.offsetX + curr.marginRight);
		curr.height = Math.max(curr.height, markdownDraw.offsetY + markdownDraw.height - curr.offsetY + curr.marginBottom);
		curr.rowHeight = Math.max(curr.rowHeight, markdownDraw.height);
	}

	public void pad(float padding) {
		getScope().currOffsetX += padding;
	}

	public Scope row(float rowPadding) {
		Scope last = getScope();
		if (last.inlineBreak) {
			if (last.drawProvider != null && last.width <= last.marginLeft + last.marginRight) {
				markdownDraws.remove(last.scopeDraw);
			}
			popScope();
		}

		Scope curr = getScope();
		curr.currOffsetX = curr.offsetX + curr.marginLeft;
		curr.currOffsetY += curr.rowHeight + rowPadding;
		curr.rowHeight = 0f;

		if (last.inlineBreak) {
			Scope scope = insertScope(
					last.drawProvider,
					last.paddingLeft, last.paddingRight, last.paddingTop, last.paddingBottom,
					last.marginLeft, last.marginRight, last.marginTop, last.marginBottom,
					last.boundX);
			scope.font = last.font;
			scope.fontColor = last.fontColor;
			scope.fontScale = last.fontScale;
			scope.tags = last.tags;
			return scope;
		}

		return curr;
	}

	public List<MarkdownDraw> renderResult() {
		return CollectionsKt.toList(markdownDraws);
	}

	public static class Scope {
		public @Nullable Prov<MarkdownDraw> drawProvider;
		public @Nullable Scope parent;

		public float paddingLeft, paddingRight, paddingTop, paddingBottom;
		public float marginLeft, marginRight, marginTop, marginBottom;
		public float boundX;
		public boolean fillX;
		public boolean inlineBreak;

		Set<String> tags = new HashSet<>();

		public @Nullable MarkdownDraw scopeDraw;

		public float offsetX, offsetY;

		public float width, height;

		public float currOffsetX, currOffsetY;

		public float rowHeight;

		public Font font;
		public float fontOffsetX, fontOffsetY;
		public boolean fontIsItalic;
		public Color fontColor;
		public float fontScale;

		public Scope(float pl, float pr, float pt, float pb, float ml, float mr, float mt, float mb, float bx, boolean fx, boolean ib) {
			this(null, null, pl, pr, pt, pb, ml, mr, mt, mb, bx, fx, ib);
		}

		public Scope(@Nullable Prov<MarkdownDraw> prov, @Nullable Scope par, float pl, float pr, float pt, float pb, float ml, float mr, float mt, float mb, float bx, boolean fx, boolean ib) {
			drawProvider = prov;
			parent = par;
			paddingLeft = pl;
			paddingRight = pr;
			paddingTop = pt;
			paddingBottom = pb;
			marginLeft = ml;
			marginRight = mr;
			marginTop = mt;
			marginBottom = mb;
			boundX = bx;
			fillX = fx;
			inlineBreak = ib;

			if (prov != null) scopeDraw = prov.get();

			if (par != null) {
				offsetX = par.offsetX;
				offsetY = par.offsetY;

				font = par.font;
				fontOffsetX = par.fontOffsetX;
				fontOffsetY = par.fontOffsetY;
				fontIsItalic = par.fontIsItalic;
				fontColor = par.fontColor;
				fontScale = par.fontScale;
			}

			if (fillX) width = boundX - offsetX;

			currOffsetX = offsetX + marginLeft;
			currOffsetY = offsetY + marginTop;
		}

		public void tags(String... targets) {
			Collections.addAll(tags, targets);
		}

		public boolean hasTag(String tag) {
			return tags.contains(tag);
		}

		public List<String> getTags() {
			return CollectionsKt.toList(tags);
		}

		public void eachAncestral(Cons<Scope> callback) {
			callback.get(this);
			if (parent != null) parent.eachAncestral(callback);
		}

		public @Nullable Scope findAncestral(Boolf<Scope> callback) {
			if (callback.get(this)) return this;
			return parent == null ? null : parent.findAncestral(callback);
		}

		public void scopeDrawTiming(Markdown.DrawTiming timing) {
			if (scopeDraw != null) {
				scopeDraw.drawTiming = timing;
			}
		}

		public void applyFont(Markdown.FontEntry entry) {
			font = entry.fontModifier == null ? parent != null ? parent.font : Fonts.def : entry.fontModifier;
			fontIsItalic = entry.isItalic;
			fontColor = entry.colorModifier == null ? parent != null ? parent.fontColor : Color.clear : entry.colorModifier;
			fontScale = Float.isInfinite(entry.scaleModifier) ? parent != null ? parent.fontScale : 0f : entry.scaleModifier;
		}
	}
}
