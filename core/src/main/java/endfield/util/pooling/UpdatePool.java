package endfield.util.pooling;

import arc.Events;
import arc.struct.ObjectMap;
import mindustry.game.EventType.Trigger;

public final class UpdatePool {
	static final ObjectMap<String, Runnable> updateTasks = new ObjectMap<>();

	static {
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
