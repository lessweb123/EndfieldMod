package endfield.content;

import endfield.ai.NullAI;
import endfield.util.FieldAccessor;
import endfield.util.Reflects;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;

public final class UnitCommands2 {
	public static final FieldAccessor COMMAND_CONTROLLER_ACCESSOR, LAST_COMMAND_ACCESSOR;

	public static UnitCommand nullUnitCommand;

	static {
		try {
			COMMAND_CONTROLLER_ACCESSOR = Reflects.newFieldAccessor(CommandAI.class.getDeclaredField("commandController"));
			LAST_COMMAND_ACCESSOR = Reflects.newFieldAccessor(CommandAI.class.getDeclaredField("lastCommand"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private UnitCommands2() {}

	public static void load() {
		nullUnitCommand = new UnitCommand("nullAI", "none", u -> new NullAI());
	}
}
