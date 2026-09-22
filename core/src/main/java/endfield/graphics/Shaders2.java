package endfield.graphics;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.Texture.TextureWrap;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.g3d.Camera3D;
import arc.graphics.gl.Shader;
import arc.math.Mat;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import arc.util.Tmp;
import endfield.Vars2;
import endfield.type.AtmospherePlanet;
import mindustry.Vars;
import mindustry.type.Planet;

import java.nio.FloatBuffer;

import static endfield.Vars2.MOD_NAME;

/**
 * Defines the {@linkplain Shader shader}s this mod offers.
 *
 * @author LessWeb
 */
public final class Shaders2 {
	public static Fi shadersDir = Vars2.internalTree.child("shaders");

	public static DepthShader depth;
	public static DepthAtmosphereShader depthAtmosphere;
	public static AlphaShader alphaShader;
	public static SurfaceShader2 brine, glacium, coldPlasma, deepColdPlasma, boundWater, pit, waterPit, shallowSlag;
	public static AberrationShader aberration;
	public static MaskShader alphaMask;
	public static WaveShader wave;
	public static MirrorFieldShader mirrorField;
	public static MaterializeShader materialize;
	public static VerticalBuildShader vertBuild;
	public static BlockBuildCenterShader blockBuildCenter;
	public static TractorConeShader tractorCone;
	public static DimShader dimShader;
	public static SmallSpaceShader smallSpaceShader;
	public static Shader simpleScreen, base, passThrough, distance, blur;
	public static AlphaAdjust linearAlpha, lerpAlpha;
	public static TilerShader tiler;
	public static PlanetTextureShader planetTexture;

	/** Don't let anyone instantiate this class. */
	private Shaders2() {}

	/** Loads the shaders. */
	public static void load() {
		String prevVert = Shader.prependVertexCode, prevFrag = Shader.prependFragmentCode;
		Shader.prependVertexCode = Shader.prependFragmentCode = "";

		depth = new DepthShader();
		depthAtmosphere = new DepthAtmosphereShader();

		alphaShader = new AlphaShader();

		brine = new SurfaceShader2(shadersDir.child("general-highp.vert"), shadersDir.child("brine.frag"));
		glacium = new SurfaceShader2(shadersDir.child("general-highp.vert"), shadersDir.child("glacium.frag"));
		coldPlasma = new SurfaceShader2(shadersDir.child("general-highp.vert"), shadersDir.child("cold-plasma.frag"));
		deepColdPlasma = new DualSurfaceShader(shadersDir.child("general-highp.vert"), shadersDir.child("deep-cold-plasma.frag")) {
			@Override
			public Texture noise() {
				return Textures2.smooth;
			}

			@Override
			public Texture noise2() {
				return Textures2.darker;
			}
		};
		boundWater = new SurfaceShader2(shadersDir.child("general-highp.vert"), shadersDir.child("bound-water.frag"));
		pit = new PitShader(shadersDir.child("general-highp.vert"), shadersDir.child("pit.frag"), MOD_NAME + "-concrete-blank1", MOD_NAME + "-stone-variants", MOD_NAME + "-truss");
		waterPit = new PitShader(shadersDir.child("general-highp.vert"), shadersDir.child("water-pit.frag"), MOD_NAME + "-concrete-blank1", MOD_NAME + "-stone-variants", MOD_NAME + "-truss");
		shallowSlag = new SurfaceShader2(shadersDir.child("general-highp.vert"), shadersDir.child("shallow-slag.frag"));

		aberration = new AberrationShader();

		alphaMask = new MaskShader();
		mirrorField = new MirrorFieldShader();
		wave = new WaveShader();

		materialize = new MaterializeShader();
		vertBuild = new VerticalBuildShader();
		blockBuildCenter = new BlockBuildCenterShader();
		tractorCone = new TractorConeShader();
		dimShader = new DimShader();
		smallSpaceShader = new SmallSpaceShader();

		simpleScreen = new Shader(shadersDir.child("general.vert"), shadersDir.child("simple.frag"));
		base = new Shader(shadersDir.child("general.vert"), shadersDir.child("dist-base.frag"));
		passThrough = new Shader(shadersDir.child("general-highp.vert"), shadersDir.child("pass-through.frag"));
		distance = new Shader(shadersDir.child("distance.vert"), shadersDir.child("distance.frag"));
		blur = new Shader(shadersDir.child("general.vert"), shadersDir.child("gauss-blur.frag"));

		linearAlpha = new AlphaAdjust(shadersDir.child("linear-alpha-adjust.frag"));
		lerpAlpha = new AlphaAdjust(shadersDir.child("lerp-alpha-adjust.frag"));

		tiler = new TilerShader();

		planetTexture = new PlanetTextureShader();

		Shader.prependVertexCode = prevVert;
		Shader.prependFragmentCode = prevFrag;
	}

