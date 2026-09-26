package endfield.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import endfield.entities.effect.WrapperEffect;
import endfield.graphics.Draws;
import endfield.graphics.Pal2;
import endfield.graphics.Shaders2;
import endfield.type.CrystalLiquid;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.gen.Puddle;
import mindustry.type.Liquid;

/**
 * Defines the {@linkplain Liquid liquid} this mod offers.
 *
 * @author LessWeb
 */
public final class Liquids2 {
	static final int coldPlasmaId = Draws.nextTaskId(), hotPlasmaId = Draws.nextTaskId();

	public static ObjectFloatMap<Liquid> densities = new ObjectFloatMap<>(), viscosities = new ObjectFloatMap<>();

	public static Liquid brine, acid, promethium;
	public static Liquid chlorine, gas, lightOil, nitratedOil, blastReagent;
	public static CrystalLiquid coldPlasma, hotPlasma;

	/** Don't let anyone instantiate this class. */
	private Liquids2() {}

	public static float getDensity(Liquid liquid) {
		return densities.get(liquid, 1 / 8f);
	}

	public static float getViscosity(Liquid liquid) {
		return viscosities.get(liquid, 1f);
	}

	/** Instantiates all contents. Called in the main thread in {@code EndFieldMod.loadContent()}. */
	public static void load() {
		brine = new Liquid("brine", new Color(0xb8c89fff)) {{
			coolant = false;
			viscosity = 0.55f;
			explosiveness = 0.1f;
			densities.put(this, 1 / 8f);
			viscosities.put(this, 1f);
		}};
		acid = new Liquid("acid", Pal2.acidFront) {{
			coolant = false;
			heatCapacity = 0.3f;
			effect = StatusEffects.corroded;
			boilPoint = 0.9f;
			viscosity = 0.65f;
			gasColor = color;
			temperature = 0.56f;
		}};
		promethium = new Liquid("promethium", Pal2.promethiumFront) {{
			flammability = 3.25f;
			temperature = 0.6f;
			viscosity = 0.9f;
			explosiveness = 0.5f;
			boilPoint = 0.5f;
			coolant = false;
			canStayOn.addAll(Liquids.water);
		}};
		chlorine = new Liquid("chlorine", Pal2.chlorineFront) {{
			barColor = color;
			gas = true;
			explosiveness = 0.6f;
			flammability = 0.8f;
		}};
		gas = new Liquid("gas", Pal2.gasFront) {{
			gasColor = barColor = lightColor = color;
			gas = true;
			flammability = 1.25f;
			explosiveness = 0.25f;
			densities.put(this, 1 / 8f);
			viscosities.put(this, 1f);
		}};
		lightOil = new Liquid("light-oil", Color.rgb(239, 202, 152).a(0.8f)) {{
			heatCapacity = 0.7f;
			temperature = 0.3f;
			boilPoint = 0.6f;
			viscosity = 0.7f;
			flammability = 1.3f;
			explosiveness = 0.3f;
			gasColor = Color.grays(0.7f);
			effect = StatusEffects.muddy;
			coolant = false;
			densities.put(this, 1 / 8f);
			viscosities.put(this, 1f);
		}};
		nitratedOil = new Liquid("nitrated-oil", Pal2.nitratedOilFront) {{
			temperature = 0.5f;
			viscosity = 0.8f;
			flammability = 1.5f;
			explosiveness = 1.8f;
			effect = StatusEffects.tarred;
			canStayOn.add(Liquids.water);
			coolant = false;
			densities.put(this, 1 / 8f);
			viscosities.put(this, 1f);
		}};
		blastReagent = new Liquid("blast-reagent", new Color(0xd97c7cff)) {{
			flammability = 0.75f;
			temperature = 0.5f;
			viscosity = 0.8f;
			explosiveness = 3f;
			densities.put(this, 1 / 8f);
			viscosities.put(this, 1f);
		}};
		coldPlasma = new CrystalLiquid("cold-plasma", Pal2.coldPlasmaFront) {{
			heatCapacity = 2.5f;
			explosiveness = 0.1f;
			temperature = 0.15f;
			boilPoint = Float.MAX_VALUE;
			lightColor = color.cpy().a(0.3f);
			colorFrom = color.cpy().a(0.5f);
			colorTo = color.cpy().a(0.4f);
			particleSpacing = 10;
			particleEffect = WrapperEffect.wrap(Fx2.glowParticle, color);
			canStayOn.addAll(Liquids.water, Liquids.cryofluid, Liquids.oil, Liquids.arkycite);
			densities.put(this, 1 / 8f);
			viscosities.put(this, 1f);
		}
			@Override
			public void drawPuddle(Puddle puddle) {
				Draws.drawTask(coldPlasmaId, puddle, Shaders2.wave, s -> {
					s.waveMix = Pal2.coldPlasmaFront;
					s.mixAlpha = 0.2f + Mathf.absin(5, 0.2f);
					s.waveScl = 0.2f;
					s.maxThreshold = 1f;
					s.minThreshold = 0.4f;
				}, super::drawPuddle);
			}
		};
		hotPlasma = new CrystalLiquid("hot-plasma", Pal2.hotPlasmaFront) {{
			heatCapacity = 2.5f;
			explosiveness = 5f;
			temperature = 5f;
			coolant = false;
			boilPoint = Float.MAX_VALUE;
			lightColor = color.cpy().a(0.3f);
			colorFrom = color.cpy().a(0.5f);
			colorTo = color.cpy().a(0.4f);
			particleSpacing = 10;
			particleEffect = WrapperEffect.wrap(Fx2.glowParticle, color);
			effect = StatusEffects2.ultFireBurn;
			densities.put(this, 1 / 8f);
			viscosities.put(this, 1f);
		}
			@Override
			public void drawPuddle(Puddle puddle) {
				Draws.drawTask(hotPlasmaId, puddle, Shaders2.wave, s -> {
					s.waveMix = Pal2.hotPlasmaFront;
					s.mixAlpha = 0.2f + Mathf.absin(5, 0.2f);
					s.waveScl = 0.2f;
					s.maxThreshold = 1f;
					s.minThreshold = 0.4f;
				}, super::drawPuddle);
			}
		};
	}
}
