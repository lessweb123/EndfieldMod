package endfield.entities.effect;

import arc.Events;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Time;
import endfield.entities.effect.VapourizeEffect.VapourizeEffectState;
import endfield.math.Mathm;
import mindustry.content.Liquids;
import mindustry.entities.Puddles;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.gen.Building;
import mindustry.gen.Entityc;
import mindustry.gen.Unit;

import java.util.Iterator;

public final class ExtraEffect {
	private static final Seq<BuildQueue> vapourizeQueue = new Seq<>(true, 512, BuildQueue.class);
	private static final IntMap<BuildQueue> buildQMap = new IntMap<>();
	private static final IntMap<VapourizeEffectState> vapourizeMap = new IntMap<>();

	static {
		Events.on(WorldLoadEvent.class, event -> {
			vapourizeQueue.clear();
			buildQMap.clear();
			vapourizeMap.clear();
		});
		Events.run(Trigger.update, () -> {
			for (BuildQueue queue : vapourizeQueue) {
				Building temp = queue.build;
				if (!temp.isValid()) {
					int size = temp.block.size;
					Puddles.deposit(temp.tile, Liquids.slag, size * size * 2 + 6);
				}
				queue.time -= Time.delta;
			}

			Iterator<BuildQueue> iter = vapourizeQueue.iterator();
			while (iter.hasNext()) {
				BuildQueue queue = iter.next();
				if (queue.build.dead || queue.time <= 0f) {
					buildQMap.remove(queue.build.id);
					iter.remove();
				}
			}
		});
	}

	private ExtraEffect() {}

	public static void addMoltenBlock(Building build) {
		BuildQueue tmp = buildQMap.get(build.id);
		if (tmp == null) {
			tmp = new BuildQueue(build);
			vapourizeQueue.add(tmp);
			buildQMap.put(build.id, tmp);
		} else {
			tmp.time = 14.99f;
		}
		//BuildQueue temp = vapourizeQueue.find(bq -> bq.build == build);
		//if (temp == null) vapourizeQueue.add(new BuildQueue(build));
		//else temp.time = 14.99f;
	}

	public static void createEvaporation(float x, float y, Unit host, Entityc influence) {
		createEvaporation(x, y, 0.001f, host, influence);
	}

	public static void createEvaporation(float x, float y, float strength, Unit host, Entityc influence) {
		if (host == null || influence == null) return;
		VapourizeEffectState tmp = vapourizeMap.get(host.id);
		if (tmp == null) {
			tmp = new VapourizeEffectState(x, y, host, influence);
			vapourizeMap.put(host.id, tmp);
			tmp.add();
		} else {
			//tmp.time = Math.min(tmp.lifetime / 2f, tmp.time);
			tmp.extraAlpha = Mathm.clamp((strength * Time.delta) + tmp.extraAlpha);
			tmp.time = Mathf.lerpDelta(tmp.time, tmp.lifetime / 2f, 0.3f);
		}
		//new VapourizeEffectState(x, y, host, influence).add();
	}

	public static void removeEvaporation(int id) {
		vapourizeMap.remove(id);
	}

	//TODO separate this or not?
	public static class BuildQueue {
		public Building build;
		public float time;

		public BuildQueue(Building build, float time) {
			this.build = build;
			this.time = time;
		}

		public BuildQueue(Building build) {
			this(build, 14.99f);
		}
	}
}
