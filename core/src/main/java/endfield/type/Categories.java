package endfield.type;

import endfield.util.handler.EnumHandler;
import mindustry.type.Category;

public final class Categories {
	public static final EnumHandler<Category> HANDLER = new EnumHandler<>(Category.class);

	public static Category environment;

	private Categories() {}

	public static void load() {
		environment = addCategory("environment");
	}

	public static Category addCategory(String name) {
		return HANDLER.addEnumItemTail(name);
	}
}
