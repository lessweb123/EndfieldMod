package endfield.world.blocks.distribution;

import mindustry.world.blocks.distribution.DirectionalUnloader;

public class DirectionalUnloader2 extends DirectionalUnloader {
	public DirectionalUnloader2(String name) {
		super(name);
	}

	public class DirectionalUnloaderBuild2 extends DirectionalUnloaderBuild {
		public float counter;

		@Override
		public void updateTile() {
			counter += edelta();
			final float limit = 60f / speed;

			while (counter >= limit) {
				unloadTimer = speed;
				super.updateTile();
				counter -= limit;
			}
		}
	}
}
