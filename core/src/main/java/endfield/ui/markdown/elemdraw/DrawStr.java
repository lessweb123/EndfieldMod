package endfield.ui.markdown.elemdraw;

import arc.graphics.Color;
import arc.graphics.g2d.DistanceFieldFont;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.FontCache;
import arc.graphics.g2d.GlyphLayout;
import arc.math.Affine2;
import arc.math.Mat;
import arc.util.Align;
import arc.util.pooling.Pools;
import endfield.ui.markdown.Markdown;
import endfield.ui.markdown.RendererContext.Scope;
import mindustry.ui.Fonts;

import static endfield.ui.markdown.elemdraw.DrawText2.distanceFieldShader;

public class DrawStr extends Markdown.MarkdownDraw {
	public String text = "";
	public Font font = Fonts.def;
	public boolean italic;
	public float scl;
	public Color color = Color.white;

	boolean isDistanceField;
	FontCache cache;
	GlyphLayout layout;

	Mat lastTrans = new Mat();
	Mat affineTrans = new Mat();
	Mat transform = new Mat();
	Affine2 affine2 = new Affine2();

	public static DrawStr get(String str, Font font, boolean italic, Color color, float scl) {
		return Pools.obtain(DrawStr.class, () -> {
			DrawStr draw = new DrawStr();
			draw.text = str;
			draw.font = font;
			draw.italic = italic;
			draw.scl = scl;
			draw.color = color;
			return draw;
		});
	}

	@Override
	public void reset() {
		super.reset();
		text = "";
		font = Fonts.def;
		italic = false;
		scl = 0f;
		color = Color.white;
	}

	@Override
	public float prefWidth() {
		return layout == null ? 0f : layout.width;
	}

	@Override
	public float prefHeight() {
		return layout == null ? 0f : layout.height;
	}

	@Override
	public void setup(Scope scope) {
		Font.FontData data = font.getData();
		float lastScl = data.scaleX;
		data.setScale(scl * lastScl);
		isDistanceField = font instanceof DistanceFieldFont;
		cache = font.newFontCache();
		layout = cache.setText(
				text,
				0f, 0f,
				0, text.length(),
				0f,
				Align.topLeft,
				false
		);
		data.setScale(lastScl);
	}

	@Override
	public void draw(float x, float y) {
		Font.FontData data = font.data;
		cache.tint(tmp1.set(color).mul(Draw.getColor()));

		boolean shouldDistanceField = isDistanceField;
		float lastSclX = data.scaleX;
		float lastSclY = data.scaleY;
		data.setScale(scl);

		if (shouldDistanceField) {
			Draw.shader(distanceFieldShader);
			distanceFieldShader.bind();
			distanceFieldShader.setUniformf("u_smoothing", 0.5f * font.getScaleX());
		}
		if (italic) {
			Mat last = lastTrans.set(Draw.trans());
			Draw.trans(
					transform.set(last)
							.translate(x + offsetX, y - offsetY)
							.mul(affineTrans.set(affine2.idt().shear(0.25f, 0f)))
			);
			cache.setPosition(0f, 0f);
			cache.draw();
			Draw.trans(last);
		} else {
			cache.setPosition(x + offsetX, y - offsetY);
			cache.draw();
		}
		if (shouldDistanceField) Draw.shader();

		data.setScale(lastSclX, lastSclY);
	}
}
