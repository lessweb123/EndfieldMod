package endfield.graphics;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.GL30;
import arc.graphics.Gl;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.GLFrameBuffer;
import arc.graphics.gl.Shader;
import endfield.util.FieldAccessor;
import endfield.util.Reflections;
import mindustry.game.EventType.ResizeEvent;

import java.util.Objects;

import static endfield.util.GetKt.sneakyThrow;

public final class ScreenSampler {
	static final FieldAccessor currentBoundFramebuffer;

	static final FrameBuffer swapBuffer = new FrameBuffer();

	private ScreenSampler() {}

	static {
		try {
			currentBoundFramebuffer = Reflections.getFieldAccessor(GLFrameBuffer.class.getDeclaredField("currentBoundFramebuffer"));
		} catch (NoSuchFieldException e) {
			throw sneakyThrow(e);
		}
	}

	public static void setup() {
		swapBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());

		Events.on(ResizeEvent.class, event -> swapBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight()));
	}

	public static void toBuffer(FrameBuffer target) {
		GLFrameBuffer<?> buffer = currentBoundFramebuffer.getObject(null);

		if (buffer != null) {
			blitBuffer(buffer, target);
		} else {
			if (swapBuffer.getWidth() == target.getWidth() && swapBuffer.getHeight() == target.getHeight()) {
				Draw.flush();

				copyPixels(target);
			} else {
				Draw.flush();

				copyPixels(swapBuffer);

				blitBuffer(swapBuffer, target);
			}
		}
	}

	public static void blitShader(Shader shader, int unit) {
		GLFrameBuffer<?> buffer = currentBoundFramebuffer.getObject(null);

		Objects.requireNonNullElse(buffer, swapBuffer).getTexture().bind(unit);
		Draw.blit(shader);
	}

	static void copyPixels(GLFrameBuffer<?> target) {
		if (Core.gl30 != null) {
			Gl.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
			Gl.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.getFramebufferHandle());
			Core.gl30.glReadBuffer(Gl.back);
			Core.gl30.glBlitFramebuffer(
					0, 0, Core.graphics.getWidth(), Core.graphics.getHeight(),
					0, 0, target.getWidth(), target.getHeight(),
					Gl.colorBufferBit, Gl.nearest
			);
			Gl.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
			Gl.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		} else {
			target.getTexture().bind();
			Gl.copyTexImage2D(
					Gl.texture2d, 0,
					Gl.rgba, 0, 0,
					target.getTexture().width, target.getTexture().height,
					0
			);
			Gl.bindTexture(Gl.texture2d, 0);
		}
	}

	static void blitBuffer(GLFrameBuffer<?> source, GLFrameBuffer<?> target) {
		if (Core.gl30 != null) {
			Core.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.getFramebufferHandle());
			Core.gl30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.getFramebufferHandle());
			Core.gl30.glBlitFramebuffer(
					0, 0, source.getWidth(), source.getHeight(),
					0, 0, target.getWidth(), target.getHeight(),
					Gl.colorBufferBit, Gl.nearest
			);
			Core.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
			Core.gl30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		} else {
			target.begin(Color.clear);
			source.getTexture().bind(0);
			Draw.blit(Shaders2.base);
			Gl.bindTexture(Gl.texture2d, 0);
			target.end();
		}
	}

	public static void dispose() {
		swapBuffer.dispose();
	}
}
