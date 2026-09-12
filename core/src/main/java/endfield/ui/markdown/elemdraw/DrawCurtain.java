package endfield.ui.markdown.elemdraw;

import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.util.pooling.Pools;
import endfield.ui.markdown.Markdown;
import endfield.ui.markdown.RendererContext.Scope;
import mindustry.gen.Tex;

public class DrawCurtain extends Markdown.MarkdownDraw implements Markdown.ActivityDrawer {
	public Drawable curtainDraw = Tex.nomap;

	CurtainElem curtain;

	public static DrawCurtain get(Drawable drawable) {
		return Pools.obtain(DrawCurtain.class, () -> {
			DrawCurtain draw = new DrawCurtain();
			draw.curtainDraw = drawable;
			return draw;
		});
	}

	@Override
	public Element activeElement() {
		return curtain;
	}

	@Override
	public float prefWidth() {
		return curtain.getPrefWidth();
	}

	@Override
	public float prefHeight() {
		return curtain.getPrefHeight();
	}

	@Override
	public void setup(Scope scope) {
		curtain = new CurtainElem(curtainDraw);
		curtain.touchable = Touchable.enabled;
	}

	@Override
	public void draw(float x, float y) {
		curtainDraw.draw(x + offsetX, y - offsetY - height, width, height);
	}

	public static class CurtainElem extends Image {
		ClickListener clickListener = new ClickListener();

		public CurtainElem(Drawable drawable) {
			super(drawable);
			addListener(clickListener);
			update(() -> color.a = clickListener.isOver() || clickListener.isPressed() ? 0f : 1f);
		}

		public void setClickListener(ClickListener listener) {
			removeListener(clickListener);
			clickListener = listener;
			addListener(listener);
		}
	}
}
