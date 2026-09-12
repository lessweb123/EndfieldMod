package endfield.world.blocks.logic;

import endfield.util.FieldAccessor;
import endfield.util.Reflects;
import mindustry.world.blocks.logic.MemoryBlock;

public class CopyMemoryBlock extends MemoryBlock {
	public static final FieldAccessor numberMemoryAccessor = Reflects.newFieldAccessor(MemoryBuild.class, "numberMemory");

	public CopyMemoryBlock(String name) {
		super(name);

		config(double[].class, (CopyMemoryBuild tile, double[] number) -> {
			double[] numberMemory = numberMemoryAccessor.get(tile);

			System.arraycopy(number, 0, numberMemory, 0, number.length);
		});
	}

	public class CopyMemoryBuild extends MemoryBuild {
		public double[] cacher = new double[memoryCapacity];

		@Override
		public Object config() {
			double[] numberMemory = numberMemoryAccessor.get(this);

			System.arraycopy(numberMemory, 0, cacher, 0, numberMemory.length);

			return cacher;
		}
	}
}
