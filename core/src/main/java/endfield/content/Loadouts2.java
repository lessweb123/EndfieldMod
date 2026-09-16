package endfield.content;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class Loadouts2 {
	public static Schematic basicCripple;

	private Loadouts2() {}

	public static void load() {
		try {
			basicCripple = readBase64("bXNjaAF4nDWKwQqAIBAFX4sU1LnP8FP6guhguoGwrWLeon8PieY0MAMCGYw+aWWti8ug+0Evbme5QOsGo+5kDL7EnIUxBb6a15gU6DCzhiOyBOtTYftvLX28HtoZ5w==");
			Vars.schematics.getLoadouts().get(Blocks2.coreShatter, () -> new Seq<>(Schematic.class)).add(basicCripple);
		} catch (Exception e) {
			Log.err(e);
		}
	}

	public static Schematic readBase64(String schematic) throws IOException {
		return Schematics.read(new ByteArrayInputStream(Base64Coder.decode(schematic.trim())));
	}
}
