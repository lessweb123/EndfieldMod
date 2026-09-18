package endfield.world.blocks.heat;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.content.Blocks2;
import endfield.world.blocks.AutoTiler;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.ChainedBuilding;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConductor;
import mindustry.world.blocks.heat.HeatProducer;
import mindustry.world.blocks.power.NuclearReactor;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import org.jetbrains.annotations.Nullable;

import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;

public class HeatBelt extends HeatConductor implements AutoTiler {
	public TextureRegion[] topRegions = new TextureRegion[5];
	public TextureRegion[] botRegions = new TextureRegion[5];
	public TextureRegion[] heatRegions = new TextureRegion[5];
	public TextureRegion capRegion;
	public Color heatColor1 = new Color(1f, 0.3f, 0.3f);
	public Color heatColor2 = Pal.turretHeat;
	public float heatPulse = 0.5f, heatPulseScl = 10f;

	public float warmupRate = 0.8f;

	public Block bridgeReplacement;

	public HeatBelt(String name) {
		super(name);

		size = 1;
		group = BlockGroup.heat;
		update = true;
		solid = false;
		conveyorPlacement = true;
		noUpdateDisabled = true;
		underBullets = true;
		rotate = true;
		rotateDraw = true;
		isDuct = true;
		priority = TargetPriority.transport;
		envEnabled = Env.space | Env.terrestrial | Env.underwater;
	}

	@Override
	public void load() {
		super.load();
		for (int i = 0; i < 5; i++) {
			botRegions[i] = Core.atlas.find(name + "-bottom-" + i);
			topRegions[i] = Core.atlas.find(name + "-top-" + i);
			heatRegions[i] = Core.atlas.find(name + "-heat-" + i);
		}
		capRegion = Core.atlas.find(name + "-cap");
	}

	@Override
	public void setBars() {
		super.setBars();

		addBar("heat", (
				HeatBeltBuilding entity) -> new Bar(() -> Core.bundle.format("bar.heatamount", (int) (entity.heat + 0.001f)), () -> Pal.lightOrange, () -> entity.heat / visualMaxHeat));
	}

	@Override
	public void init() {
		super.init();

		if (bridgeReplacement == null) bridgeReplacement = Blocks2.heatBridge;
	}

	@Override
	protected void initBuilding() {
		if (buildType == null) buildType = HeatBeltBuilding::new;
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		int[] bits = getTiling(plan, list);

		if (bits == null) return;

		Draw.scl(bits[1], bits[2]);
		Draw.alpha(0.5f);
		Draw.rect(botRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
		Draw.color();
		Draw.rect(topRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
		Draw.scl();
	}

	@Override
	public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock) {
		Building build = tile.build;

		boolean directionalProducer = otherblock instanceof HeatProducer || otherblock instanceof NuclearReactor;

		boolean orientationMatch = directionalProducer ? otherFacesThis(tile, otherx, othery, otherrot, otherblock) :
				lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);

		return (build instanceof HeatBlock || lookingAt(tile, rotation, otherx, othery, otherblock)
				|| otherblock instanceof HeatConductor
				|| otherblock instanceof ThermalGenerator
				|| (otherblock instanceof HeatProducer b && b.heatOutput > 0)
				|| otherblock instanceof HeatDirectionBridge) && orientationMatch;
	}

	public boolean otherFacesThis(Tile tile, int otherx, int othery, int otherrot, Block otherblock) {
		Tile facing = Edges.getFacingEdge(otherblock, otherx, othery, tile);
		return facing != null && facing.relativeTo(tile) == otherrot;
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{botRegions[0], topRegions[0]};
	}

	@Override
	public void handlePlacementLine(Seq<BuildPlan> plans) {
		if (bridgeReplacement instanceof HeatDirectionBridge bridge) Placement.calculateBridges(plans, bridge, false, b -> b instanceof HeatDirectionBridge);
	}

	public class HeatBeltBuilding extends HeatConductorBuild implements ChainedBuilding {
		public int blendbits, xscl = 1, yscl = 1, blending;
		public boolean capped, backCapped = false;
		public @Nullable Building next;
		public @Nullable HeatBeltBuilding nextc;

		@Override
		public void payloadDraw() {
			Draw.rect(fullIcon, x, y);
		}

		@Override
		public void drawCached() {
			super.drawCached();
		}

		@Override
		public void draw() {
			float rotation = rotdeg();
			int r = this.rotation;

			Draw.z(Layer.blockUnder);
			//draw extra ducts facing this one for tiling purposes
			for (int i = 0; i < 4; i++) {
				if ((blending & (1 << i)) != 0) {
					int dir = r - i;
					float rot = i == 0 ? rotation : (dir) * 90;
					drawAt(x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
				}
			}

			Draw.scl(xscl, yscl);
			drawAt(x, y, blendbits, rotation, SliceMode.none);
			Draw.scl();
			if (capped && capRegion.found()) Draw.rect(capRegion, x, y, rotation);
			if (backCapped && capRegion.found()) Draw.rect(capRegion, x, y, rotation + 180);
		}


		public void drawAt(float x, float y, int bits, float rotation, SliceMode slice) {
			Draw.z(Layer.block - 0.2f);
			Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);

			Draw.z(Layer.block);
			Draw.rect(sliced(topRegions[bits], slice), x, y, rotation);

			if (heat > 0.001f && !headless) {
				Draw.tint(heatColor1, heatColor2, Mathf.clamp(heatFrac() / 2) * Mathf.absin(Time.time, heatPulse));
				Draw.alpha(Mathf.curve(heat / visualMaxHeat, 0f, 1f) * (1f - Mathf.absin(Time.time, (3f * heat / visualMaxHeat + 6f * heatPulseScl * 3f) / heatPulseScl, heatPulse)));
				Draw.blend(Blending.additive);
				Draw.rect(sliced(heatRegions[bits], slice), x, y, rotation);
				Draw.blend();
			}
			Draw.reset();
		}

		@Override
		public boolean acceptItem(Building source, Item item) {
			return false;
		}

		@Override
		public void handleStack(Item item, int amount, Teamc source) {}

		@Override
		public void handleItem(Building source, Item item) {}

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

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();

			int[] bits = buildBlending(tile, rotation, null, true);
			blendbits = bits[0];
			xscl = bits[1];
			yscl = bits[2];
			blending = bits[4];
			next = front();
			nextc = next instanceof HeatBeltBuilding d ? d : null;

			Building next = front(), prev = back();
			capped = next == null || next.team != team;
			backCapped = blendbits == 0 && (prev == null || prev.team != team);
		}

		@Nullable
		@Override
		public Building next() {
			Tile next = tile.nearby(rotation);
			if (next != null && next.build instanceof HeatBeltBuilding) {
				return next.build;
			}
			return null;
		}
	}
}
