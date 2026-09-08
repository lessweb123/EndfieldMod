package endfield.util.aspector.classes;

public abstract class AnnotationValue<T, R> {
	public abstract ClassName getType();

	/**
	 * Will load some class or instance some object.
	 * <p>Shouldn't be call most of the time when a pure compile-time or mixin environment.
	 */
	public abstract T value();

	/**
	 * Get the annotation value raw declare, use pure compile indicated.
	 * <p>Don't need to load target class. use for mixin or compile plugin.
	 */
	public abstract R rawValue();
}
