package endfield.util.aspector.classes;

import kotlin.Pair;

import java.util.NoSuchElementException;

public class EnumValue<T extends Enum<T>> extends AnnotationValue<T, Pair<ClassName, String>> {
	public final ClassName enumClassName;
	public final String enumConstName;

	public EnumValue(ClassName className, String constName) {
		enumClassName = className;
		enumConstName = constName;
	}

	@Override
	public ClassName getType() {
		return enumClassName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T value() {
		try {
			for (Object cons : Class.forName(enumClassName.name()).getEnumConstants()) {
				if (((Enum<?>) cons).name().equals(enumConstName)) return (T) cons;
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		throw new NoSuchElementException(enumConstName);
	}

	@Override
	public Pair<ClassName, String> rawValue() {
		return new Pair<>(enumClassName, enumConstName);
	}
}
