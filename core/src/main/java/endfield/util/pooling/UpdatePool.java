package endfield.util.pooling;

import arc.Events;
import arc.struct.OrderedMap;
import mindustry.game.EventType.Trigger;

public final class UpdatePool {
	static final OrderedMap<String, Runnable> updateTasks = new OrderedMap<>();

	static {
		updateTasks.orderedKeys().ordered = false;

		Events.run(Trigger.update, UpdatePool::update);
	}

	private UpdatePool() {}

	public static void receive(String key, Runnable task) {
		updateTasks.put(key, task);
	}

	public static boolean remove(String key) {
		return updateTasks.remove(key) != null;
	}

	public static void update() {
		for (Runnable task : updateTasks.values()) {
			task.run();
		}
	}
}
