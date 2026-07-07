package endfield.world.consumers;

import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import endfield.world.blocks.HasPressure;
import endfield.world.meta.StatValues2;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import org.jetbrains.annotations.Nullable;

public class ConsumeFluid extends Consume {
	/**
	 * Fluid used by this consumer, if null, air is used
	 */
	public @Nullable Liquid fluid;
	/**
	 * Amount of fluid consumed, will always be positive if fluid isn't air.
	 */
	public float amount;
	/**
	 * If true, fluid is consumed per tick and not per craft.
	 */
	public boolean continuous;

	/**
	 * Min pressure required for this consumer to function.
	 */
	public float startRange;
	/**
	 * Max pressure allowed for this consumer to function.
	 */
	public float endRange;
	/**
	 * Efficiency multiplier of this consumer. Based on pressure.
	 */
	public float efficiencyMultiplier = 1f;
	/**
	 * Pressure whose building's efficiency is at it's peak.
	 */
	public float optimalPressure;
	/**
	 * Whether to display the optimal pressure.
	 */
	public boolean hasOptimalPressure = false;
	/**
	 * Interpolation curve used to determine efficiency. 0 is startRange, 1 is endRange.
	 */
	public Interp curve = Interp.one;

	public ConsumeFluid(@Nullable Liquid fluid, float amount) {
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public void apply(Block block) {
		block.hasLiquids = true;
	}

	@Override
	public void display(Stats stats) {
		if (amount != 0) {
			if (continuous) {
				stats.add(amount > 0 ? Stat.input : Stat.output, StatValues2.fluid(fluid, Math.abs(amount), 1f, true));
			} else {
				stats.add(amount > 0 ? Stat.input : Stat.output, StatValues2.fluid(fluid, Math.abs(amount), 60f, false));
			}
		}
	}

	@Override
	public float efficiency(Building build) {
		if (!shouldConsume(build)) return 0f;
		return 1f;
	}

	@Override
	public float efficiencyMultiplier(Building build) {
		if (!shouldConsume(build)) return 0f;
		return curve.apply(1f, efficiencyMultiplier, Mathf.clamp(Mathf.map(((HasPressure) build).getPressure(fluid), startRange, endRange, 0f, 1f)));
	}

	public boolean shouldConsume(Building build) {
		if (!(build instanceof HasPressure pressure)) return false;

		if (fluid != null && amount > 0 && pressure.getFluid(fluid) <= amount) return false;
		if (startRange == endRange) return true;
		return startRange <= pressure.getPressure(fluid) && pressure.getPressure(fluid) <= endRange && (fluid == null || pressure.pressure().liquids[fluid.id] > amount);
	}

	@Override
	public void trigger(Building build) {
		if (!continuous && shouldConsume(build)) {
			((HasPressure) build).removeFluid(fluid, amount);
		}
	}

	@Override
	public void update(Building build) {
		if (continuous && shouldConsume(build)) {
			((HasPressure) build).removeFluid(fluid, amount * Time.delta);
		}
	}
}
