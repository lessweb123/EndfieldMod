package endfield.ui.markdown.elemdraw;

import arc.Core;
import arc.graphics.g2d.Font;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.pooling.Pools;
import endfield.ui.markdown.Markdown;
import endfield.ui.markdown.RendererContext.Scope;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

public class DrawCodeBlock extends Markdown.MarkdownDraw implements Markdown.ActivityDrawer {
	public Font font = Fonts.def;
	public float fontScl;
	public String code = "";
	public String tag = "";
	public ScrollPane.ScrollPaneStyle sliderStyle = Styles.noBarPane;

	float realWidth;
	float realHeight;

	Label label;
	ScrollPane pane;
	Stack resElem;

	public static DrawCodeBlock get(Font font, float fontScl, String code, String tag, ScrollPane.ScrollPaneStyle sliderStyle) {
		return Pools.obtain(DrawCodeBlock.class, () -> {
			DrawCodeBlock draw = new DrawCodeBlock();
			draw.font = font;
			draw.fontScl = fontScl;
			draw.code = code;
			draw.tag = tag;
			draw.sliderStyle = sliderStyle;
			return draw;
		});
	}

	@Override
	public Element activeElement() {
		return resElem;
	}

	@Override
	public void reset() {
		super.reset();
		font = Fonts.def;
		fontScl = 0f;
		code = "";
		tag = "";
		sliderStyle = Styles.noBarPane;
	}

	@Override
	public float prefWidth() {
		return realWidth;
	}

	@Override
	public float prefHeight() {
		return realHeight;
	}

	@Override
	public void setup(Scope scope) {
		realWidth = scope.boundX - scope.marginRight - offsetX;
		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = font;
		label = new Label(code, labelStyle);
		label.setFontScale(fontScl);
		label.validate();
		realHeight = label.getHeight();

		pane = new ScrollPane(label, sliderStyle);
		pane.setScrollingDisabledY(true);

		resElem = new Stack(pane, new Table(over -> {
			TextButton button = over.top().right().button(Core.bundle.get("editor.copy"), Icon.copySmall, Styles.nonet, () -> {
						Core.app.setClipboardText(code);
						Vars.ui.showInfoFade(Core.bundle.get("copied"));
			}).get();
			button.getLabel().setWrap(false);
			button.color.a = 0.4f;
			button.hovered(() -> button.actions(Actions.alpha(1f, 0.3f)));
			button.exited(() -> button.actions(Actions.alpha(0.4f, 0.3f)));
		}));
		resElem.validate();
	}

	@Override
	public void draw(float x, float y) {}
}
