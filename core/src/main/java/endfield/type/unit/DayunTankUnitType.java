package endfield.type.unit;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.gen.Sounds;
import org.jetbrains.annotations.Nullable;

public class DayunTankUnitType extends UnitType2 {
	public DayunTankUnitType(String name) {
		super(name);
	}

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
	public Seq<String> whitelistBlockNames = new Seq<>(String.class);
}
