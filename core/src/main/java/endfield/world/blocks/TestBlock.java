package endfield.world.blocks;

import arc.struct.Seq;
import endfield.util.Get;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;

public class TestBlock extends Block implements IBlock {
	public TestBlock(String name) {
		super(name);

		update = true;
		targetable = false;
	}

	@Override
	public void init() {
		Get.init(this);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	protected void initBuilding() {
		Get.initBuilding(this);
	}

	public class TestBuild extends Building implements IBuilding {
		@Override
		public Block block() {
			return TestBlock.this;
		}

		@Override
		public Tile tile() {
			return tile;
		}

		@Override
		public float efficiency() {
			return efficiency;
		}

		@Override
		public Seq<Building> proximity() {
			return proximity;
		}
	}
}
