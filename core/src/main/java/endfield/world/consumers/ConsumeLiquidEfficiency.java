package endfield.world.consumers;

import arc.func.Boolf;
import arc.func.Floatf;
import arc.struct.ObjectFloatMap;
import endfield.world.meta.StatValues2;
import mindustry.type.Liquid;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import org.jetbrains.annotations.Nullable;

public class ConsumeLiquidEfficiency extends ConsumeLiquidFilter {
	public @Nullable ObjectFloatMap<Liquid> liquidDurationMultipliers;
	public Floatf<Liquid> efficiency;

	public ConsumeLiquidEfficiency(Boolf<Liquid> liquid, Floatf<Liquid> eff, float amount) {
		super(liquid, amount);
		efficiency = eff;
	}

	public ConsumeLiquidEfficiency() {}

	@Override
	public void display(Stats stats) {
		stats.add(Stat.input, StatValues2.liquidEffMultiplier(liquid -> efficiency.get(liquid), amount * 60f, filter, liquidDurationMultipliers));
	}

	@Override
	public float liquidEfficiencyMultiplier(Liquid liquid) {
		return efficiency.get(liquid);
	}
}
