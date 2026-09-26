package endfield.world.blocks.environment;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Strings;
import endfield.util.Sprites;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.RuneOverlay;

import static mindustry.Vars.tilesize;

public class LineMarkingOverlay extends RuneOverlay {
	public TextureRegion[] letterRegions2;

	public LineMarkingOverlay(String name) {
		super(name);
	}

	@Override
	public void load() {
		int tsize = (int) (tilesize / Draw.scl);

		customShadowRegion = Core.atlas.find(name + "-shadow");
		teamRegion = Core.atlas.find(name + "-team");

		//load specific team regions
		teamRegions = new TextureRegion[Team.all.length];
		for (Team team : Team.all) {
			teamRegions[team.id] = teamRegion.found() && team.hasPalette ? Core.atlas.find(name + "-team-" + team.name, teamRegion) : teamRegion;
		}

		variantRegions = new TextureRegion[]{Core.atlas.find(name)};
		variantRegions[0] = region;

		if (Core.atlas.has(name + "-edge")) {
			edges = Core.atlas.find(name + "-edge").split(tsize, tsize);
			if (edges.length != 3 || edges[0].length != 3) {
				//edges must be 3x3
				TextureRegion error = Core.atlas.find("error");
				edges = new TextureRegion[][]{
						new TextureRegion[]{error, error, error},
						new TextureRegion[]{error, error, error},
						new TextureRegion[]{error, error, error},
				};
			}
		}
		region = variantRegions[0];
		edgeRegion = Core.atlas.find("edge");

		letterRegions = Sprites.splitArray(Core.atlas.find(name + "-0"), 32);
		letterRegions2 = Sprites.splitArray(Core.atlas.find(name + "-1"), 32);
	}

	@Override
	public void drawBase(Tile tile) {
		Draw.color(color);
		Draw.rect(tile.overlayData >= 0 ? letterRegions[Math.min(tile.overlayData, letterRegions.length - 1)] : letterRegions2[Math.min(-tile.overlayData -1, letterRegions2.length - 1)], tile.worldx(), tile.worldy());
		Draw.color();
	}

	@Override
	public Object getConfig(Tile tile) {
		return tile.overlayData;
	}

	@Override
	public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
		byte data = 0;

		if (plan.config instanceof Number num) {
			data = num.byteValue();
		}

		TextureRegion reg = data >= 0 ? letterRegions[Math.min(data, letterRegions.length - 1)] : letterRegions2[Math.min(-data -1, letterRegions2.length - 1)];
		Draw.tint(color);
		Draw.rect(reg, plan.drawx(), plan.drawy());
		Draw.tint(Color.white);
	}

	@Override
	public void buildEditorConfig(Table table) {
		byte value = lastConfig instanceof Number num ? num.byteValue() : 0;
		table.field(Byte.toString(value), val -> lastConfig = Strings.parseInt(val)).valid(t -> {
			int val = Strings.parseInt(t, 999);
			return val >= 0 ? val < letterRegions.length : -val -1 < letterRegions2.length;
		});
	}

	@Override
	public void placeEnded(Tile tile, Unit builder, int rotation, Object config) {
		if (config instanceof Number num) {
			tile.overlayData = num.byteValue();
		}
	}
}
