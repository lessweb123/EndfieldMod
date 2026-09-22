package endfield.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.Shader;
import arc.struct.Seq;
import endfield.graphics.Shaders2.MaskColorShader;
import endfield.graphics.Shaders2.ScalingShader;
import endfield.graphics.Shaders2.ShaderWrapper;
import endfield.graphics.gl.CaptureBuffer;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.CacheLayer.ShaderLayer;
import mindustry.graphics.Shaders;

import static mindustry.Vars.renderer;

/**
 * Defines the {@linkplain CacheLayer cache layer}s this mod offers.
 *
 * @author LessWeb
 */
public final class CacheLayer2 {
	public static Seq<AtlasPackHandle> handles = new Seq<>(AtlasPackHandle.class);

	public static ShaderLayer brine, glacium, coldPlasma, deepColdPlasma, pit, waterPit;
	public static LiquidUnderFloorLayer shallowSlag;

	/** Don't let anyone instantiate this class. */
	private CacheLayer2() {}

	/** Loads the cache layers. */
	public static void load() {
		brine = new ShaderLayer(Shaders2.brine);
		glacium = new ShaderLayer(Shaders2.glacium);
		coldPlasma = new ShaderLayer(Shaders2.coldPlasma);
		deepColdPlasma = new ShaderLayer(Shaders2.deepColdPlasma);
		pit = new ShaderLayer(Shaders2.pit);
		waterPit = new ShaderLayer(Shaders2.waterPit);
		shallowSlag = new LiquidUnderFloorLayer(Shaders.slag, new Color(0xff8142ff), "molten-slag");

		CacheLayer.add(brine, coldPlasma, deepColdPlasma, pit, waterPit);
		CacheLayer.addLast(shallowSlag);

		handles.add(shallowSlag);
	}

	public static class LiquidUnderFloorLayer extends ShaderLayer implements AtlasPackHandle {
		/** shader for masking the base texture */
		public MaskColorShader maskTex;

		/** shader for scaling the base texture to a tile in size */
		public Shader scalingShader;

		public String textureName;
		/**  */
		public Texture fetcher;

		public CaptureBuffer scalingBuffer = new CaptureBuffer();

		public CaptureBuffer applyLiquidBuffer = new CaptureBuffer();

		public LiquidUnderFloorLayer(Shader shader, Color baseColor) {
			this(shader, baseColor, "white");
		}

		public LiquidUnderFloorLayer(Shader shader, Color targetColor, String baseTexName) {
			super(shader, true);
			this.shader = new ShaderWrapper(shader) {
				@Override
				public void apply() {
					super.apply();
					scalingBuffer.getTexture().bind(0);
				}
			};
			maskTex = new MaskColorShader(targetColor);
			scalingShader = new ScalingShader();
			textureName = baseTexName;
		}

		@Override
		public void getPack() {
			TextureRegion region = Core.atlas.find(textureName);
			Pixmap pix = new Pixmap(region.width, region.height);
			pix.draw(Core.atlas.getPixmap(region));

			fetcher = new Texture(pix);
			fetcher.setFilter(Texture.TextureFilter.linear);
			fetcher.setWrap(Texture.TextureWrap.repeat);

			pix.dispose();
		}

		@Override
		public void end() {
			if (!renderer.animateWater) return;

			//finish capturing floors
			renderer.effectBuffer.end();
			renderer.blocks.floor.beginDraw();

			//scale base floor texture to the block grid
			scalingBuffer.capture();
			Draw.blit(fetcher, scalingShader);
			scalingBuffer.stopCapture();

			//apply liquid distortion to base
			applyLiquidBuffer.capture();
			scalingBuffer.blit(shader);
			applyLiquidBuffer.stopCapture();

			renderer.blocks.floor.beginDraw();

			//use the drawn layer texture as a mask for the liquid texture
			maskTex.maskTex = renderer.effectBuffer.getTexture();
			applyLiquidBuffer.blit(maskTex);

			renderer.blocks.floor.beginDraw();
		}
	}
}
