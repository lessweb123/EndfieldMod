package endfield.world.blocks.environment;

import endfield.world.meta.Attributes2;
import mindustry.type.Item;
import mindustry.world.blocks.environment.OreBlock;

public class OreVein extends OreBlock {
	public float density;

	public OreVein(String name, Item ore, float dens) {
		super(name, ore);

		density = dens;

		attributes.set(Attributes2.density, dens);
	}
}
