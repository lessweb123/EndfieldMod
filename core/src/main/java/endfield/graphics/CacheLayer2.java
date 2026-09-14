package endfield.graphics;

import mindustry.graphics.CacheLayer;
import mindustry.graphics.CacheLayer.ShaderLayer;

/**
 * Defines the {@linkplain CacheLayer cache layer}s this mod offers.
 *
 * @author LessWeb
 */
public final class CacheLayer2 {
	public static ShaderLayer brine, glacium, coldPlasma, deepColdPlasma, pit, waterPit;

	/** Don't let anyone instantiate this class. */
	private CacheLayer2() {}

	/** Loads the cache layers. */
	public static void load() {
		brine = new ShaderLayer(Shaders2.brine);
		glacium = new ShaderLayer(Shaders2.glacium);
		coldPlasma = new ShaderLayer(Shaders2.coldPlasma);
		deepColdPlasma = new ShaderLayer(Shaders2.deepColdPlasma);
		pit = new ShaderLayer(Shaders2.pit);
		waterPit = new ShaderLayer(Shaders2.waterPit);

		CacheLayer.add(brine, coldPlasma, deepColdPlasma, pit, waterPit);
	}
}
