package endfield.world.blocks.heat;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.IntSet;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.graphics.Pal2;
import endfield.util.Get;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.graphics.Layer;
import mindustry.graphics.Lod;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.ui.Bar;
import mindustry.world.blocks.distribution.DirectionBridge;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class HeatDirectionBridge extends DirectionBridge {
	public float visualMaxHeat = 15f;
	public boolean splitHeat = false;

	public TextureRegion heatRegion, bridgeHeatRegion;

	public HeatDirectionBridge(String name) {
		super(name);

		underBullets = true;
		noUpdateDisabled = true;
	}

	@Override
	public void setBars() {
		super.setBars();
		addBar("heat", (HeatDirectionBridgeBuild tile) -> new Bar(
				() -> Core.bundle.format("bar.heatamount", Strings.autoFixed(tile.heat, 1)),
				() -> Pal.lightOrange,
				() -> tile.heat / visualMaxHeat
		));
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.add(Stat.range, range, StatUnit.blocks);
	}

	@Override
	public void load() {
		super.load();

		heatRegion = Core.atlas.find(name + "-heat");
		bridgeHeatRegion = Core.atlas.find(name + "-bridge-heat");
	}

	public class HeatDirectionBridgeBuild extends DirectionBridgeBuild implements HeatConsumer, HeatBlock {
		public float heat = 0f;
		public float[] sideHeat = new float[4];
		public IntSet cameFrom = new IntSet();
		public long lastHeatUpdate = -1;

		@Override
		public void draw() {
			DirectionBridgeBuild link = findLink();
			if (link != null) {
				Draw.z(Layer.power - 1);
				drawBridge(rotation, x, y, link.x, link.y);
			}
			if (linked()) drawHeatOutput();
			else drawHeatInput();
		}

		public void drawHeatInput() {
			Draw.z(Layer.blockAdditive);
			float[] side = sideHeat;
			for (int i = 0; i < 4; i++) {
				if (side[i] > 0) {
					Draw.blend(Blending.additive);
					Draw.color(Pal2.heat, side[i] / visualMaxHeat * (Pal2.heat.a * (1f - 0.3f + Mathf.absin(10f, 0.3f))));
					Draw.rect(heatRegion, x, y, i * 90f);
					Draw.blend();
					Draw.color();
				}
			}
			Draw.z(Layer.block);
		}

		public void drawHeatOutput() {
			if (heat > 0) {
				Draw.z(Layer.blockAdditive);
				Draw.blend(Blending.additive);
				Draw.color(Pal2.heat, heat / visualMaxHeat * (Pal2.heat.a * (1f - 0.3f + Mathf.absin(10f, 0.3f))));
				Draw.rect(heatRegion, x, y, rotation * 90);
				Draw.blend();
				Draw.color();
				Draw.z(Layer.block);
			}
		}

		public void drawBridge(int rotation, float x1, float y1, float x2, float y2) {
			Draw.alpha(Renderer.bridgeOpacity);
			float angle = Angles.angle(x1, y1, x2, y2);
			float cx = (x1 + x2) / 2f;
			float cy = (y1 + y2) / 2f;
			float len = Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) - size * tilesize;

			Draw.rect(bridgeRegion, cx, cy, len, bridgeRegion.height * bridgeRegion.scl(), angle);

			if (bridgeBotRegion.found()) {
				Draw.color(0.4f, 0.4f, 0.4f, 0.4f * Renderer.bridgeOpacity);
				Draw.rect(bridgeBotRegion, cx, cy, len, bridgeBotRegion.height * bridgeBotRegion.scl(), angle);
				Draw.reset();
			}

			if (heat > 0 && bridgeHeatRegion.found()) {
				Draw.blend(Blending.additive);
				Draw.color(Pal2.heat, heat / visualMaxHeat * (Pal2.heat.a * (1f - 0.3f + Mathf.absin(10f, 0.3f))));
				Draw.rect(bridgeHeatRegion, cx, cy, len, bridgeBotRegion.height * bridgeBotRegion.scl(), angle);
				Draw.blend();
				Draw.color();
			}

			Draw.alpha(Renderer.bridgeOpacity);

			if (Lod.l1) {
				Draw.alpha(Lod.alpha1);
				for (float i = 6f; i <= len + size * tilesize - 5f; i += 5f) {
					Draw.rect(arrowRegion, x1 + Geometry.d4x(rotation) * i, y1 + Geometry.d4y(rotation) * i, angle);
				}
			}

			Draw.reset();
		}

		@Override
		public void updateTile() {
			DirectionBridgeBuild link = lastLink = findLink();
			if (link instanceof HeatDirectionBridgeBuild other) {
				link.occupied[rotation % 4] = this;
				updateTransfer();
				other.updateTransfer();
			}

			for (int i = 0; i < 4; i++) {
				if (occupied[i] == null) continue;

				if (occupied[i].rotation != i || !occupied[i].isValid() || occupied[i].lastLink != this) {
					occupied[i] = null;
				}
			}
		}

		public void updateTransfer() {
			if (linked()) {
				float totalHeat = 0f;

				for (int i = 0; i < 4; i++) {
					DirectionBridgeBuild link = occupied[i];

					if (link instanceof HeatDirectionBridgeBuild other) {
						totalHeat += other.heat;
					}
				}
				heat = totalHeat;
			} else {
				updateHeat();
			}
		}

		public boolean linked() {
			for (int i = 0; i < 4; i++) {
				if (i == Get.reverse(rotation)) continue;

				DirectionBridgeBuild link = occupied[i];
				if (link != null) {
					return true;
				}
			}
			return false;
		}

		public void updateHeat() {
			if (lastHeatUpdate == Vars.state.updateId) return;

			lastHeatUpdate = Vars.state.updateId;
			heat = enabled ? calculateHeat(sideHeat, cameFrom) : 0f;
		}

		@Override
		public float heat() {
			return findLink() == null ? heat : 0f;
		}

		@Override
		public float heatFrac() {
			return (heat / visualMaxHeat) / (splitHeat ? 3f : 1);
		}

		@Override
		public float heatRequirement() {
			return visualMaxHeat;
		}

		@Override
		public float[] sideHeat() {
			return sideHeat;
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

		@Override
		public double sense(LAccess sensor) {
			if (sensor == LAccess.heat) return heat;
			return super.sense(sensor);
		}
	}
}
