package endfield.world.consumers;

import arc.Events;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stats;

import static mindustry.Vars.tilesize;

public class ConsumeLiquidExplode extends ConsumeLiquidFilter {
	public float damage = 4f;
	public float threshold, baseChance = 0.06f;
	public Effect explodeEffect = Fx.generatespark;

	public ConsumeLiquidExplode(float thr) {
		filter = liquid -> liquid.explosiveness >= threshold;
		threshold = thr;
	}

	public ConsumeLiquidExplode() {
		this(1.5f);
	}

	@Override
	public void update(Building build) {
		Liquid liquid = getConsumed(build);

		if (liquid != null) {
			if (Vars.state.rules.reactorExplosions && Mathf.chance(build.delta() * baseChance * Mathf.clamp(liquid.explosiveness - threshold))) {
				build.damage(damage * liquid.explosiveness);
				explodeEffect.at(build.x + Mathf.range(build.block.size * tilesize / 2f), build.y + Mathf.range(build.block.size * tilesize / 2f));
				Events.fire(Trigger.blastGenerator);
			}
		}
	}

	@Override
	public void build(Building build, Table table) {}

	@Override
	public void display(Stats stats) {}

	@Override
	public void apply(Block block) {}

	@Override
	public float efficiency(Building build) {
		return 1f;
	}
}
