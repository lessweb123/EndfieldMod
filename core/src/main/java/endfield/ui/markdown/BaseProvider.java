package endfield.ui.markdown;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.ClickListener;
import arc.scene.style.BaseDrawable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.io.Streams;
import endfield.ui.markdown.Markdown.Box;
import endfield.ui.markdown.Markdown.FontEntry;
import endfield.ui.markdown.Markdown.Format;
import endfield.ui.markdown.RendererContext.Scope;
import endfield.ui.markdown.elemdraw.DrawCodeBlock;
import endfield.ui.markdown.elemdraw.DrawCurtain;
import endfield.ui.markdown.elemdraw.DrawImg;
import endfield.ui.markdown.elemdraw.DrawStr;
import endfield.ui.markdown.elemdraw.DrawTable;
import endfield.ui.markdown.elemdraw.DrawThematicBreak;
import endfield.ui.markdown.elemdraw.DrawUrl;
import endfield.ui.markdown.extensions.curtain.Curtain;
import endfield.ui.markdown.extensions.curtain.CurtainExtension;
import endfield.ui.markdown.extensions.curtain.CurtainProvider;
import endfield.ui.markdown.extensions.imgattr.ImgAttrExtension;
import endfield.ui.markdown.extensions.ins.InsExtension;
import endfield.ui.markdown.extensions.ins.InsProvider;
import endfield.ui.markdown.extensions.strikethrough.StrikethroughExtension;
import endfield.ui.markdown.extensions.strikethrough.StrikethroughProvider;
import endfield.ui.markdown.extensions.table.CellShadowBox;
import endfield.ui.markdown.extensions.table.TableProvider;
import endfield.ui.markdown.extensions.table.TablesExtension;
import endfield.ui.markdown.url.AtlasHandler;
import endfield.ui.markdown.url.DataHandler;
import endfield.ui.markdown.url.HttpHandler;
import endfield.ui.markdown.url.LocalFileHandler;
import endfield.ui.markdown.url.ResourceHandler;
import endfield.util.FieldAccessor;
import endfield.util.Reflects;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.ext.ins.Ins;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static endfield.ui.markdown.elemdraw.DrawText2.drawTextWrap;

public class BaseProvider implements MarkdownProvider, CurtainProvider, InsProvider, StrikethroughProvider, TableProvider {
	public static final FieldAccessor clickListenerAccessor = Reflects.newFieldAccessor(ClickListener.class, "clickListener");

	@Override
	public List<Extension> extensions() {
		return List.of(
				new ImgAttrExtension(),
				new TablesExtension(),
				new InsExtension(),
				new StrikethroughExtension(),
				new CurtainExtension()
		);
	}

	@Override
	public List<UrlHandler> urlHandlers() {
		return List.of(
				new HttpHandler(),
				new AtlasHandler(),
				new LocalFileHandler(),
				new ResourceHandler(),
				new DataHandler()
		);
	}

	@Override
	public void handleLayoutException(Throwable exception) {
		Log.err("Markdown layout error, detail info: ", exception);
	}

	@Override
	public void add(RendererContext context, Document node) {
		//always create a root scope, set the max width.
		Scope scope = context.insertScope();
		scope.boundX = context.mdWidth();
		scope.applyFont(context.mdStyle().textFont);
		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, Heading node) {
		Box[] headBox = context.mdStyle().headBox;

		Scope scope;

		if (headBox.length > 0) {
			Box box = headBox[Mathf.clamp(node.getLevel() - 1, 0, headBox.length - 1)];
			scope = context.insertScope(box);
		} else {
			scope = context.insertScope();
		}

		FontEntry[] headFonts = context.mdStyle().headFonts;

		if (headFonts.length == 0) throw new IllegalArgumentException("Markdown style must provide least one HeadFonts");

		scope.applyFont(headFonts[Mathf.clamp(node.getLevel() - 1, 0, headFonts.length - 1)]);

		renderChildren(context, node);

		Text firstText = (Text) findDescendants(node, it -> it instanceof Text);

		context.pushChapterEntry(firstText == null ? "capter-" + context.captureCount() : firstText.getLiteral(), scope.offsetX, scope.offsetY, node.getLevel());
	}

	@Override
	public void add(RendererContext context, Paragraph node) {
		renderChildren(context, node);
		context.row(Scl.scl(context.mdStyle().paragraphPadding));
	}

	@Override
	public void add(RendererContext context, BlockQuote node) {
		Scope scope = context.insertScope(context.mdStyle().quoteBox);
		scope.fillX = true;
		renderChildren(context, node);

		context.row(Scl.scl(context.mdStyle().paragraphPadding));
	}

