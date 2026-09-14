package endfield.entities.bullet;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

public class BallLightningBulletType extends BasicBulletType {
	public float shockRange = 60f;
	public float shockDamage = 20f;
	public int shockAmount = 5;
	public float shockCooldown = 10f;
	public int shockLimit = 3;

	public int maxRemaining = 6;
	public float minRemainingRange = 0.15f, maxRemainingRange = 0.5f;
	public Effect shockEffect = Fx.chainLightning;
	//public boolean overflow = true;

	public float statusDuration = 60f * 2f;
	public StatusEffect shockStatus = StatusEffects.shocked;

	public Drawer bulletDrawer = null;

	public Seq<Healthc> targets = new Seq<>(Healthc.class);
	public ObjectIntMap<Healthc> shocksMap = new ObjectIntMap<>();

	public BallLightningBulletType(float speed, float damage, String sprite) {
		super(speed, damage, sprite);
	}

	public BallLightningBulletType(float speed, float damage) {
		super(speed, damage);
	}

	public BallLightningBulletType() {}

	@Override
	public void init(Bullet b) {
		super.init(b);

		if (b.data == null) {
			b.data = new BallLightningBulletData();
		}
		if (instantDisappear) {
			remaining(b, shock(b));
		}
	}

	@Override
	public void update(Bullet b) {
		super.update(b);

		if (instantDisappear) return;

		if (b.data == null) {
			b.data = new BallLightningBulletData();
		}
		if (b.data instanceof BallLightningBulletData data) {
			data.time += Time.delta;

			if (data.time >= shockCooldown) {
				data.time = 0;
				data.surplus = shock(b);

			}

			int surplus = data.surplus;

			if (surplus > 0) {
				data.surplusTime += Time.delta;
				if (data.surplusTime > shockCooldown / surplus) {
					data.surplusTime = 0;
					remaining(b, 1);
				}
			}
		}
	}

	public int shock(Bullet b) {
		targets.clear();
		Units.nearbyEnemies(b.team, b.x, b.y, shockRange, enemy -> {
			if (!enemy.dead() && enemy.isValid() && enemy.targetable(b.team)) {
				targets.add(enemy);
			}
		});
		Units.nearbyBuildings(b.x, b.y, shockRange, build -> {
			if (build.team != b.team && build.isValid() && build.block.targetable) {
				targets.add(build);
			}
		});
		int remainingStrikes = shockAmount;

		if (targets.size > 0) {
			shocksMap.clear();
			for (Healthc u : targets) {
				shocksMap.put(u, 0);
			}

			int currentIndex = Mathf.random(targets.size - 1);

			while (remainingStrikes > 0 && targets.size > 0) {
				currentIndex %= targets.size;
				if (currentIndex < 0) currentIndex = 0;

				Healthc target = targets.get(currentIndex);

				if (targets.get(currentIndex) instanceof Unit utarget) {
					if (utarget.dead() || !utarget.isValid() || !utarget.targetable(b.team) || shocksMap.get(utarget) >= shockLimit) {
						targets.remove(currentIndex);
						shocksMap.remove(utarget);

						if (targets.size == 0) break;

						continue;
					}
					if (shockStatus != null && shockStatus != StatusEffects.none) {
						utarget.apply(shockStatus, statusDuration);
					}
				} else if (targets.get(currentIndex) instanceof Building btarget) {
					if (btarget.dead() || !btarget.isValid() || shocksMap.get(btarget) >= shockLimit) {
						targets.remove(currentIndex);
						shocksMap.remove(btarget);

						if (targets.size == 0) break;

						continue;
					}
				}

				Vec2 endPoint = new Vec2(b.x, b.y);

				shockEffect.at(target.getX(), target.getY(), 0f, hitColor, endPoint);

				target.damage(shockDamage * b.damageMultiplier());
				shocksMap.put(target, shocksMap.get(target) + 1);

				remainingStrikes--;

				currentIndex++;
			}
		}
		return remainingStrikes;
	}

	public void remaining(Bullet b, int count) {
		count = Math.min(count, maxRemaining);
		if (count <= 0) return;
		Vec2 endPoint = new Vec2(b.x, b.y);
		Angles.randLenVectors((long) (Time.time + b.id * 114L), count, shockRange * minRemainingRange, shockRange * maxRemainingRange, (x, y) -> {
			shockEffect.at(b.getX() + x, b.getY() + y, 0f, hitColor, endPoint);
		});
	}

	public void draw(Bullet b) {
		super.draw(b);

		if (bulletDrawer != null) bulletDrawer.draw(b);
	}

	public interface Drawer {
		void draw(Bullet b);
	}

	public static class BallLightningBulletData {
		public float time = 0;
		public float surplusTime = 0;
		public int surplus = 0;
	}
}
