package endfield.world.blocks.distribution;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import endfield.util.Sprites;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Conveyor;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class Conveyor3 extends Conveyor {
	public TextureRegion[] edgeRegions, lightRegions, pulseRegions, arrowRegions;
	public float framePeriod = 8f;

	public Conveyor3(String name) {
		super(name);

		placeableLiquid = true;
		drawTeamOverlay = false;

		emitLight = true;
		lightRadius = 20f;
	}

	@Override
	public void load() {
		region = Core.atlas.find(name);

		customShadowRegion = Core.atlas.find(name + "-shadow");

		//load specific team regions
		teamRegion = Core.atlas.find(name + "-team");

		teamRegions = new TextureRegion[Team.all.length];
		for (Team team : Team.all) {
			teamRegions[team.id] = teamRegion.found() && team.hasPalette ? Core.atlas.find(name + "-team-" + team.name, teamRegion) : teamRegion;
		}

		edgeRegions = Sprites.splitArray(Core.atlas.find(name + "-edge"), 32, 0, Sprites.index4x4);
		lightRegions = Sprites.splitArray(Core.atlas.find(name + "-light"), 32, 0, Sprites.index4x4);
		pulseRegions = Sprites.splitArray(Core.atlas.find(name + "-pulse"), 32, 0, Sprites.index4x4);
		arrowRegions = Sprites.splitArray(Core.atlas.find(name + "-arrow"), 32, 1);
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		int[] bits = getTiling(plan, list);

		if (bits == null) return;
		Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90);
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{region};
	}

	public boolean blends(Building self, Building other) {
		if (other == null) return false;
		return blends(self.tile, self.rotation, other.tileX(), other.tileY(), other.rotation, other.block);
	}

	public int conveyorFrame() {
		return (int) ((((Time.time) % framePeriod) / framePeriod) * 16);
	}

	@Override
	public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock) {
		return noSideBlend ? (otherblock.outputsItems() && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock)) || (lookingAt(tile, rotation, otherx, othery, otherblock) && otherblock.hasItems) : super.blends(tile, rotation, otherx, othery, otherrot, otherblock);
	}

	@Override
	public boolean blendsArmored(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock) {
		return noSideBlend ? Point2.equals(tile.x + Geometry.d4(rotation).x, tile.y + Geometry.d4(rotation).y, otherx, othery) || ((!otherblock.rotatedOutput(otherx, othery) && Edges.getFacingEdge(otherblock, otherx, othery, tile) != null && Edges.getFacingEdge(otherblock, otherx, othery, tile).relativeTo(tile) == rotation) || (otherblock instanceof Conveyor && otherblock.rotatedOutput(otherx, othery) && Point2.equals(otherx + Geometry.d4(otherrot).x, othery + Geometry.d4(otherrot).y, tile.x, tile.y))) : super.blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock);
	}

	public class ConveyorBuild3 extends ConveyorBuild {
		public int drawIndex = 0;

		@Override
		public boolean acceptItem(Building source, Item item) {
			return super.acceptItem(source, item) && (!noSideBlend || (source.block instanceof Conveyor || Edges.getFacingEdge(source.tile, tile).relativeTo(tile) == rotation));
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			drawIndex = 0;
			if (check(tile.x, tile.y + 1)) drawIndex += 1;
			if (check(tile.x + 1, tile.y)) drawIndex += 2;
			if (check(tile.x, tile.y - 1)) drawIndex += 4;
			if (check(tile.x - 1, tile.y)) drawIndex += 8;
		}

		public boolean check(int x, int y) {
			return blends(this, world.build(x, y));
		}

		@Override
		public void draw() {
			Draw.z(Layer.block - 0.25f);
			Draw.mixcol(team.color, Color.clear, 0.75f);
			Draw.rect(pulseRegions[drawIndex], x, y);

			Draw.z(Layer.block - 0.2f);
			Draw.rect(arrowRegions[conveyorFrame()], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

			boolean backDraw = true;
			if (blends(this, right())) {
				Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() + 90);
				backDraw = false;
			}
			if (blends(this, back())) {
				Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());
				backDraw = false;
			}
			if (blends(this, left())) {
				Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() - 90);
				backDraw = false;
			}
			if (backDraw) {
				Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());
			}

			Draw.color();
			Draw.reset();
			Draw.rect(edgeRegions[drawIndex], x, y);

			Draw.color(team.color, Color.white, 0.25f);
			Draw.alpha(0.4f);
			Draw.rect(lightRegions[drawIndex], x, y);
			Draw.alpha(1f);
			Draw.color();

			Draw.reset();
			Draw.z(Layer.block - 0.05f);
			float layer = Layer.block - 0.1f, wwidth = world.unitWidth(), wheight = world.unitHeight(), scaling = 0.01f;

			for (int i = 0; i < len; i++) {
				Item item = ids[i];
				Tmp.v1.trns(rotation * 90, tilesize, 0);
				Tmp.v2.trns(rotation * 90, -tilesize / 2f, xs[i] * tilesize / 2f);

				float
						ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x),
						iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

				//keep draw position deterministic.
				Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
				Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
			}
		}

		@Override
		public void drawLight() {
			Drawf.light(x, y, lightRadius, Tmp.c1.set(team.color).lerp(Color.white, 0.5f), 0.5f);
		}
	}
}
