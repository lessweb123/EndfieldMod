package endfield.graphics;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Camera;
import arc.graphics.Texture;
import arc.graphics.g2d.CacheBatch;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.math.Mat;
import arc.util.Log;
import arc.util.Reflect;
import endfield.Vars2;
import mindustry.graphics.MenuRenderer;

public class CustomMenuRenderer extends MenuRenderer {
	public static TextureRegion backgroundRegion;

	static Camera camera;
	static Mat mat;
	static FrameBuffer shadows;
	static CacheBatch batch;

	public static void setRegion(TextureRegion region) {
		backgroundRegion = region;
	}

	public static void applyPath(String... splitName) {
		Fi out = Core.settings.getDataDirectory();
		for (String s : splitName) {
			if (!s.isEmpty())
				out = out.child(s);
		}
		applyFile(out);
	}

	public static void applyFile(Fi file) {
		if (!file.exists() || file.isDirectory()) {
			Log.warn("The image path is invalid: @", file);
			return;
		}

		backgroundRegion = new TextureRegion(new Texture(Pixmaps2.load(file)));
	}

	public static void init() {
		MenuRenderer renderer = Vars2.originalMenuRenderer;

		camera = Reflect.get(renderer, "camera");
		mat = Reflect.get(renderer, "mat");
		shadows = Reflect.get(renderer, "shadows");
		batch = Reflect.get(renderer, "batch");
	}

	@Override
	public void render() {
		if (backgroundRegion == null || backgroundRegion.texture.isDisposed() || !Core.settings.getBool("override-background", false)) {
			Vars2.originalMenuRenderer.render();
			return;
		}

		float screenWidth = Core.graphics.getWidth();
		float screenHeight = Core.graphics.getHeight();
		float textureWidth = backgroundRegion.width;
		float textureHeight = backgroundRegion.height;

		float scale = Math.max(screenWidth / textureWidth, screenHeight / textureHeight);

		camera.position.set(screenWidth / 2f, screenHeight / 2f);
		camera.resize(screenWidth, screenHeight);

		mat.set(Draw.proj());
		Draw.flush();
		Draw.proj(camera);

		Draw.color();
		Draw.rect(Draw.wrap(shadows.getTexture()), screenWidth / 2f - 4f, screenHeight / 2f - 4f, screenWidth, -screenHeight);
		Draw.flush();

		Draw.rect(backgroundRegion, screenWidth / 2f, screenHeight / 2f, textureWidth * scale, textureHeight * scale);

		Draw.proj(mat);
		Draw.color(0f, 0f, 0f, 0.3f);
		Fill.crect(0, 0, screenWidth, screenHeight);
		Draw.color();
	}

	@Override
	public void dispose() {}
}