	@Override
	public void add(RendererContext context, Link node) {
		String text = node.getFirstChild() instanceof Text tex ? tex.getLiteral() : "";

		Scope scope = context.insertScope();
		scope.inlineBreak = true;

		scope.applyFont(context.mdStyle().linkFont);
		ClickListener clickListener = new ClickListener();

		drawTextWrap(context, text, s -> {
			DrawUrl draw = DrawUrl.get(
					s, node.getDestination(),
					scope.font, scope.fontOffsetX, scope.fontOffsetY,
					scope.fontIsItalic, scope.fontColor, scope.fontScale,
					context.mdStyle().linkOverColor
			);
			context.draw(draw);
			TextButton elem = (TextButton) draw.activeElement();
			elem.removeListener(elem.getClickListener());
			elem.addListener(clickListener);
			clickListenerAccessor.set(elem, clickListener);
		});
	}

	@Override
	public void add(RendererContext context, Text node) {
		drawTextWrap(context, node.getLiteral());
	}

	@Override
	public void add(RendererContext context, Code node) {
		Scope scope = context.insertScope();
		scope.inlineBreak = true;

		scope.applyFont(context.mdStyle().codeFont);

		drawTextWrap(context, node.getLiteral());
	}

	@Override
	public void add(RendererContext context, BulletList node) {
		Scope scope = context.insertScope();
		AtomicInteger layer = new AtomicInteger();
		scope.eachAncestral(it -> {
			if (it.hasTag("list")) layer.getAndIncrement();
		});

		scope.tags("list");

		eachChildren(node, n -> {
			Drawable[] marks = context.mdStyle().bulletListMarks;
			Drawable head = marks[Mathf.clamp(layer.get(), 0, marks.length - 1)];

			context.insertScope(context.mdStyle().listItemHeadBox);
			context.draw(DrawImg.get(head));

			context.render(n);
		});
	}

	@Override
	public void add(RendererContext context, OrderedList node) {
		Scope scope = context.insertScope();
		AtomicInteger layer = new AtomicInteger();
		scope.eachAncestral(it -> {
			if (it.hasTag("list")) layer.getAndIncrement();
		});

		scope.tags("list");

		AtomicInteger n = new AtomicInteger(node.getMarkerStartNumber());
		eachChildren(node, child -> {
			scope.applyFont(context.mdStyle().listOrderFont);
			Format[] formatters = context.mdStyle().orderedListFormatters;
			Format format = formatters[Mathf.clamp(layer.get(), 0, formatters.length - 1)];

			context.draw(DrawStr.get(
					format.get(n.get()) + ".",
					scope.font,
					scope.fontOffsetX,
					scope.fontOffsetY,
					false,
					scope.fontColor,
					scope.fontScale
			));

			context.render(child);
			n.getAndIncrement();
		});

		context.row(Scl.scl(context.mdStyle().paragraphPadding));
	}

	@Override
	public void add(RendererContext context, ListItem node) {
		context.insertScope();
		renderChildren(context, node);

		context.row(Scl.scl(context.mdStyle().paragraphPadding));
	}

	@Override
	public void add(RendererContext context, IndentedCodeBlock node) {
		Scope scope = context.insertScope(context.mdStyle().codeBlockBox);

		scope.applyFont(context.mdStyle().codeFont);

		context.draw(DrawCodeBlock.get(
				scope.font,
				scope.fontScale,
				node.getLiteral().substring(0, node.getLiteral().length() - 1),
				"",
				context.mdStyle().sliderStyle
		));
	}

	@Override
	public void add(RendererContext context, FencedCodeBlock node) {
		Scope scope = context.insertScope(context.mdStyle().codeBlockBox);

		scope.applyFont(context.mdStyle().codeFont);

		context.draw(DrawCodeBlock.get(
				scope.font,
				scope.fontScale,
				node.getLiteral().substring(0, node.getLiteral().length() - 1),
				node.getInfo(),
				context.mdStyle().sliderStyle
		));
	}

	@Override
	public void add(RendererContext context, ThematicBreak node) {
		context.draw(DrawThematicBreak.get(
				context.mdStyle().lineColor,
				Scl.scl(context.mdStyle().lineStroke)
		));
		context.row(Scl.scl(context.mdStyle().paragraphPadding));
	}

	@Override
	public void add(RendererContext context, Image node) {
		ImageAttributes attributes = (ImageAttributes) findChild(node, it -> it instanceof ImageAttributes);
		String url = node.getDestination();
		Drawable drawable;
		try {
			drawable = context.resolveResource(url, input -> {
				AtomicReference<Drawable> resource = new AtomicReference<>();
				BaseDrawable res = new BaseDrawable() {
					@Override
					public void draw(float x, float y, float width, float height) {
						if (resource.get() != null) resource.get().draw(x, y, width, height);
						else context.mdStyle().loadingImg.draw(x, y, width, height);
					}
				};
				new Thread(() -> {
					try {
						byte[] bytes = input.open().readAllBytes();

						Core.app.post(() -> {
							Pixmap pixmap = new Pixmap(bytes);
							Texture texture = new Texture(pixmap);
							resource.set(new TextureRegionDrawable(new TextureRegion(texture)));
							context.mdInvalidate();
						});
					} catch (Exception e) {
						Core.app.post(() -> context.invalidResource(url));
						Log.err(e);
					} finally {
						Streams.close(input);
					}
				}).start();
				return res;
			});
		} catch (Exception e) {
			drawable = context.mdStyle().loadingImg;
		}

		boolean lastIsSoftBreak = node.getPrevious() instanceof SoftLineBreak;

		if (attributes != null) {
			float width = attributes.getAttributes().containsKey("height") ? Strings.parseFloat(attributes.getAttributes().get("width")) : 0f;
			float height = attributes.getAttributes().containsKey("width") ? Strings.parseFloat(attributes.getAttributes().get("height")) : 0f;

			Scaling scaling = Scaling.stretch;

			for (Scaling s : Scaling.values()) {
				if (s.name().equals(attributes.getAttributes().get("scaling"))) scaling = s;
				break;
			}

			Vec2 size = scaling.apply(drawable.getMinWidth(), drawable.getMinHeight(), width, height);

			Scope scope = context.getScope();
			if (lastIsSoftBreak || scope.currOffsetX + size.x >= scope.boundX - scope.marginRight) {
				context.row(context.mdStyle().linesPadding);
			}

			context.draw(DrawImg.get(drawable, scaling, width, height));
		}
	}

