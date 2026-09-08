package endfield.util.aspector;

public @interface Stub {
	Class<?> attacheTo() default void.class;
}
