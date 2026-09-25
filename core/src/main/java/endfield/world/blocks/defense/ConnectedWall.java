package endfield.world.blocks.defense;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import endfield.util.Sprites;
import mindustry.game.Team;
import mindustry.world.blocks.defense.Wall;

public class ConnectedWall extends Wall {
	public ConnectedWall(String name) {
		super(name);
	}

	@Override
	public void load() {
		region = Core.atlas.find(name);

		customShadowRegion = Core.atlas.find(name + "-shadow");

		teamRegion = Core.atlas.find(name + "-team");

		teamRegions = new TextureRegion[Team.all.length];
		for (Team team : Team.all) {
			teamRegions[team.id] = teamRegion.found() && team.hasPalette ? Core.atlas.find(name + "-team-" + team.name, teamRegion) : teamRegion;
		}

		if (autotile) {
			autotileRegions = Sprites.split(name + "-autotile", 32, 12, 4);
		}
	}

	public class ConnectedWallBuild extends WallBuild {
	}
}
