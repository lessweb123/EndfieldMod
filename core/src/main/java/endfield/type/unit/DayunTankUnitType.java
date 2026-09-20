package endfield.type.unit;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.struct.ObjectSet;
import mindustry.gen.Sounds;
import org.jetbrains.annotations.Nullable;

public class DayunTankUnitType extends UnitType2 {
	public Sound truckMusic = Sounds.none;
	public float truckMusicVolume = 2f;
	public float crushEnergy = 0f;
	public float crushEnergyMax = 600f;
	public float crushEnergyEach = 60f;
	public float healFraction = 0.5f;
	public @Nullable Color ringColor;
	public float ringRadius = -1;
	public float fullHealthMultiplier = 2.5f;
	public float fullSpeedMultiplier = 2.5f;
	public boolean strengthenAfterStart = true;
	public ObjectSet<String> whitelistBlockNames = new ObjectSet<>();

	public DayunTankUnitType(String name) {
		super(name);

		whitelistBlockNames.addAll(
				"world-message", "world-switch", "world-processor", "world-cell",
				"endfield-reinforced-payload-source", "endfield-team-changer", "endfield-barrier-projector",
				"endfield-invincible-wall", "endfield-invincible-wall-large", "endfield-invincible-wall-huge", "endfield-invincible-wall-gigantic",
				"endfield-dps-wall", "endfield-dps-wall-large", "endfield-dps-wall-huge", "endfield-dps-wall-gigantic"
		);
	}
}