	@Override
	public void add(RendererContext context, Emphasis node) {
		Scope scope = context.insertScope();
		scope.applyFont(context.mdStyle().emFont);

		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, StrongEmphasis node) {
		Scope scope = context.insertScope();
		scope.applyFont(context.mdStyle().emFont);

		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, SoftLineBreak node) {
		drawTextWrap(context, " ");
	}

	@Override
	public void add(RendererContext context, HardLineBreak node) {
		context.row(Scl.scl(context.mdStyle().linesPadding));
	}

	@Override
	public void add(RendererContext context, LinkReferenceDefinition node) {
		context.putVar("link-def-" + node.getLabel(), node.getDestination());
	}

	@Override
	public void add(RendererContext context, Curtain node) {
		Drawable background = context.mdStyle().curtainBox.background;
		Scope scope = context.insertScope(context.mdStyle().curtainBox, background == null ? null : () -> DrawCurtain.get(background));
		scope.inlineBreak = true;

		drawTextWrap(context, node.literal);
	}

	@Override
	public void add(RendererContext context, Ins node) {
		Scope scope = context.insertScope(new Box(context.mdStyle().underLine));
		scope.inlineBreak = true;
		scope.scopeDrawTiming(Markdown.DrawTiming.POST);

		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, Strikethrough node) {
		Scope scope = context.insertScope(new Box(context.mdStyle().strikethrough));
		scope.inlineBreak = true;
		scope.scopeDrawTiming(Markdown.DrawTiming.POST);

		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, TableBlock node) {
		TableBuilder tableBuilder = new TableBuilder(context);
		context.putVar("curr-table-builder", tableBuilder);
		renderChildren(context, node);
		context.invalidVar("curr-table-builder");

		context.draw(DrawTable.get(
				tableBuilder.result(),
				context.mdStyle().sliderStyle
		));

		context.row(Scl.scl(context.mdStyle().paragraphPadding));
	}

	@Override
	public void add(RendererContext context, TableHead node) {
		TableBuilder builder = context.getVar("curr-table-builder", () -> {
			throw new IllegalStateException("Incorrect table structure, no top level available.");
		});
		builder.makeRow();
		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, TableBody node) {
		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, TableRow node) {
		TableBuilder builder = context.getVar("curr-table-builder", () -> {
			throw new IllegalStateException("Incorrect table structure, no top level available.");
		});
		builder.makeRow();
		renderChildren(context, node);
	}

	@Override
	public void add(RendererContext context, TableCell node) {
		TableBuilder builder = context.getVar("curr-table-builder", () -> {
			throw new IllegalStateException("Incorrect table structure, no top level available.");
		});
		builder.pushCell(node);
	}

	@Override
	public void add(RendererContext context, CellShadowBox node) {
		context.insertScope(node.cellBox, null, context.mdWidth(), true, false);
		renderChildren(context, node);
	}

	static class TableBuilder {
		public static Drawable transparent = new BaseDrawable();

		public RendererContext context;

		Table result = new Table();
		int currColumn;

		public TableBuilder(RendererContext c) {
			context = c;
		}

		public Table result() {
			return result;
		}

		public void makeRow() {
			currColumn = 0;
			result.row();
		}

		public void pushCell(TableCell node) {
			Box back1 = context.mdStyle().tableBack1;
			Box back2 = context.mdStyle().tableBack2;

			Box back = currColumn % 2 == 0 ? back1 : back2;

			result.table(back.background == null ? transparent : back.background, t -> {
				t.align(switch (node.getAlignment()) {
					case LEFT -> Align.left;
					case CENTER -> Align.center;
					case RIGHT -> Align.right;
				});
				Markdown markdown = context.createSubMarkdown(new CellShadowBox(node, back));
				markdown.wrapContent = false;
				t.add(markdown);
			}).fill().marginTop(back.paddingTop).marginLeft(back.paddingLeft).marginRight(back.paddingRight).marginBottom(back.paddingBottom);

			currColumn++;
		}
	}
}
