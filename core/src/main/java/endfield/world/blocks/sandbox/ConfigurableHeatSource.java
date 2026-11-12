package endfield.world.blocks.sandbox;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawHeatOutput;
import mindustry.world.draw.DrawMulti;

public class ConfigurableHeatSource extends Block {
	public float heatOutput = 1000f;

	public DrawBlock drawer;

	public ConfigurableHeatSource(String name) {
		super(name);

		destructible = true;
		configurable = true;

		drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput());
		rotateDraw = false;
		rotate = true;
		canOverdrive = false;
		drawArrow = true;

		config(Float.class, (ConfigurableHeatSourceBuild tile, Float heat) -> tile.heat = heat);
	}

	@Override
	public void load() {
		super.load();

		drawer.load(this);
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		drawer.drawPlan(this, plan, list);
	}

	@Override
	public TextureRegion[] icons() {
		return drawer.finalIcons(this);
	}

	@Override
	public void getRegionsToOutline(Seq<TextureRegion> out) {
		drawer.getRegionsToOutline(this, out);
	}

	public class ConfigurableHeatSourceBuild extends Building implements HeatBlock {
		public float heat;

		@Override
		public Building create(Block block, Team team) {
			heat = heatOutput;

			return super.create(block, team);
		}

		@Override
		public void draw() {
			drawer.draw(this);
		}

		@Override
		public void drawLight() {
			super.drawLight();

			drawer.drawLight(this);
		}

		@Override
		public void buildConfiguration(Table table) {
			table.table(Styles.black5, t -> {
				t.margin(6f);
				t.field(String.valueOf(heat), text -> {
					float newHeat = heat;
					if (Strings.canParsePositiveFloat(text)) {
						newHeat = Strings.parseFloat(text);
					}
					configure(newHeat);
				}).width(120).get().setFilter(TextFieldFilter.floatsOnly);
				t.add(Core.bundle.get("unit.seconds")).left();
			});
		}

		@Override
		public float heat() {
			return heat;
		}

		@Override
		public float heatFrac() {
			return heat;
		}

		@Override
		public void write(Writes write) {
			super.write(write);

			write.f(heat);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);

			heat = read.f();
		}
	}
}
