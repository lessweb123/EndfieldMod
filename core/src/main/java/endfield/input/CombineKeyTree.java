package endfield.input;

import arc.Input;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Cons2;
import arc.input.KeyCode;
import arc.struct.ObjectMap;
import arc.util.Nullable;
import endfield.util.Arrays2;
import kotlin.Pair;

public class CombineKeyTree<T> {
	final ObjectMap<CombinedKeys, T> tempMap = new ObjectMap<>();

	final ObjectMap<CombinedKeys, T> normalBindings = new ObjectMap<>();
	final ObjectMap<CombinedKeys, T> ctrlBindings = new ObjectMap<>();
	final ObjectMap<CombinedKeys, T> altBindings = new ObjectMap<>();
	final ObjectMap<CombinedKeys, T> shiftBindings = new ObjectMap<>();
	final ObjectMap<CombinedKeys, T> altCtrlBindings = new ObjectMap<>();
	final ObjectMap<CombinedKeys, T> ctrlShiftBindings = new ObjectMap<>();
	final ObjectMap<CombinedKeys, T> altShiftBindings = new ObjectMap<>();
	final ObjectMap<CombinedKeys, T> altCtrlShiftBindings = new ObjectMap<>();

	public final void putKeyBinding(CombinedKeys binding, T rec) {
		if (binding.isShift) {
			if (binding.isAlt && binding.isCtrl) altCtrlShiftBindings.put(binding, rec);
			else if (binding.isAlt) altShiftBindings.put(binding, rec);
			else if (binding.isCtrl) ctrlShiftBindings.put(binding, rec);
			else shiftBindings.put(binding, rec);
		} else {
			if (binding.isAlt && binding.isCtrl) altCtrlBindings.put(binding, rec);
			else if (binding.isAlt) altBindings.put(binding, rec);
			else if (binding.isCtrl) ctrlBindings.put(binding, rec);
			else normalBindings.put(binding, rec);
		}
	}

	@SuppressWarnings("unchecked")
	public final void putKeyBinds(Pair<CombinedKeys, T>... bindings) {
		for (Pair<CombinedKeys, T> binding : bindings) {
			putKeyBinding(binding.getFirst(), binding.getSecond());
		}
	}

	public final void clear() {
		normalBindings.clear();
		altBindings.clear();
		ctrlBindings.clear();
		shiftBindings.clear();
		altShiftBindings.clear();
		ctrlShiftBindings.clear();
		altCtrlBindings.clear();
		altCtrlShiftBindings.clear();
	}

	public final void each(Cons2<CombinedKeys, T> block) {
		normalBindings.each(block);
		ctrlBindings.each(block);
		altBindings.each(block);
		altCtrlBindings.each(block);
		ctrlShiftBindings.each(block);
		altShiftBindings.each(block);
		altCtrlShiftBindings.each(block);
	}

	public final boolean containsKeyCode(KeyCode key) {
		if (key == null) return false;

		if (CombinedKeys.isCtrl(key))
			return !ctrlBindings.isEmpty() || !ctrlShiftBindings.isEmpty() || !altCtrlBindings.isEmpty() || !altCtrlShiftBindings.isEmpty();
		if (CombinedKeys.isAlt(key))
			return !altBindings.isEmpty() || !altShiftBindings.isEmpty() || !altCtrlBindings.isEmpty() || !altCtrlShiftBindings.isEmpty();
		if (CombinedKeys.isShift(key))
			return !shiftBindings.isEmpty() || !ctrlShiftBindings.isEmpty() || !altShiftBindings.isEmpty() || !altCtrlShiftBindings.isEmpty();

		Boolf<ObjectMap.Entry<CombinedKeys, T>> func = k -> k.key.key == key;

		return Arrays2.any(normalBindings, func)
				|| Arrays2.any(ctrlBindings, func)
				|| Arrays2.any(altBindings, func)
				|| Arrays2.any(shiftBindings, func)
				|| Arrays2.any(altCtrlBindings, func)
				|| Arrays2.any(ctrlShiftBindings, func)
				|| Arrays2.any(altShiftBindings, func)
				|| Arrays2.any(altCtrlShiftBindings, func);
	}

