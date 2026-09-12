package endfield.ui.markdown.elemdraw;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.util.pooling.Pools;
import endfield.ui.markdown.Markdown;
import endfield.ui.markdown.RendererContext.Scope;

public class DrawThematicBreak extends Markdown.MarkdownDraw {
	public Color color = Color.white;
	public float stroke;

	float realWidth;

	public static DrawThematicBreak get(Color color, float stroke) {
		return Pools.obtain(DrawThematicBreak.class, () -> {
			DrawThematicBreak draw = new DrawThematicBreak();
			draw.color = color;
			draw.stroke = stroke;
			return draw;
		});
	}

	@Override
	public void reset() {
		super.reset();
		color = Color.white;
		stroke = 0f;
	}

	@Override
	public float prefWidth() {
		return realWidth;
	}

	@Override
	public float prefHeight() {
		return stroke;
	}

	@Override
	public void setup(Scope scope) {
		realWidth = scope.boundX - scope.marginRight - offsetX;
	}

	@Override
	public void draw(float x, float y) {
		Draw.color(color);
		Fill.rect(x + offsetX + width / 2, y - offsetY - height / 2, width, height);
	}
}
