package endfield.ui.markdown.elemdraw;

import arc.func.Cons;
import arc.graphics.g2d.Font;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import endfield.ui.markdown.RendererContext;
import endfield.ui.markdown.RendererContext.Scope;
import endfield.util.Strings2;
import kotlin.text.StringsKt;

import java.util.regex.Pattern;

public final class DrawText2 {
	static final int MAX_SPLITTABLE_WIDTH = 32 * 16;
	static final Pattern wordSplitMatcher = Pattern.compile("[^a-zA-Z0-9_]");

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
				
				void main(){
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
		drawTextWrap(context, str, context.getScope().font, context.getScope().currOffsetX, context.getScope().currOffsetY, context.getScope().fontIsItalic, context.getScope().fontScale);
	}

	public static void drawTextWrap(RendererContext context, String str, Cons<String> doDraw) {
		drawTextWrap(context, str, context.getScope().font, context.getScope().fontScale, doDraw);
	}

	public static void drawTextWrap(RendererContext context, String str, Font font, float offsetX, float offsetY, boolean italic, float scl) {
		drawTextWrap(context, str, font, scl, s -> {
			context.draw(DrawStr.get(s, font, offsetX, offsetY, italic, context.getScope().fontColor, scl));
		});
	}

	public static void drawTextWrap(RendererContext context, String str, Font font, float scl, Cons<String> doDraw) {
		if (context.mdShouldWrap()) {
			Font.FontData data = font.getData();

			int lastIndex = 0;
			int splitIndex = 0;
			float currWidth = 0f;
			float splitWidth = 0f;

			Scope currScope = context.getScope();

			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);

				Font.Glyph glyph = data.getGlyph(c);

				if (wordSplitMatcher.matcher(String.valueOf(c)).matches()) {
					splitIndex = i;
					splitWidth = 0f;
				}

				if (splitWidth + glyph.xadvance > MAX_SPLITTABLE_WIDTH) {
					splitIndex = i;
				}

				if (currWidth + glyph.xadvance * scl > currScope.boundX - currScope.currOffsetX - currScope.marginRight) {
					String appendText = str.substring(lastIndex, splitIndex);
					CharSequence remText = StringsKt.trimStart(str.substring(splitIndex, i));

					doDraw.get(appendText);
					currScope = context.row(Scl.scl(context.mdStyle().linesPadding));

					lastIndex = splitIndex;
					splitIndex = i;
					splitWidth = Strings2.sumOf(remText, it -> data.getGlyph(it).xadvance);
					currWidth = splitWidth * scl;
				}

				currWidth += glyph.xadvance * scl;
				splitWidth += glyph.xadvance;
			}

			if (lastIndex < str.length()) {
				doDraw.get(str.substring(lastIndex));
			}
		} else {
			doDraw.get(str);
		}
	}
}
