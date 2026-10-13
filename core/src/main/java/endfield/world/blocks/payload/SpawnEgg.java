package endfield.world.blocks.payload;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.io.TypeIO;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;

import static mindustry.Vars.content;
import static mindustry.Vars.renderer;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class SpawnEgg extends PayloadBlock {
	public TextureRegion arrowRegion;

	public @Nullable UnitType presetUnitType;

	public SpawnEgg(String name) {
		super(name);

		commandable = true;
		clearOnDoubleTap = true;
		configurable = true;
		selectionRows = selectionColumns = 8;
		rotate = true;
		targetable = false;
		solid = false;
		placeableLiquid = true;
		floating = true;
		underBullets = true;
		update = true;
		noUpdateDisabled = true;
		rotateDraw = false;
		rotateDrawEditor = false;
		rebuildable = false;
		destroyEffect = Fx.none;
		destroySoundVolume = 0f;
		baseShake = 0f;
		createRubble = false;
		drawTeamOverlay = false;

		config(UnitType.class, (SpawnEggBuild build, UnitType unit) -> {
			if (canProduce(unit) && build.unit != unit) {
				build.unit = unit;
			}
			if (build.command != null && (build.unit == null || !build.unit.commands.contains(build.command))) {
				build.command = null;
			}
		});
		configClear((SpawnEggBuild build) -> {
			build.unit = null;
			build.command = null;
		});
	}

	public boolean canProduce(UnitType u) {
		return true;
	}

	@Override
	public void load() {
		super.load();
		arrowRegion = Core.atlas.find(name + "-arrow");
	}

	public class SpawnEggBuild extends PayloadBlock.PayloadBlockBuild<Payload> {
		public UnitType unit;
		public Block configBlock;
		public @Nullable Vec2 commandPos;
		public @Nullable UnitCommand command;
		private boolean spawned = false;

		@Override
		public Vec2 getCommandPosition() {
			return commandPos;
		}

		@Override
		public void onCommand(Vec2 target) {
			commandPos = target;
		}

		public boolean canSetCommand() {
			var output = unit;
			return output != null && output.commands.size > 1 && output.allowChangeCommands &&
					//to avoid cluttering UI, don't show command selection for "standard" units that only have two commands.
					!(output.commands.size == 2 && output.commands.get(1) == UnitCommand.enterPayloadCommand);
		}

		@Override
		public void buildConfiguration(Table table) {
			ItemSelection.buildTable(SpawnEgg.this, table,
					(content.units().select(SpawnEgg.this::canProduce).as()),
					() -> unit == null ? configBlock : unit, this::configure, selectionRows, selectionColumns);
		}

		@Override
		public Object config() {
			return unit == null ? configBlock : unit;
		}

		@Override
		public boolean acceptPayload(Building source, Payload payload) {
			return false;
		}

		@Override
		public void updateTile() {
			if (presetUnitType != null) {
				unit = presetUnitType;
			}

			if (unit != null && !state.isPaused() && !state.isEditor() && isValid() && enabled && !spawned) {
				UnitType spawnUnit = unit;
				Vec2 spawnCommandPos = commandPos;
				float spawnX = tile.worldx();
				float spawnY = tile.worldy();
				Team spawnTeam = team;
				float spawnRot = rotdeg();

				Unit u = spawnUnit.create(spawnTeam);
				u.set(spawnX, spawnY);
				Events.fire(new EventType.UnitCreateEvent(u, this, null));
				if (!Vars.net.client()) {
					u.add();
					Units.notifyUnitSpawn(u);
				}
				u.rotation = spawnRot;
				if (u.isCommandable()) {
					if (spawnCommandPos != null) {
						u.command().commandPosition(spawnCommandPos);
					}
					u.command().command(command == null && u.type.defaultCommand != null ? u.type.defaultCommand : command);
				}
				spawned = true;
				kill();
			}
			if (spawned) kill();
		}

		@Override
		public void drawTeamTop() {
			float a = Draw.getColorAlpha();
			if (teamRegions[team().id] == teamRegion) Draw.color(team().color, a);
			Draw.rect(teamRegions[team().id], x, y);
			Draw.color(1f, 1f, 1f, a);
			Draw.color();
		}

		@Override
		public void draw() {
			super.draw();
			if (unit != null) {
				if (unit.uiIcon.width >= unit.uiIcon.height) {
					float scale = (float) unit.uiIcon.height / unit.uiIcon.width;
					Draw.rect(unit.uiIcon, x, y, tilesize, tilesize * scale, rotdeg() - 90f);
				} else {
					float scale = (float) unit.uiIcon.width / unit.uiIcon.height;
					Draw.rect(unit.uiIcon, x, y, tilesize * scale, tilesize, rotdeg() - 90f);
				}
			}
		}

		@Override
		public void drawSelect() {
			Draw.z(Layer.overlayUI);
			if (commandPos != null) {
				Draw.color(team.color);
				Draw.alpha(0.75f);
				Lines.stroke(2f);
				Lines.line(x, y, commandPos.x, commandPos.y);
				Lines.poly(commandPos.x, commandPos.y, 4, tilesize);
				Draw.alpha(1f);
			}
			Draw.color(Color.white);
			Draw.rect(arrowRegion, x, y, tilesize, tilesize, rotdeg());
			Draw.color();
			if (renderer.pixelate || unit == null) return;

			Font font = Fonts.outline;
			GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
			boolean ints = font.usesIntegerPositions();
			font.getData().setScale(1 / 4f / Scl.scl(1f));
			font.setUseIntegerPositions(false);

			String text = team.emoji + unit.localizedName;

			layout.setText(font, text, Color.white, 90f, Align.left, true);
			float offset = 1f;

			Draw.color(0f, 0f, 0f, 0.2f);
			Fill.rect(x, y - tilesize / 2f - layout.height / 2f - offset, layout.width + offset * 2f, layout.height + offset * 2f);
			Draw.color();
			font.setColor(Color.white);
			font.draw(text, x - layout.width / 2f, y - tilesize / 2f - offset, 90f, Align.left, true);
			font.setUseIntegerPositions(ints);

			font.getData().setScale(1f);

			Pools.free(layout);
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			write.s(unit == null ? -1 : unit.id);
			TypeIO.writeVecNullable(write, commandPos);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			unit = content.unit(read.s());
			commandPos = TypeIO.readVecNullable(read);
		}
	}
}