	public static void dispose() {
		brine.dispose();
		coldPlasma.dispose();
		deepColdPlasma.dispose();
		boundWater.dispose();
		pit.dispose();
		waterPit.dispose();
	}

	/**
	 * Resolves shader files from this mod via {@link Vars#tree}.
	 *
	 * @param name The shader file name, e.g. {@code my-shader.frag}.
	 * @return The shader file, located inside {@code shaders/}.
	 */
	public static Fi frag(String name) {
		return shadersDir.child(name + ".frag");
	}

	public static Fi vert(String name) {
		return shadersDir.child(name + ".vert");
	}

	/** Specialized mesh shader to capture fragment depths. */
	public static class DepthShader extends Shader {
		public Camera3D camera;

		/** The only instance of this class: {@link #depth}. */
		DepthShader() {
			super(shadersDir.child("depth.vert"), shadersDir.child("depth.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_camPos", camera.position);
			setUniformf("u_camRange", camera.near, camera.far - camera.near);
		}
	}

	/**
	 * An atmosphere shader that incorporates the planet shape in a form of depth texture. Better quality, but at the little
	 * cost of performance.
	 */
	public static class DepthAtmosphereShader extends Shader {
		static final Mat3D mat = new Mat3D();

		public Camera3D camera;
		public AtmospherePlanet planet;

		/** The only instance of this class: {@link #depthAtmosphere}. */
		DepthAtmosphereShader() {
			super(shadersDir.child("depth-atmosphere.vert"), shadersDir.child("depth-atmosphere.frag"));
		}

		@Override
		public void apply() {
			setUniformMatrix4("u_proj", camera.combined.val);
			setUniformMatrix4("u_trans", planet.getTransform(mat).val);

			setUniformf("u_camPos", camera.position);
			setUniformf("u_relCamPos", Tmp.v31.set(camera.position).sub(planet.position));
			setUniformf("u_camRange", camera.near, camera.far - camera.near);
			setUniformf("u_center", planet.position);
			setUniformf("u_light", planet.getLightNormal());
			setUniformf("u_color", planet.atmosphereColor.r, planet.atmosphereColor.g, planet.atmosphereColor.b);

			setUniformf("u_innerRadius", planet.radius + planet.atmosphereRadIn);
			setUniformf("u_outerRadius", planet.radius + planet.atmosphereRadOut);

			planet.buffer.getTexture().bind(0);
			setUniformi("u_topology", 0);
			setUniformf("u_viewport", Core.graphics.getWidth(), Core.graphics.getHeight());
		}
	}

	public static class PlanetTextureShader extends Shader {
		public Vec3 lightDir = new Vec3(1, 1, 1).nor();
		public Color ambientColor = Color.white.cpy();
		public Vec3 camDir = new Vec3();
		public float alpha = 1f;
		public Planet planet;

		/** The only instance of this class: {@link #planetTexture}. */
		PlanetTextureShader() {
			super(shadersDir.child("circle-mesh.vert"), shadersDir.child("circle-mesh.frag"));
		}

