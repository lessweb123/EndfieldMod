package endfield.ui.markdown.elemdraw;

import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.pooling.Pools;
import endfield.ui.markdown.Markdown;
import endfield.ui.markdown.RendererContext.Scope;

public class DrawTable extends Markdown.MarkdownDraw implements Markdown.ActivityDrawer {
	static ScrollPane.ScrollPaneStyle defSlider = new ScrollPane.ScrollPaneStyle();

	public Table table;
	public ScrollPane.ScrollPaneStyle sliderStyle = defSlider;

	ScrollPane pane;
	float tableWidth;
	float tableHeight;

	public static DrawTable get(Table table, ScrollPane.ScrollPaneStyle sliderStyle) {
		return Pools.obtain(DrawTable.class, () -> {
			DrawTable draw = new DrawTable();
			draw.table = table;
			draw.sliderStyle = sliderStyle;
			return draw;
		});
	}

	@Override
	public Element activeElement() {
		return pane;
	}

	@Override
	public float prefWidth() {
		return tableWidth;
	}

	@Override
	public float prefHeight() {
		return tableHeight;
	}

	@Override
	public void setup(Scope scope) {
		float maxWidth = scope.boundX - scope.marginRight - offsetX;

		pane = new ScrollPane(table, sliderStyle);
		pane.setScrollingDisabledY(true);
		pane.pack();

		tableWidth = Math.min(pane.getWidth(), maxWidth);
		tableHeight = pane.getHeight();
	}

	@Override
	public void draw(float x, float y) {}
}