	public final ObjectMap<CombinedKeys, T> getTargetBindings(Input input) {
		return getTargetBindings(input, false);
	}

	public final ObjectMap<CombinedKeys, T> getTargetBindings(Input input, boolean fuzzyMatch) {
		return getTargetBindings(input.ctrl(), input.alt(), input.shift(), fuzzyMatch);
	}

	public final ObjectMap<CombinedKeys, T> getTargetBindings(CombinedKeys input, boolean fuzzyMatch) {
		return getTargetBindings(input.isCtrl, input.isAlt, input.isShift, fuzzyMatch);
	}

	public final ObjectMap<CombinedKeys, T> getTargetBindings(CombinedKeys input) {
		return getTargetBindings(input, false);
	}

	public final ObjectMap<CombinedKeys, T> getTargetBindings(boolean ctrlDown, boolean altDown, boolean shiftDown) {
		return getTargetBindings(ctrlDown, altDown, shiftDown, false);
	}

	public final ObjectMap<CombinedKeys, T> getTargetBindings(boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fuzzyMatch) {
		tempMap.clear();
		if (fuzzyMatch) {
			tempMap.putAll(normalBindings);
			if (ctrlDown) {
				tempMap.putAll(ctrlBindings);
				if (altDown) {
					tempMap.putAll(altCtrlBindings);
					if (shiftDown) tempMap.putAll(altCtrlShiftBindings);
				} else if (shiftDown) tempMap.putAll(ctrlShiftBindings);
			} else {
				if (altDown) {
					tempMap.putAll(altBindings);
					if (shiftDown) tempMap.putAll(altShiftBindings);
				} else if (shiftDown) tempMap.putAll(shiftBindings);
			}
			return tempMap;
		} else {
			return shiftDown ?
					(altDown ? (ctrlDown ? altCtrlShiftBindings : altShiftBindings)
							: (ctrlDown ? ctrlShiftBindings : shiftBindings))
					: (altDown ? (ctrlDown ? altCtrlBindings : altBindings)
					: (ctrlDown ? ctrlBindings : normalBindings));

		}
	}

	public final void eachTargetBindings(Input input, boolean fuzzyMatch, Cons2<CombinedKeys, T> cons) {
		for (var entry : getTargetBindings(input, fuzzyMatch).entries()) cons.get(entry.key, entry.value);
	}

	public final void eachTargetBindings(Input input, Cons2<CombinedKeys, T> cons) {
		eachTargetBindings(input, false, cons);
	}

	public final void eachDown(Input input, boolean fuzzyMatch, Cons<T> cons) {
		for (var entry : getTargetBindings(input, fuzzyMatch).entries()) {
			if (input.keyDown(entry.key.key)) cons.get(entry.value);
		}
	}

	public final void eachRelease(Input input, boolean fuzzyMatch, Cons<T> cons) {
		for (var entry : getTargetBindings(input, fuzzyMatch).entries()) {
			if (input.keyRelease(entry.key.key)) cons.get(entry.value);
		}
	}

	public final void eachTap(Input input, boolean fuzzyMatch, Cons<T> cons) {
		for (var entry : getTargetBindings(input, fuzzyMatch).entries()) {
			if (input.keyTap(entry.key.key)) cons.get(entry.value);
		}
	}

	public final @Nullable T checkDown(Input input) {
		for (var entry : getTargetBindings(input, false).entries()) {
			if (input.keyDown(entry.key.key)) return entry.value;
		}
		return null;
	}

	public final @Nullable T checkRelease(Input input) {
		for (var entry : getTargetBindings(input, false).entries()) {
			if (input.keyRelease(entry.key.key)) return entry.value;
		}
		return null;
	}

	public final @Nullable T checkTap(Input input) {
		for (var entry : getTargetBindings(input, false).entries()) {
			if (input.keyTap(entry.key.key)) return entry.value;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		each((keys, rec) -> buf.append(keys).append(" -> ").append(rec).append(",\n"));
		return buf.toString();
	}
}
