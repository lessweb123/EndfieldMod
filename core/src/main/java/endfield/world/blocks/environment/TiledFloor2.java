package endfield.world.blocks.environment;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Log;
import endfield.util.Sprites;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;

public class TiledFloor2 extends Floor {
	public int tilingVariants = 0;

	protected TextureRegion[][][] tilingRegions;
	protected int tilingSize;

	public TiledFloor2(String name, int variants) {
		super(name, variants);
	}

	public TiledFloor2(String name) {
		super(name);
	}

	@Override
	public void load() {
		region = Core.atlas.find(name);

		customShadowRegion = Core.atlas.find(name + "-shadow");
		teamRegion = Core.atlas.find(name + "-team");

		//load specific team regions
		teamRegions = new TextureRegion[Team.all.length];
		for (Team team : Team.all) {
			teamRegions[team.id] = teamRegion.found() && team.hasPalette ? Core.atlas.find(name + "-team-" + team.name, teamRegion) : teamRegion;
		}

		if (autotile) {
			variants = 0;
		}

		int tsize = (int) (tilesize / Draw.scl);

		if (tilingVariants > 0 && !headless) {
			tilingRegions = new TextureRegion[tilingVariants][][];
			for (int i = 0; i < tilingVariants; i++) {
				TextureRegion tile = Core.atlas.find(name + "-tile" + (i + 1));
				tilingRegions[i] = tile.split(tsize, tsize);
				tilingSize = tilingRegions[i].length;
			}

			for (int i = 0; i < tilingVariants; i++) {
				if (tilingRegions[i].length != tilingSize || tilingRegions[i][0].length != tilingSize) {
					Log.warn("Block: @: In order to prevent crashes, tiling regions must all be valid regions with the same size. Tiling has been disabled. Sprite '@' has a width or height inconsistent with other tiles.", name, name + "-tile" + (i + 1));
					tilingVariants = 0;
				}
			}
		}

		if (variants > 0) {
			variantRegions = Sprites.splitLayer(name + "-variants", 32, variants);
			for (int i = 0; i < variants; i++) {
				variantRegions[i] = Core.atlas.find(name + (i + 1));
			}
		} else {
			variantRegions = new TextureRegion[]{region};
		}

		if (Core.atlas.has(name + "-edge")) {
			edges = Core.atlas.find(name + "-edge").split(tsize, tsize);
		}
		edgeRegion = Core.atlas.find("edge");
	}

	@Override
	public void drawMain(Tile tile) {
		if (tilingVariants > 0) {
			int index = Mathf.randomSeed(Point2.pack(tile.x / tilingSize, tile.y / tilingSize), 0, tilingVariants - 1);
			TextureRegion[][] regions = tilingRegions[index];
			Draw.rect(regions[tile.x % tilingSize][tilingSize - 1 - tile.y % tilingSize], tile.worldx(), tile.worldy());
		} else {
			Draw.rect(variantRegions[variant(tile.x, tile.y)], tile.worldx(), tile.worldy());
		}
	}
}
