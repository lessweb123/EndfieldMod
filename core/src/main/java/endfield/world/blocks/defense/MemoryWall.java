package endfield.world.blocks.defense;

import arc.Core;
import arc.util.Strings;
import arc.util.Time;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import mindustry.world.meta.StatUnit;

public class MemoryWall extends Wall {
	public float hitArmorUp = 1;
	public float maxArmor = 10;

	public float regenDelay = 60f * 2;

	public float regenAmount = 0f, regenPercent = 0f;

	public MemoryWall(String name) {
		super(name);
		update = true;
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.add(new Stat("wallmaxarmor", StatCat.function), maxArmor);
		stats.add(new Stat("regendelay", StatCat.function), (int) (regenDelay / 60f), StatUnit.seconds);

		if (regenPercent > 0) {
			stats.add(Stat.repairTime, (int) (1f / (regenPercent / 100f) / 60f), StatUnit.seconds);
		}
		if (regenAmount > 0) {
			stats.add(Stat.repairTime, (int) (health / regenAmount / 60f), StatUnit.seconds);
		}
	}

	@Override
	public void setBars() {
		super.setBars();
		addBar("healdelay", (MemoryWallBuild e) -> new Bar(
				() -> Core.bundle.format("bar.healdelay", Strings.fixed(regenDelay / 60f, 0)),
				() -> e.charge > 0 ? Pal.heal : Pal.health,
				() -> e.charge > 0 ? (regenDelay - e.charge) / regenDelay : 1
		));
		addBar("armor", (MemoryWallBuild e) -> new Bar(
				() -> Core.bundle.format("bar.armorup", Strings.fixed(armor, 0)),
				() -> Pal.yellowBoltFront,
				() -> armor / maxArmor
		));
	}

	public class MemoryWallBuild extends WallBuild {
		public float charge = 0f;
		public float basicArmor = armor;

		@Override
		public void updateTile() {
			super.updateTile();

			if ((armor > basicArmor || health() < maxHealth()) && !wasRecentlyDamaged()) {
				charge += Time.delta;

				if (charge >= regenDelay) {
					heal((this.maxHealth * regenPercent / 100f + regenAmount) * Time.delta);
					armor = 0;
				}
			} else {
				charge = 0;
			}

		}

		@Override
		public boolean collision(Bullet bullet) {
			super.collision(bullet);

			if (hitArmorUp > 0f) {
				if (maxArmor > 0f) {
					armor = Math.min(maxArmor, armor + hitArmorUp);
				} else {
					armor += hitArmorUp;
				}
			} else if (hitArmorUp < 0f) {
				if (maxArmor >= 0f) {
					armor = Math.max(maxArmor, armor + hitArmorUp);
				} else {
					armor += hitArmorUp;
				}
			}
			return true;
		}
	}
}
