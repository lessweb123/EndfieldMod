package endfield.input;

import arc.input.KeyBind;
import arc.input.KeyCode;

public class KeyBinds {
	public KeyBind hotKey = KeyBind.add("endfield_hot_key", KeyCode.controlLeft, "endfield");

	public void load() {
		hotKey.load();
	}

	public void reset(String name) {
		if ("hot_key".equals(name)) hotKey.resetToDefault();
		save();
	}

	public void save() {
		hotKey.save();
	}

	public void resetAll() {
		hotKey.resetToDefault();
		save();
	}
}
