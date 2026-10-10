package endfield.graphics;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Camera;
import arc.graphics.Texture;
import arc.graphics.g2d.CacheBatch;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.SpriteCache;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.math.Mat;
import arc.util.Log;
import arc.util.OS;
import arc.util.Reflect;
import endfield.Vars2;
import mindustry.Vars;
import mindustry.graphics.MenuRenderer;

public class CustomMenuRenderer extends MenuRenderer {
	static int width = !Vars.mobile ? 100 : 60, height = !Vars.mobile ? 50 : 40;
	static TextureRegion backgroundRegion;

	static Camera camera;
	static Mat mat;
	static FrameBuffer shadows;
	static CacheBatch batch;

	public static void setRegion(TextureRegion region) {
		backgroundRegion = region;
	}

	public static void setPaths(String... splitName) {
		Fi out = Core.settings.getDataDirectory();
		for (String s : splitName) {
			if (!s.isEmpty())
				out = out.child(s);
		}
		setFile(out);
	}

	public static void setFile(Fi file) {
		if (!file.exists() || file.isDirectory()) {
			Log.warn("The image path is invalid: @", file);
			return;
		}

		if (OS.isAndroid) {
			backgroundRegion = new TextureRegion(new Texture(Pixmaps2.load(file)));
		} else {
			backgroundRegion = new TextureRegion(new Texture(file));
		}
	}

	public static void init() {
		try {
			MenuRenderer renderer = Vars2.originalMenuRenderer;

			camera = Reflect.get(renderer, "camera");
			mat = Reflect.get(renderer, "mat");
			shadows = Reflect.get(renderer, "shadows");
			batch = Reflect.get(renderer, "batch");
		} catch (Exception e) {
			camera = new Camera();
			mat = new Mat();
			shadows = new FrameBuffer(width, height);
			batch = Core.batch instanceof CacheBatch cb ? cb : new CacheBatch(new SpriteCache(width * height * 6, false));

			Log.err(e);
		}
	}

	@Override
	public void render() {
		if (backgroundRegion == null || !Core.settings.getBool("override-background", false)) {
			Vars2.originalMenuRenderer.render();
			return;
		}

		float screenWidth = Core.graphics.getWidth();
		float screenHeight = Core.graphics.getHeight();
		float textureWidth = backgroundRegion.width;
		float textureHeight = backgroundRegion.height;

		float scale = Math.max(screenWidth / textureWidth, screenHeight / textureHeight);

		Draw.rect(backgroundRegion, screenWidth / 2f, screenHeight / 2f, textureWidth * scale, textureHeight * scale);

		Draw.reset();
	}

	@Override
	public void dispose() {}
}
