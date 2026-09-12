package endfield.ui.markdown.elemdraw;

import arc.graphics.Color;
import arc.graphics.g2d.DistanceFieldFont;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.math.Affine2;
import arc.math.Mat;
import arc.scene.Element;
import arc.scene.ui.TextButton;
import arc.util.pooling.Pools;
import endfield.ui.markdown.Markdown;
import endfield.ui.markdown.RendererContext.Scope;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

import static endfield.ui.markdown.elemdraw.DrawText2.distanceFieldShader;

public class DrawUrl extends Markdown.MarkdownDraw implements Markdown.ActivityDrawer {
	public String text = "";
	public String url = "";
	public Font font = Fonts.def;
	public float fontOffX;
	public float fontOffY;
	public boolean italic;
	public Color color = Color.white;
	public float scl;
	public Color overColor = color;

	TextButton button;

	public static DrawUrl get(String str, String url, Font font, float fontOffsetX, float fontOffsetY, Color color, float scl) {
		return get(str, url, font, fontOffsetX, fontOffsetY, false, color, scl, color);
	}

	public static DrawUrl get(String str, String url, Font font, float fontOffsetX, float fontOffsetY, boolean italic, Color color, float scl, Color overColor) {
		return Pools.obtain(DrawUrl.class, () -> {
			DrawUrl draw = new DrawUrl();
			draw.text = str;
			draw.url = url;
			draw.font = font;
			draw.fontOffX = fontOffsetX;
			draw.fontOffY = fontOffsetY;
			draw.italic = italic;
			draw.color = color;
			draw.scl = scl;
			draw.overColor = overColor;
			return draw;
		});
	}

	@Override
	public Element activeElement() {
		return button;
	}

	@Override
	public float prefWidth() {
		return button.getWidth() + fontOffX * scl;
	}

	@Override
	public float prefHeight() {
		return button.getHeight() + fontOffY * scl;
	}

	@Override
	public void setup(Scope scope) {
		button = new TextButton(text, makeStyle()) {
			final Mat affineTrans = new Mat();
			final Mat trans = new Mat();
			final Affine2 affine2 = new Affine2();

			@Override
			protected void applyTransform(Mat transform) {
				if (italic) {
					super.applyTransform(
							trans.set(transform)
									.mul(affineTrans.set(affine2.idt().shear(0.25f, 0f)))
					);
				} else {
					super.applyTransform(transform);
				}
			}

			@Override
			protected void drawChildren() {
				boolean shouldDistanceField = font instanceof DistanceFieldFont;

				if (shouldDistanceField) {
					Draw.shader(distanceFieldShader);
					distanceFieldShader.bind();
					distanceFieldShader.setUniformf("u_smoothing", 0.5f * font.getScaleX());
				}

				super.drawChildren();

				if (shouldDistanceField) Draw.shader();
			}
		};
	}

	TextButton.TextButtonStyle makeStyle() {
		TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
		style.font = font;
		style.fontColor = color;
		style.overFontColor = overColor;
		style.up = Styles.none;
		return style;
	}

	@Override
	public void draw(float x, float y) {}
}
