package endfield.ui.markdown.elemdraw;

import arc.math.geom.Vec2;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Scl;
import arc.util.Scaling;
import arc.util.pooling.Pools;
import endfield.ui.markdown.Markdown.MarkdownDraw;
import endfield.ui.markdown.RendererContext.Scope;
import mindustry.gen.Tex;

public class DrawImg extends MarkdownDraw {
	public Drawable drawable = Tex.nomap;
	public Scaling scaling = Scaling.none;
	public float widthModifier;
	public float heightModifier;

	float realWidth;
	float realHeight;

	public static DrawImg get(Drawable drawable) {
		return get(drawable, Scaling.none, 0f, 0f);
	}

	public static DrawImg get(Drawable drawable, Scaling scaling, float widthModifier, float heightModifier) {
		return Pools.obtain(DrawImg.class, () -> {
			DrawImg drawImg = new DrawImg();
			drawImg.drawable = drawable;
			drawImg.scaling = scaling;
			drawImg.widthModifier = widthModifier;
			drawImg.heightModifier = heightModifier;
			return drawImg;
		});
	}

	@Override
	public void reset() {
		super.reset();
		drawable = Tex.nomap;
		scaling = Scaling.none;
		widthModifier = 0f;
		heightModifier = 0f;
	}

	@Override
	public float prefWidth() {
		return Scl.scl(realWidth);
	}

	@Override
	public float prefHeight() {
		return Scl.scl(realHeight);
	}

	@Override
	public void setup(Scope scope) {
		float drawableWidth = drawable.getMinWidth() + drawable.getLeftWidth() + drawable.getRightWidth();
		float drawableHeight = drawable.getMinHeight() + drawable.getTopHeight() + drawable.getBottomHeight();
		if (widthModifier > 0 || heightModifier > 0) {
			try {
				Vec2 res = scaling.apply(
						drawableWidth,
						drawableHeight,
						widthModifier,
						heightModifier
				);

				realWidth = res.x;
				realHeight = res.y;
			} catch (Exception e) {
				realWidth = drawableWidth;
				realHeight = drawableHeight;
			}
		} else {
			realWidth = drawableWidth;
			realHeight = drawableHeight;
		}
	}

	@Override
	public void draw(float x, float y) {
		drawable.draw(x + offsetX, y - offsetY - height, width, height);
	}
}
