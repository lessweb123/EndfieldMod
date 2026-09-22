package endfield.ui.markdown.elemdraw;

import arc.Core;
import arc.func.Cons;
import arc.graphics.g2d.Font;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import endfield.ui.markdown.RendererContext;
import endfield.ui.markdown.RendererContext.Scope;
import endfield.util.Strings2;
import kotlin.text.StringsKt;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.regex.Pattern;

public final class DrawText2 {
	static final int MAX_SPLITTABLE_WIDTH = 32 * 16;

	static Shader distanceFieldShader = createDistanceFieldShader();

	private DrawText2() {}

	static Shader createDistanceFieldShader() {
		return new Shader("""
				attribute vec4 a_position;
				attribute vec4 a_color;
				attribute vec2 a_texCoord0;
				uniform mat4 u_projTrans;
				varying vec4 v_color;
				varying vec2 v_texCoords;
				
				void main() {
					v_color = a_color;
					v_color.a = v_color.a * (255.0 / 254.0);
					v_texCoords = a_texCoord0;
					gl_Position =  u_projTrans * a_position;
				}
				""", """
				uniform sampler2D u_texture;
				uniform float u_smoothing;
				varying vec4 v_color;
				varying vec2 v_texCoords;
				
				void main() {
					if (u_smoothing > 0.0) {
						float smoothing = 0.25 / u_smoothing;
						vec4 color = texture2D(u_texture, v_texCoords);
						float distance = color.a;
						float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
						gl_FragColor = vec4(v_color.rgb*color.rgb, alpha * v_color.a);
					} else {
						gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
					}
				}
				""");
	}

	public static void drawTextWrap(RendererContext context, String str) {
		drawTextWrap(context, str, context.getScope().font, context.getScope().fontIsItalic, context.getScope().fontScale);
	}

	public static void drawTextWrap(RendererContext context, String str, Cons<String> doDraw) {
		drawTextWrap(context, str, context.getScope().font, context.getScope().fontScale, doDraw);
	}

	public static void drawTextWrap(RendererContext context, String str, Font font, boolean italic, float scl) {
		drawTextWrap(context, str, font, scl, s -> {
			context.draw(DrawStr.get(s, font, italic, context.getScope().fontColor, scl));
		});
	}

	public static void drawTextWrap(RendererContext context, String str, Font font, float scl, Cons<String> doDraw) {
		if (context.mdShouldWrap()) {
			Font.FontData data = font.getData();

			int lastIndex = 0;
			float currWidth = 0f;

			Scope currScope = context.getScope();

			Locale locale = Locale.getDefault();

			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);

				Font.Glyph glyph = data.getGlyph(c);

				if (currWidth + glyph.xadvance * scl > availWidth(currScope)) {
					int boundary = findBreakBoundary(str, lastIndex, i, locale);
					int breakIdx = boundary <= lastIndex ? i : boundary;

					String appendText = str.substring(lastIndex, breakIdx);
					CharSequence remText = StringsKt.trimStart(str.substring(breakIdx, i));

					doDraw.get(appendText);
					currScope = context.row(Scl.scl(context.mdStyle().linesPadding));

					lastIndex = breakIdx;

					var remWidth = 0f;
					for (int j = 0; j < remText.length(); j++) {
						char ch = remText.charAt(j);
						remWidth += data.getGlyph(ch).xadvance;
					}
					currWidth = remWidth * scl;
				}

				currWidth += glyph.xadvance * scl;
			}

			if (lastIndex < str.length()) {
				doDraw.get(str.substring(lastIndex));
			}
		} else {
			doDraw.get(str);
		}
	}

	static float availWidth(Scope currScope) {
		return currScope.boundX - currScope.currOffsetX - currScope.marginRight;
	}

	static int findBreakBoundary(String text, int fromIndex, int toIndex) {
		return findBreakBoundary(text, fromIndex, toIndex, Core.bundle.getLocale());
	}

	static int findBreakBoundary(String text, int fromIndex, int toIndex, Locale locale) {
		if (fromIndex >= toIndex) return -1;
		BreakIterator it = BreakIterator.getLineInstance(locale);
		it.setText(text);
		int lastBoundary = -1;
		int b = it.first();
		while (b != BreakIterator.DONE && b <= toIndex) {
			if (b > fromIndex) lastBoundary = b;
			b = it.next();
		}
		return lastBoundary;
	}
}
