package endfield.graphics;

import mindustry.graphics.MultiPacker;

public interface AtlasPackHandle {
	void getPack();

	default void getPack(MultiPacker multiPacker) {
		getPack();
	}
}
