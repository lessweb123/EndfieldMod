package endfield.world.blocks.production;

import arc.struct.Seq;
import endfield.world.blocks.production.DrillModule.DrillModuleBuild;
import endfield.world.meta.Stats2;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.world.blocks.production.Drill;

public class ModuleDrill extends Drill {
	public int maxModules = 1;

	public ModuleDrill(String name) {
		super(name);
		size = 4;
		itemCapacity = 40;
		canOverdrive = false;
		drawTeamOverlay = false;
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.add(Stats2.maxModules, maxModules);
	}

	public class ModuleDrillBuild extends DrillBuild {
		public Seq<DrillModuleBuild> modules = new Seq<>(DrillModuleBuild.class);

		public float maxModules() {
			return maxModules;
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			modules.clear();
			proximity.each(b -> {
				if (b instanceof DrillModuleBuild module) {
					if (module.canApply(this)) {
						module.drillBuild = this;
						modules.add(module);
						module.apply(this);
					}
				}
			});
		}

		@Override
		public void drawSelect() {
			super.drawSelect();
			Drawf.selected(this, Pal.accent);
			modules.each(b -> Drawf.selected(b, Pal.accent));
		}

		@Override
		public void remove() {
			super.remove();
			for (DrillModuleBuild module : modules) {
				module.drillBuild = null;
			}
		}

		@Override
		public Object senseObject(LAccess sensor) {
			if (sensor == LAccess.firstItem) return dominantItem;
			return super.senseObject(sensor);
		}
	}
}