		@Override
		public void apply() {
			camDir.set(Vars.renderer.planets.cam.direction).rotate(Vec3.Y, planet.getRotation());

			setUniformf("u_alpha", alpha);
			setUniformf("u_lightdir", lightDir);
			setUniformf("u_ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
			setPlanetInfo("u_sun_info", planet.solarSystem);
			setPlanetInfo("u_planet_info", planet);
			setUniformf("u_camdir", camDir);
			setUniformf("u_campos", Vars.renderer.planets.cam.position);
		}

		void setPlanetInfo(String name, Planet planet) {
			Vec3 position = planet.position;
			setUniformf(name, position.x, position.y, position.z, planet.radius);
		}
	}

	public static class TilerShader extends Shader {
		public Texture texture = Core.atlas.white().texture;
		public float scl = 4f;

		/** The only instance of this class: {@link #tiler}. */
		TilerShader() {
			super(shadersDir.child("general-highp.vert"), shadersDir.child("tiler.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_offset", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_texsize", Core.camera.width, Core.camera.height);
			setUniformf("u_tiletexsize", texture.width / scl, texture.height / scl);

			texture.bind(1);
			Vars.renderer.effectBuffer.getTexture().bind(0);

			setUniformi("u_tiletex", 1);
		}
	}

	public static class AlphaShader extends Shader {
		public float alpha = 1f;

		/** The only instance of this class: {@link #alphaShader}. */
		AlphaShader() {
			super(shadersDir.child("general-highp.vert"), shadersDir.child("post-alpha.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_alpha", alpha);
		}
	}

	public static class WaveShader extends Shader {
		public Color waveMix = Color.white;
		public float mixAlpha = 0.4f;
		public float mixOmiga = 0.75f;
		public float maxThreshold = 0.9f;
		public float minThreshold = 0.6f;
		public float waveScl = 0.2f;

		/** The only instance of this class: {@link #wave}. */
		WaveShader() {
			super(shadersDir.child("general-highp.vert"), shadersDir.child("wave.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);
			setUniformf("u_time", Time.time);

			setUniformf("mix_color", waveMix);
			setUniformf("mix_alpha", mixAlpha);
			setUniformf("mix_omiga", mixOmiga);
			setUniformf("wave_scl", waveScl);
			setUniformf("max_threshold", maxThreshold);
			setUniformf("min_threshold", minThreshold);
		}
	}

	public static class MirrorFieldShader extends Shader {
		public Color waveMix = Color.white;
		public Vec2 offset = new Vec2(0f, 0f);
		public float stroke = 2;
		public float gridStroke = 0.8f;
		public float mixAlpha = 0.4f;
		public float alpha = 0.2f;
		public float maxThreshold = 1;
		public float minThreshold = 0.7f;
		public float waveScl = 0.03f;
		public float sideLen = 10;

		/** The only instance of this class: {@link #mirrorField}. */
		MirrorFieldShader() {
			super(shadersDir.child("general-highp.vert"), shadersDir.child("mirror-field.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);
			setUniformf("u_time", Time.time);

			setUniformf("offset", offset);
			setUniformf("u_step", stroke);
			setUniformf("mix_color", waveMix);
			setUniformf("mix_alpha", mixAlpha);
			setUniformf("u_stroke", gridStroke);
			setUniformf("u_alpha", alpha);
			setUniformf("wave_scl", waveScl);
			setUniformf("max_threshold", maxThreshold);
			setUniformf("min_threshold", minThreshold);
			setUniformf("side_len", sideLen);
		}
	}

	public static final class MaskShader extends Shader {
		public Texture texture;

		/** The only instance of this class: {@link #alphaMask}. */
		MaskShader() {
			super(shadersDir.child("general.vert"), shadersDir.child("alpha-mask.frag"));
		}

		@Override
		public void apply() {
			setUniformi("u_texture", 1);
			setUniformi("u_mask", 0);

			texture.bind(1);
		}
	}

	public static class AlphaAdjust extends Shader {
		public float coef;

		public AlphaAdjust(Fi fragmentShader) {
			super(shadersDir.child("general.vert"), fragmentShader);
		}

		@Override
		public void apply() {
			setUniformf("u_coef", coef);
		}
	}

	public static final class AberrationShader extends Shader {
		AberrationShader() {
			super(shadersDir.child("general.vert"), shadersDir.child("aberration.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_dp", Scl.scl(1f));
			setUniformf("u_time", Time.time / Scl.scl(1f));
			setUniformf("u_offset",
					Core.camera.position.x - Core.camera.width / 2,
					Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_texsize", Core.camera.width, Core.camera.height);
		}
	}

	public static class MaterializeShader extends Shader {
		public float progress, offset, time;
		public int shadow;
		public Color color = new Color();
		public TextureRegion region;

		MaterializeShader() {
			super(shadersDir.child("materialize.vert"), shadersDir.child("materialize.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_progress", progress);
			setUniformf("u_offset", offset);
			setUniformf("u_time", time);
			setUniformf("u_width", region.width);
			setUniformf("u_shadow", shadow);
			setUniformf("u_color", color);
			setUniformf("u_uv", region.u, region.v);
			setUniformf("u_uv2", region.u2, region.v2);
			setUniformf("u_texsize", region.texture.width, region.texture.height);
		}
	}

	public static class VerticalBuildShader extends Shader {
		public float progress, time;
		public Color color = new Color();
		public TextureRegion region;

		VerticalBuildShader() {
			super(shadersDir.child("vertical-build.vert"), shadersDir.child("vertical-build.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_time", time);
			setUniformf("u_color", color);
			setUniformf("u_progress", progress);
			setUniformf("u_uv", region.u, region.v);
			setUniformf("u_uv2", region.u2, region.v2);
			setUniformf("u_texsize", region.texture.width, region.texture.height);
		}
	}

	public static class BlockBuildCenterShader extends Shader {
		public float progress;
		public TextureRegion region;
		public float time;

		BlockBuildCenterShader() {
			super(shadersDir.child("block-build-center.vert"), shadersDir.child("block-build-center.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_progress", progress);
			setUniformf("u_uv", region.u, region.v);
			setUniformf("u_uv2", region.u2, region.v2);
			setUniformf("u_time", time);
			setUniformf("u_texsize", region.texture.width, region.texture.height);
		}
	}

	public static class TractorConeShader extends Shader {
		public float cx, cy;
		public float time, spacing, thickness;

		TractorConeShader() {
			super(shadersDir.child("general-highp.vert"), shadersDir.child("tractor-cone.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_dp", Scl.scl(1f));
			setUniformf("u_time", time / Scl.scl(1f));
			setUniformf("u_offset",
					Core.camera.position.x - Core.camera.width / 2,
					Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_texsize", Core.camera.width, Core.camera.height);

			setUniformf("u_spacing", spacing / Scl.scl(1f));
			setUniformf("u_thickness", thickness / Scl.scl(1f));
			setUniformf("u_cx", cx / Scl.scl(1f));
			setUniformf("u_cy", cy / Scl.scl(1f));
		}

		public void setCenter(float x, float y) {
			cx = x;
			cy = y;
		}
	}

	public static class DimShader extends Shader {
		public float alpha;

		DimShader() {
			super(shadersDir.child("general.vert"), shadersDir.child("dim.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_alpha", alpha);
		}
	}

	public static class SmallSpaceShader extends Shader {
		Texture texture;

		SmallSpaceShader() {
			super(shadersDir.child("general-highp.vert"), shadersDir.child("small-space.frag"));
		}

		@Override
		public void apply() {
			if (texture == null) {
				texture = new Texture(new Pixmap(Textures2.texturesDir.child("small-space.png")));
				texture.setFilter(TextureFilter.linear);
				texture.setWrap(TextureWrap.repeat);
			}

			setUniformf("u_campos", Core.camera.position.x, Core.camera.position.y);
			setUniformf("u_ccampos", Core.camera.position);
			setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
			setUniformf("u_time", Time.time);

			texture.bind(1);
			Vars.renderer.effectBuffer.getTexture().bind(0);

			setUniformi("u_stars", 1);
		}
	}

	/** SurfaceShader but uses a mod fragment asset. */
	public static class PitShader extends SurfaceShader2 {
		protected TextureRegion topLayer, bottomLayer, truss;
		protected String topLayerName, bottomLayerName, trussName;

		public PitShader(Fi vertex, Fi fragment, String topLayer, String bottomLayer, String truss) {
			super(vertex, fragment);
			topLayerName = topLayer;
			bottomLayerName = bottomLayer;
			trussName = truss;
		}

		@Override
		public void apply() {
			Texture texture = Core.atlas.find("grass1").texture;
			if (topLayer == null)
				topLayer = Core.atlas.find(topLayerName);

			if (bottomLayer == null)
				bottomLayer = Core.atlas.find(bottomLayerName);

			if (truss == null)
				truss = Core.atlas.find(trussName);

			if (noiseTex == null)
				noiseTex = noise();

			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);
			setUniformf("u_time", Time.time);

			setUniformf("u_toplayer", topLayer.u, topLayer.v, topLayer.u2, topLayer.v2);
			setUniformf("u_bottomlayer", bottomLayer.u, bottomLayer.v, bottomLayer.u2, bottomLayer.v2);
			setUniformf("bvariants", bottomLayer.width / 32f);
			setUniformf("u_truss", truss.u, truss.v, truss.u2, truss.v2);

			texture.bind(2);
			noiseTex.bind(1);
			Vars.renderer.effectBuffer.getTexture().bind(0);
			setUniformi("u_noise", 1);
			setUniformi("u_texture2", 2);
		}
	}

	public static class DualSurfaceShader extends SurfaceShader2 {
		protected Texture noiseTex2;

		public DualSurfaceShader(Fi vertex, Fi fragment) {
			super(vertex, fragment);
		}

		public DualSurfaceShader(String vertex, String fragment) {
			super(vertex, fragment);
		}

		public Texture noise2() {
			return Textures2.noise;
		}

		@Override
		public void apply() {
			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);
			setUniformf("u_time", Time.time);

			if (hasUniform("u_noise")) {
				if (noiseTex == null) {
					noiseTex = noise();
				}

				noiseTex.bind(1);
				Vars.renderer.effectBuffer.getTexture().bind(0);

				setUniformi("u_noise", 1);
			}

			if (hasUniform("u_noise_2")) {
				if (noiseTex2 == null) {
					noiseTex2 = noise2();
				}

				noiseTex2.bind(1);
				Vars.renderer.effectBuffer.getTexture().bind(0);

				setUniformi("u_noise_2", 1);
			}
		}
	}

	public static class SurfaceShader2 extends Shader {
		protected Texture noiseTex;

		public SurfaceShader2(Fi vertex, Fi fragment) {
			super(vertex, fragment);
		}

		public SurfaceShader2(String vertex, String fragment) {
			super(vertex, fragment);
		}

		public Texture noise() {
			return Textures2.noise;
		}

		@Override
		public void apply() {
			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);
			setUniformf("u_time", Time.time);

			if (hasUniform("u_noise")) {
				if (noiseTex == null) {
					noiseTex = noise();
				}

				noiseTex.bind(1);
				Vars.renderer.effectBuffer.getTexture().bind(0);

				setUniformi("u_noise", 1);
			}
		}
	}

	public static class MaskColorShader extends Shader {
		Color targetColor;

		public Texture maskTex;

		public MaskColorShader() {
			this(Color.black);
		}

		public MaskColorShader(Color targetColor) {
			super(shadersDir.child("general.vert"), shadersDir.child("mask-base-texture.frag"));
			this.targetColor = targetColor;
		}

		@Override
		public void apply() {
			setUniformf("u_target_color", targetColor.r, targetColor.g, targetColor.b, targetColor.a);

			maskTex.bind(1);
			setUniformi("u_texture_mask", 1);

		}

		@Override
		public void dispose() {
			super.dispose();
			maskTex.dispose();
		}
	}

	public static class ScalingShader extends Shader {
		public ScalingShader() {
			super(Core.files.internal("shaders/screenspace.vert"), shadersDir.child("tile-tex-scaler.frag"));
		}

		@Override
		public void apply() {
			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);

		}
	}

	public static class ShaderWrapper extends Shader {
		public Shader wrapped;

		public ShaderWrapper(Shader wrap) {
			super(Core.files.internal("shaders/screenspace.vert"), Core.files.internal("shaders/screenspace.frag"));
			wrapped = wrap;
		}

		@Override
		public void apply() {
			wrapped.apply();
		}

		@Override
		public String getLog() {
			return wrapped.getLog();
		}

		@Override
		public boolean isCompiled() {
			return wrapped == null || wrapped.isCompiled();
		}

		@Override
		public int fetchUniformLocation(String name, boolean pedantic) {
			return wrapped.fetchUniformLocation(name, pedantic);
		}

		@Override
		public void setUniformi(String name, int value) {
			wrapped.setUniformi(name, value);
		}

		@Override
		public void setUniformi(int location, int value) {
			wrapped.setUniformi(location, value);
		}

		@Override
		public void setUniformi(String name, int value1, int value2) {
			wrapped.setUniformi(name, value1, value2);
		}

		@Override
		public void setUniformi(int location, int value1, int value2) {
			wrapped.setUniformi(location, value1, value2);
		}

		@Override
		public void setUniformi(String name, int value1, int value2, int value3) {
			wrapped.setUniformi(name, value1, value2, value3);
		}

		@Override
		public void setUniformi(int location, int value1, int value2, int value3) {
			wrapped.setUniformi(location, value1, value2, value3);
		}

		@Override
		public void setUniformi(String name, int value1, int value2, int value3, int value4) {
			wrapped.setUniformi(name, value1, value2, value3, value4);
		}

		@Override
		public void setUniformi(int location, int value1, int value2, int value3, int value4) {
			wrapped.setUniformi(location, value1, value2, value3, value4);
		}

		@Override
		public void setUniformf(String name, float value) {
			wrapped.setUniformf(name, value);
		}

		@Override
		public void setUniformf(int location, float value) {
			wrapped.setUniformf(location, value);
		}

		@Override
		public void setUniformf(String name, float value1, float value2) {
			wrapped.setUniformf(name, value1, value2);
		}

		@Override
		public void setUniformf(int location, float value1, float value2) {
			wrapped.setUniformf(location, value1, value2);
		}

		@Override
		public void setUniformf(String name, float value1, float value2, float value3) {
			wrapped.setUniformf(name, value1, value2, value3);
		}

		@Override
		public void setUniformf(int location, float value1, float value2, float value3) {
			wrapped.setUniformf(location, value1, value2, value3);
		}

		@Override
		public void setUniformf(String name, float value1, float value2, float value3, float value4) {
			wrapped.setUniformf(name, value1, value2, value3, value4);
		}

		@Override
		public void setUniformf(int location, float value1, float value2, float value3, float value4) {
			wrapped.setUniformf(location, value1, value2, value3, value4);
		}

		@Override
		public void setUniform1fv(String name, float[] values, int offset, int length) {
			wrapped.setUniform1fv(name, values, offset, length);
		}

		@Override
		public void setUniform1fv(int location, float[] values, int offset, int length) {
			wrapped.setUniform1fv(location, values, offset, length);
		}

		@Override
		public void setUniform2fv(String name, float[] values, int offset, int length) {
			wrapped.setUniform2fv(name, values, offset, length);
		}

		@Override
		public void setUniform2fv(int location, float[] values, int offset, int length) {
			wrapped.setUniform2fv(location, values, offset, length);
		}

		@Override
		public void setUniform3fv(String name, float[] values, int offset, int length) {
			wrapped.setUniform3fv(name, values, offset, length);
		}

		@Override
		public void setUniform3fv(int location, float[] values, int offset, int length) {
			wrapped.setUniform3fv(location, values, offset, length);
		}

		@Override
		public void setUniform4fv(String name, float[] values, int offset, int length) {
			wrapped.setUniform4fv(name, values, offset, length);
		}

		@Override
		public void setUniform4fv(int location, float[] values, int offset, int length) {
			wrapped.setUniform4fv(location, values, offset, length);
		}

		@Override
		public void setUniformMatrix(String name, Mat matrix) {
			wrapped.setUniformMatrix(name, matrix);
		}

		@Override
		public void setUniformMatrix(String name, Mat matrix, boolean transpose) {
			wrapped.setUniformMatrix(name, matrix, transpose);
		}

		@Override
		public void setUniformMatrix(int location, Mat matrix) {
			wrapped.setUniformMatrix(location, matrix);
		}

		@Override
		public void setUniformMatrix(int location, Mat matrix, boolean transpose) {
			wrapped.setUniformMatrix(location, matrix, transpose);
		}

		@Override
		public void setUniformMatrix4(String name, float[] val) {
			wrapped.setUniformMatrix4(name, val);
		}

		@Override
		public void setUniformMatrix4(String name, Mat mat) {
			wrapped.setUniformMatrix4(name, mat);
		}

		@Override
		public void setUniformMatrix4(String name, Mat mat, float near, float far) {
			wrapped.setUniformMatrix4(name, mat, near, far);
		}

		@Override
		public void setUniformMatrix3fv(String name, FloatBuffer buffer, int count, boolean transpose) {
			wrapped.setUniformMatrix3fv(name, buffer, count, transpose);
		}

		@Override
		public void setUniformMatrix4fv(String name, FloatBuffer buffer, int count, boolean transpose) {
			wrapped.setUniformMatrix4fv(name, buffer, count, transpose);
		}

		@Override
		public void setUniformMatrix4fv(int location, float[] values, int offset, int length) {
			wrapped.setUniformMatrix4fv(location, values, offset, length);
		}

		@Override
		public void setUniformMatrix4fv(String name, float[] values, int offset, int length) {
			wrapped.setUniformMatrix4fv(name, values, offset, length);
		}

		@Override
		public void setUniformf(String name, Vec2 values) {
			wrapped.setUniformf(name, values);
		}

		@Override
		public void setUniformf(int location, Vec2 values) {
			wrapped.setUniformf(location, values);
		}

		@Override
		public void setUniformf(String name, Vec3 values) {
			wrapped.setUniformf(name, values);
		}

		@Override
		public void setUniformf(int location, Vec3 values) {
			wrapped.setUniformf(location, values);
		}

		@Override
		public void setUniformf(String name, Color values) {
			wrapped.setUniformf(name, values);
		}

		@Override
		public void setUniformf(int location, Color values) {
			wrapped.setUniformf(location, values);
		}

		@Override
		public void bind() {
			wrapped.bind();
		}

		@Override
		public void dispose() {
			wrapped.dispose();
		}

		@Override
		public boolean isDisposed() {
			return wrapped.isDisposed();
		}

		@Override
		public void disableVertexAttribute(String name) {
			wrapped.disableVertexAttribute(name);
		}

		@Override
		public boolean hasAttribute(String name) {
			return wrapped.hasAttribute(name);
		}

		@Override
		public int getAttributeType(String name) {
			return wrapped.getAttributeType(name);
		}

		@Override
		public int getAttributeLocation(String name) {
			return wrapped.getAttributeLocation(name);
		}

		@Override
		public int getAttributeSize(String name) {
			return wrapped.getAttributeSize(name);
		}

		@Override
		public boolean hasUniform(String name) {
			return wrapped.hasUniform(name);
		}

		@Override
		public int getUniformType(String name) {
			return wrapped.getUniformType(name);
		}

		@Override
		public int getUniformLocation(String name) {
			return wrapped.getUniformLocation(name);
		}

		@Override
		public int getUniformSize(String name) {
			return wrapped.getUniformSize(name);
		}

		@Override
		public String[] getAttributes() {
			return wrapped.getAttributes();
		}

		@Override
		public String[] getUniforms() {
			return wrapped.getUniforms();
		}

		@Override
		public String getVertexShaderSource() {
			return wrapped.getVertexShaderSource();
		}

		@Override
		public String getFragmentShaderSource() {
			return wrapped.getFragmentShaderSource();
		}
	}
}
