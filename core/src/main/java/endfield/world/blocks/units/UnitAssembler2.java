package endfield.world.blocks.units;

import arc.Core;
import arc.Events;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.net.Call2;
import endfield.world.consumers.ConsumePayloadDynamic2;
import mindustry.Vars;
import mindustry.ai.types.AssemblerAI;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.EntityCollisions;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.UnitCreateEvent;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.BuildingTetherc;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.units.UnitAssembler.YeetData;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeLiquidsDynamic;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static mindustry.Vars.collisions;
import static mindustry.Vars.indexer;
import static mindustry.Vars.net;
import static mindustry.Vars.player;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class UnitAssembler2 extends PayloadBlock {
	public TextureRegion sideRegion1, sideRegion2;

	public int areaSize = 11;
	public UnitType droneType = UnitTypes.assemblyDrone;
	public int dronesCreated = 4;
	public float droneConstructTime = 60f * 4f;
	public int[] capacities = {};

	public Seq<AssemblerUnitPlan2> plans = new Seq<>(true, 4, AssemblerUnitPlan2.class);

	public Sound createSound = Sounds.unitCreateBig;
	public float createSoundVolume = 1f;

	protected @Nullable ConsumePayloadDynamic2 consPayload;
	protected @Nullable ConsumeItemDynamic consItem;
	protected @Nullable ConsumeLiquidsDynamic consLiquids;

	public UnitAssembler2(String name) {
		super(name);

		update = solid = true;
		rotate = true;
		rotateDraw = false;
		acceptsPayload = hasItems = true;
		flags = EnumSet.of(BlockFlag.unitAssembler);
		regionRotated1 = 1;
		sync = true;
		group = BlockGroup.units;
		commandable = true;
		quickRotate = false;
		ambientSound = Sounds.loopUnitBuilding;
		ambientSoundVolume = 0.13f;
		configurable = true;

		config(Integer.class, (UnitAssemblerBuild2 build, Integer i) -> {
			if (build.currentPlan == i) return;

			build.currentPlan = i;
			build.progress = 0;
		});
		config(UnitType.class, (UnitAssemblerBuild2 build, UnitType type) -> {
			int next = plans.indexOf(p -> p.unit == type);
			if (build.currentPlan == next) return;
			build.currentPlan = next;
			build.progress = 0;
		});
		configClear((UnitAssemblerBuild2 build) -> {
			build.currentPlan = -1;
			build.progress = 0;
		});
	}

	public Rect getRect(Rect rect, float x, float y, int rotation) {
		rect.setCentered(x, y, areaSize * tilesize);
		float len = tilesize * (areaSize + size) / 2f;

		rect.x += Geometry.d4x(rotation) * len;
		rect.y += Geometry.d4y(rotation) * len;

		return rect;
	}

	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		super.drawPlace(x, y, rotation, valid);

		x *= tilesize;
		y *= tilesize;
		x += offset;
		y += offset;

		Rect rect = getRect(Tmp.r1, x, y, rotation);

		Drawf.dashRect(valid ? Pal.accent : Pal.remove, rect);
	}

	@Override
	public boolean canPlaceOn(Tile tile, Team team, int rotation) {
		//overlapping construction areas not allowed unless it s being replaced; grow by a tiny amount so edges can't overlap either.
		Rect rect = getRect(Tmp.r1, tile.worldx() + offset, tile.worldy() + offset, rotation).grow(0.1f);
		return !indexer.getFlagged(team, BlockFlag.unitAssembler).contains(b -> b != tile.build && b.block instanceof UnitAssembler2 assembler && assembler.getRect(Tmp.r2, b.x, b.y, b.rotation).overlaps(rect)) &&
				!team.data().getBuildings(ConstructBlock.get(size)).contains(b -> b != tile.build && ((ConstructBuild) b).current instanceof UnitAssembler2 assembler && assembler.getRect(Tmp.r2, b.x, b.y, b.rotation).overlaps(rect));
	}

	@Override
	public void setBars() {
		super.setBars();

		boolean planLiquids = false;
		for (int i = 0; i < plans.size; i++) {
			LiquidStack[] req = plans.get(i).liquidReq;
			if (req != null && req.length > 0) {
				for (LiquidStack stack : req) {
					addLiquidBar(stack.liquid);
				}
				planLiquids = true;
			}
		}

		if (planLiquids) {
			removeBar("liquid");
		}

		addBar("progress", (UnitAssemblerBuild2 e) -> new Bar(
				() -> Core.bundle.format("bar.progress", Strings.autoFixed(e.progress * 100f, 0)),
				() -> Pal.ammo,
				() -> e.progress
		));

		addBar("units", (UnitAssemblerBuild2 e) ->
				new Bar(() -> {
					UnitType type = e.unit();
					return type == null ? "@none" : Core.bundle.format("bar.unitcap",
							Fonts.getUnicodeStr(type.name),
							e.team.data().countType(type),
							type.useUnitCap ? Units.getStringCap(e.team) : "∞"
					);
				}, () -> Pal.power, () -> {
					UnitType type = e.unit();
					return type != null && type.useUnitCap ? ((float) e.team.data().countType(e.unit()) / Units.getCap(e.team)) : 1f;
				}));
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		Draw.rect(region, plan.drawx(), plan.drawy());
		Draw.rect(plan.rotation >= 2 ? sideRegion2 : sideRegion1, plan.drawx(), plan.drawy(), plan.rotation * 90);
		Draw.rect(topRegion, plan.drawx(), plan.drawy());
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{region, sideRegion1, topRegion};
	}

	@Override
	public void load() {
		super.load();

		sideRegion1 = Core.atlas.find(name + "-side1");
		sideRegion2 = Core.atlas.find(name + "-side2");
	}

	@Override
	public void init() {
		updateClipRadius((areaSize + 1) * tilesize);

		initConsumers();

		super.init();

		initCapacities();
	}

	@Override
	public void afterPatch() {
		super.afterPatch();

		initCapacities();
	}

	public void initConsumers() {
		consume(consPayload = new ConsumePayloadDynamic2((UnitAssemblerBuild2 build) -> {
			AssemblerUnitPlan2 plan = build.plan();
			return plan == null ? null : plan.requirements;
		}));
		consume(consItem = new ConsumeItemDynamic((UnitAssemblerBuild2 build) -> {
			AssemblerUnitPlan2 plan = build.plan();
			return plan == null || plan.itemReq == null ? ItemStack.empty : plan.itemReq;
		}));
		consume(consLiquids = new ConsumeLiquidsDynamic((UnitAssemblerBuild2 build) -> {
			AssemblerUnitPlan2 plan = build.plan();
			return plan == null || plan.liquidReq == null ? LiquidStack.empty : plan.liquidReq;
		}));
	}

	public void initCapacities() {
		consumeBuilder.each(c -> c.multiplier = b -> state.rules.unitCost(b.team));

		itemCapacity = 10;
		capacities = new int[Vars.content.items().size];
		for (AssemblerUnitPlan2 plan : plans) {
			if (plan.itemReq != null) {
				for (ItemStack stack : plan.itemReq) {
					capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
					itemCapacity = Math.max(itemCapacity, stack.amount * 2);
				}
			}

			if (plan.liquidReq != null) {
				for (LiquidStack stack : plan.liquidReq) {
					if (stack.liquid.id < liquidFilter.length) liquidFilter[stack.liquid.id] = true;
				}
			}
		}
	}

	@Override
	public void checkContentArrayCapacity(int items, int liquids) {
		super.checkContentArrayCapacity(items, liquids);
		if (capacities.length != items) capacities = Arrays.copyOf(capacities, items);
	}

	@Override
	public void setStats() {
		super.setStats();

		stats.add(Stat.output, table -> {
			table.row();

			for (AssemblerUnitPlan2 plan : plans) {
				table.table(Styles.grayPanel, t -> {
					if (plan.unit.isBanned()) {
						t.image(Icon.cancel).color(Pal.remove).size(40).pad(10);
						return;
					}

					if (plan.unit.unlockedNow()) {
						t.image(plan.unit.uiIcon).scaling(Scaling.fit).size(40).pad(10f).left().with(i -> StatValues.withTooltip(i, plan.unit));
						t.table(info -> {
							info.defaults().left();
							info.add(plan.unit.localizedName);
							info.row();
							info.add(Strings.autoFixed(plan.time / 60f, 1) + " " + Core.bundle.get("unit.seconds")).color(Color.lightGray);
						}).left();

						t.table(req -> {
							req.add().grow(); //it refuses to go to the right unless I do this. please help.

							req.table(solid -> {
								int length = 0;

								ItemStack[] items = plan.itemReq;
								if (items != null) {
									for (ItemStack stack : items) {
										if (length % 6 == 0) {
											solid.row();
										}
										solid.add(StatValues.stack(stack)).pad(5);
										length++;
									}
								}

								Seq<PayloadStack> payloads = plan.requirements;
								if (payloads != null){
									for (int i = 0; i < payloads.size; i++) {
										if (length % 6 == 0) {
											solid.row();
										}
										solid.add(StatValues.stack(payloads.get(i))).pad(5);
										length++;
									}
								}
							}).right();

							LiquidStack[] liquids = plan.liquidReq;
							if (liquids != null) {
								for (LiquidStack stack : liquids) {
									req.row();

									req.add().grow(); //another one.

									req.add(StatValues.displayLiquid(stack.liquid, stack.amount * 60f, true)).right();
								}
							}
						}).grow().pad(10f);
					} else {
						t.image(Icon.lock).color(Pal.darkerGray).size(40).pad(10);
					}
				}).growX().pad(5);
				table.row();
			}
		});
	}

	@Override
	public void getPlanConfigs(Seq<UnlockableContent> options) {
		for (AssemblerUnitPlan2 plan : plans) {
			if (!plan.unit.isBanned()) {
				options.add(plan.unit);
			}
		}
	}

	public static void assemblerUnitSpawned(Tile tile) {
		if (tile == null || !(tile.build instanceof UnitAssemblerBuild2 build)) return;
		build.spawned();
	}

	public static void assemblerDroneSpawned(Tile tile, int id) {
		if (tile == null || !(tile.build instanceof UnitAssemblerBuild2 build)) return;
		build.droneSpawned(id);
	}

	public class UnitAssemblerBuild2 extends PayloadBlockBuild<Payload> {
		public IntSeq readUnits = new IntSeq();
		public IntSeq whenSyncedUnits = new IntSeq();

		public @Nullable Vec2 commandPos;
		public Seq<Unit> units = new Seq<>(Unit.class);
		public PayloadSeq blocks = new PayloadSeq();
		public float progress, warmup, droneWarmup, powerWarmup, sameTypeWarmup;
		public float invalidWarmup = 0f;
		public int currentPlan = -1;
		public int lastPlan = -2;
		public boolean wasOccupied = false;

		public float droneProgress, totalDroneProgress;

		public Vec2 getUnitSpawn() {
			float len = tilesize * (areaSize + size) / 2f;
			float unitX = x + Geometry.d4x(rotation) * len, unitY = y + Geometry.d4y(rotation) * len;
			return Tmp.v4.set(unitX, unitY);
		}

		public @Nullable UnitType unit() {
			AssemblerUnitPlan2 plan = plan();
			return plan == null ? null : plan.unit;
		}

		public @Nullable AssemblerUnitPlan2 plan() {
			//clamp plan pos
			return currentPlan == -1 ? null : plans.get(Math.min(currentPlan, plans.size - 1));
		}

		@Override
		public boolean shouldConsume() {
			AssemblerUnitPlan2 plan = plan();
			//liquid is only consumed when building is being done
			return plan != null && enabled && !wasOccupied && Units.canCreate(team, plan.unit)
					&& (consPayload == null || consPayload.efficiency(this) > 0)
					&& (consItem == null || consItem.efficiency(this) > 0)
					&& team.activateUnitFactories();
		}

		@Override
		public void created() {
			//auto-set to the first plan, it's better than nothing.
			if (currentPlan == -1) {
				currentPlan = plans.indexOf(u -> u.unit.unlockedNow());
			}
		}

		@Override
		public void drawSelect() {
			super.drawSelect();

			Drawf.dashRect(Tmp.c1.set(Pal.accent).lerp(Pal.remove, invalidWarmup), getRect(Tmp.r1, x, y, rotation));

			drawItemSelection(unit());
		}

		@Override
		public void display(Table table) {
			super.display(table);

			if (team != player.team()) return;

			TextureRegionDrawable reg = new TextureRegionDrawable();

			table.row();
			table.table(t -> {
				t.left().defaults().left();

				t.image().update(i -> {
					i.setDrawable(currentPlan == -1 ? Icon.cancel : reg.set(plans.get(currentPlan).unit.uiIcon));
					i.setScaling(Scaling.fit);
					i.setColor(currentPlan == -1 ? Color.lightGray : Color.white);
				}).size(32).padBottom(-4).padRight(2);

				t.label(() -> {
					UnitType type = unit();
					return type == null ? "@none" : "[accent] -> []" + type.emoji() + " " + type.localizedName;
				});
			}).pad(4).padLeft(0f).fillX().left();
		}

		@Override
		public void buildConfiguration(Table table) {
			Seq<UnitType> units = Seq.with(plans).map(u -> u.unit).retainAll(u -> u.unlockedNow() && !u.isBanned());

			if (units.any()) {
				ItemSelection.buildTable(block, table, units, () -> currentPlan == -1 ? null : plans.get(currentPlan).unit, unit -> configure(plans.indexOf(u -> u.unit == unit)), selectionRows, selectionColumns);

				table.row();
			} else {
				table.table(Styles.black3, t -> t.add("@none").color(Color.lightGray));
			}
		}

		@Override
		public void updateTile() {
			if (!readUnits.isEmpty()) {
				units.clear();
				readUnits.each(i -> {
					Unit unit = Groups.unit.getByID(i);
					if (unit != null) {
						units.add(unit);
					}
				});
				readUnits.clear();
			}

			if (lastPlan != currentPlan) {
				if (lastPlan >= 0f) {
					progress = 0f;
				}

				lastPlan = lastPlan == -2 ? -1 : currentPlan;
			}

			//read newly synced drones on client end
			if (units.size < dronesCreated && whenSyncedUnits.size > 0) {
				whenSyncedUnits.each(id -> {
					Unit unit = Groups.unit.getByID(id);
					if (unit != null) {
						units.addUnique(unit);
					}
				});
			}

			units.removeAll(u -> !u.isAdded() || u.dead || !(u.controller() instanceof AssemblerAI));

			//unsupported
			if (!allowUpdate()) {
				progress = 0f;
				units.each(Unit::kill);
				units.clear();
			}

			float powerStatus = !enabled ? 0f : power == null ? 1f : power.status;
			powerWarmup = Mathf.lerpDelta(powerStatus, powerStatus > 0.0001f ? 1f : 0f, 0.1f);
			droneWarmup = Mathf.lerpDelta(droneWarmup, units.size < dronesCreated ? powerStatus : 0f, 0.1f);
			totalDroneProgress += droneWarmup * delta();

			if (units.size < dronesCreated && enabled && (droneProgress += delta() * state.rules.unitBuildSpeed(team) * powerStatus / droneConstructTime) >= 1f) {
				if (!net.client()) {
					Unit unit = droneType.create(team);
					//If a unit isn't using AssemblerAI, it's bugged, likely because of an incorrect data patch or mod.
					//In that case, just ignore it and don't spawn anything
					if (unit.controller() instanceof AssemblerAI) {
						if (unit instanceof BuildingTetherc bt) {
							bt.building(this);
						}
						unit.set(x, y);
						unit.rotation = 90f;
						unit.add();
						units.add(unit);
						Call2.assemblerDroneSpawned(tile, unit.id);
					} else {
						droneProgress = 0f;
					}
				}
			}

			if (units.size >= dronesCreated) {
				droneProgress = 0f;
			}

			Vec2 spawn = getUnitSpawn();

			if (moveInPayload() && !wasOccupied) {
				yeetPayload(payload);
				payload = null;
			}

			//arrange units around perimeter
			for (int i = 0; i < units.size; i++) {
				Unit unit = units.get(i);
				AssemblerAI ai = (AssemblerAI) unit.controller();

				ai.targetPos.trns(i * 90f + 45f, areaSize / 2f * Mathf.sqrt2 * tilesize).add(spawn);
				ai.targetAngle = i * 90f + 45f + 180f;
			}

			wasOccupied = checkSolid(spawn, false);
			boolean visualOccupied = checkSolid(spawn, true);
			float eff = (units.count(u -> ((AssemblerAI) u.controller()).inPosition()) / (float) dronesCreated);

			sameTypeWarmup = Mathf.lerpDelta(sameTypeWarmup, wasOccupied && !visualOccupied ? 0f : 1f, 0.1f);
			invalidWarmup = Mathf.lerpDelta(invalidWarmup, visualOccupied ? 1f : 0f, 0.1f);

			AssemblerUnitPlan2 plan = plan();

			//check if all requirements are met
			if (plan != null && !wasOccupied && efficiency > 0 && Units.canCreate(team, plan.unit)) {
				warmup = Mathf.lerpDelta(warmup, efficiency, 0.1f);

				if ((progress += edelta() * state.rules.unitBuildSpeed(team) * eff / plan.time) >= 1f) {
					Call2.assemblerUnitSpawned(tile);
				}
			} else {
				warmup = Mathf.lerpDelta(warmup, 0f, 0.1f);
			}
		}

		public void droneSpawned(int id) {
			Fx.spawn.at(x, y);
			droneProgress = 0f;
			if (net.client()) {
				whenSyncedUnits.add(id);
			}
		}

		public void spawned() {
			AssemblerUnitPlan2 plan = plan();

			if (plan == null) return;

			Vec2 spawn = getUnitSpawn();
			consume();

			Unit unit = plan.unit.create(team);
			if (unit.isCommandable() && commandPos != null) {
				unit.command().commandPosition(commandPos);
			}
			unit.set(spawn.x + Mathf.range(0.001f), spawn.y + Mathf.range(0.001f));
			unit.rotation = rotdeg();
			Building targetBuild = unit.buildOn();
			//'source' is the target build instead of this building; this is because some blocks only accept things from certain angles, and this is a non-standard payload
			UnitPayload payload = new UnitPayload(unit);
			if (targetBuild != null && targetBuild.team == team && targetBuild.acceptPayload(targetBuild, payload)) {
				targetBuild.handlePayload(targetBuild, payload);
			} else if (!net.client()) {
				unit.add();
				Units.notifyUnitSpawn(unit);
			}

			createSound.at(spawn.x, spawn.y, 1f + Mathf.range(0.06f), createSoundVolume);

			progress = 0f;
			Fx.unitAssemble.at(spawn.x, spawn.y, rotdeg() - 90f, plan.unit);
			blocks.clear();

			Events.fire(new UnitCreateEvent(unit, this));
		}

		@Override
		public void draw() {
			Draw.rect(region, x, y);

			//draw input conveyors
			for (int i = 0; i < 4; i++) {
				if (blends(i) && i != rotation) {
					Draw.rect(inRegion, x, y, (i * 90) - 180);
				}
			}

			Draw.rect(rotation >= 2 ? sideRegion2 : sideRegion1, x, y, rotdeg());

			Draw.z(Layer.blockOver);

			payRotation = rotdeg();
			drawPayload();

			Draw.z(Layer.blockOver + 0.1f);

			Draw.rect(topRegion, x, y);

			if (isPayload()) return;

			//draw drone construction
			if (droneWarmup > 0.001f) {
				Draw.draw(Layer.blockOver + 0.2f, () -> Drawf.construct(this, droneType.fullIcon, Pal.accent, 0f, droneProgress, droneWarmup, totalDroneProgress, 14f));
			}

			Vec2 spawn = getUnitSpawn();
			float sx = spawn.x, sy = spawn.y;

			AssemblerUnitPlan2 plan = plan();

			if (plan == null) return;

			//draw the unit construction as outline
			Draw.draw(Layer.blockBuilding, () -> {
				Draw.color(Pal.accent, warmup);

				Shaders.blockbuild.region = plan.unit.fullIcon;
				Shaders.blockbuild.time = Time.time;
				Shaders.blockbuild.alpha = warmup;
				//margin due to units not taking up whole region
				Shaders.blockbuild.progress = Mathf.clamp(progress + 0.05f);

				Draw.rect(plan.unit.fullIcon, sx, sy, rotdeg() - 90f);
				Draw.flush();
				Draw.color();
				Shaders.blockbuild.alpha = 1f;
			});

			Draw.reset();

			Draw.z(Layer.buildBeam);

			//draw unit silhouette
			Draw.mixcol(Tmp.c1.set(Pal.accent).lerp(Pal.remove, invalidWarmup), 1f);
			Draw.alpha(Math.min(powerWarmup, sameTypeWarmup));
			Draw.rect(plan.unit.fullIcon, spawn.x, spawn.y, rotdeg() - 90f);

			//build beams do not draw when invalid
			Draw.alpha(Math.min(1f - invalidWarmup, warmup));

			//draw build beams
			for (Unit unit : units) {
				if (!((AssemblerAI) unit.controller()).inPosition()) continue;

				float
						px = unit.x + Angles.trnsx(unit.rotation, unit.type.buildBeamOffset),
						py = unit.y + Angles.trnsy(unit.rotation, unit.type.buildBeamOffset);

				Drawf.buildBeam(px, py, spawn.x, spawn.y, plan.unit.hitSize / 2f);
			}

			//fill square in middle
			Fill.square(spawn.x, spawn.y, plan.unit.hitSize / 2f);

			Draw.reset();

			Draw.z(Layer.buildBeam);

			float fulls = areaSize * tilesize / 2f;

			//draw full area
			Lines.stroke(2f, Pal.accent);
			Draw.alpha(powerWarmup);
			Drawf.dashRectBasic(spawn.x - fulls, spawn.y - fulls, fulls * 2f, fulls * 2f);

			Draw.reset();

			float outSize = plan.unit.hitSize + 9f;

			if (invalidWarmup > 0) {
				//draw small square for area
				Lines.stroke(2f, Tmp.c3.set(Pal.accent).lerp(Pal.remove, invalidWarmup).a(invalidWarmup));
				Drawf.dashSquareBasic(spawn.x, spawn.y, outSize);
			}

			Draw.reset();
		}

		public boolean checkSolid(Vec2 v, boolean same) {
			UnitType output = unit();
			if (output == null) return false;
			float hitSize = output.hitSize * 1.4f;
			return ((!output.flying && collisions.overlapsTile(Tmp.r1.setCentered(v.x, v.y, output.hitSize), EntityCollisions::solid)) ||
					Units.anyEntities(v.x - hitSize / 2f, v.y - hitSize / 2f, hitSize, hitSize, u -> (!same || u.type != output) && !u.spawnedByCore &&
							((u.type.allowLegStep && output.allowLegStep) || (output.flying && u.isFlying()) || (!output.flying && u.isGrounded()))));
		}

		public boolean ready() {
			return efficiency > 0 && !wasOccupied;
		}

		public void yeetPayload(Payload payload) {
			var spawn = getUnitSpawn();
			blocks.add(payload.content(), 1);
			float rot = payload.angleTo(spawn);
			Fx.shootPayloadDriver.at(payload.x(), payload.y(), rot);
			Fx.payloadDeposit.at(payload.x(), payload.y(), rot, new YeetData(spawn.cpy(), payload.content()));
			Sounds.shootPayload.at(x, y, 1f + Mathf.range(0.1f), 1f);
		}

		@Override
		public Object config() {
			return currentPlan;
		}

		@Override
		public BlockStatus status() {
			if (!team.activateUnitFactories()) return BlockStatus.inactiveUnitFactory;
			return super.status();
		}

		@Override
		public double sense(LAccess sensor) {
			if (sensor == LAccess.progress) return progress;
			return super.sense(sensor);
		}

		@Override
		public boolean acceptUnitPayload(Unit unit) {
			AssemblerUnitPlan2 plan = plan();
			return plan != null && plan.requirements != null && plan.requirements.contains(b -> b.item == unit.type() &&
					blocks.get(unit.type()) < Mathf.round(b.amount * state.rules.unitCost(team)));
		}

		@Override
		public PayloadSeq getPayloads() {
			return blocks;
		}

		@Override
		public boolean acceptPayload(Building source, Payload payload) {
			AssemblerUnitPlan2 plan = plan();
			return plan != null && (this.payload == null) &&
					plan.requirements != null && plan.requirements.contains(b -> b.item == payload.content() &&
							blocks.get(payload.content()) < Mathf.round(b.amount * state.rules.unitCost(team)));
		}

		@Override
		public boolean acceptItem(Building source, Item item) {
			AssemblerUnitPlan2 plan = plan();
			return plan != null && plan.itemReq != null && items.get(item) < getMaximumAccepted(item) &&
					Structs.contains(plan.itemReq, stack -> stack.item == item);
		}

		@Override
		public Vec2 getCommandPosition() {
			return commandPos;
		}

		@Override
		public void onCommand(Vec2 target) {
			commandPos = target;
		}

		@Override
		public void write(Writes write) {
			super.write(write);

			write.i(currentPlan);
			write.f(progress);
			write.b(units.size);
			for (Unit unit : units) {
				write.i(unit.id);
			}

			blocks.write(write);
			TypeIO.writeVecNullable(write, commandPos);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);

			currentPlan = read.i();
			progress = read.f();
			int count = read.b();
			readUnits.clear();
			for (int i = 0; i < count; i++) {
				readUnits.add(read.i());
			}
			whenSyncedUnits.clear();

			blocks.read(read);
			commandPos = TypeIO.readVecNullable(read);
		}
	}

	public static class AssemblerUnitPlan2 {
		public UnitType unit;

		public @Nullable Seq<PayloadStack> requirements;
		public ItemStack @Nullable [] itemReq;
		public LiquidStack @Nullable [] liquidReq;
		public float time;

		//public int tier;

		public AssemblerUnitPlan2() {}

		public AssemblerUnitPlan2(UnitType u) {
			unit = u;
		}

		public AssemblerUnitPlan2(UnitType u, float t) {
			unit = u;
			time = t;
		}

		public AssemblerUnitPlan2(UnitType u, float t, ItemStack @Nullable [] items) {
			unit = u;
			time = t;
			itemReq = items;
		}

		public AssemblerUnitPlan2(UnitType u, float t, @Nullable Seq<PayloadStack> req) {
			unit = u;
			time = t;
			requirements = req;
		}
	}
}
