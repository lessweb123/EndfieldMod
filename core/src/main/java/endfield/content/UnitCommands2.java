package endfield.content;

import endfield.ai.NullAI;
import endfield.util.FieldAccessor;
import endfield.util.Reflects;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;

import static endfield.util.GetKt.sneakyThrow;

public final class UnitCommands2 {
	public static final FieldAccessor commandControllerAccessor;

	public static UnitCommand nullUnitCommand;

	static {
		try {
			commandControllerAccessor = Reflects.newFieldAccessor(CommandAI.class.getDeclaredField("commandController"));
		} catch (NoSuchFieldException e) {
			throw sneakyThrow(e);
		}
	}

	private UnitCommands2() {}

	public static void load() {
		nullUnitCommand = new UnitCommand("nullAI", "none", u -> new NullAI());
	}
}
