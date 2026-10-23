package endfield.type;

import endfield.util.handler.EnumHandler;
import endfield.util.handler.FieldHandler;
import mindustry.type.Category;

public final class Categories {
	static final EnumHandler<Category> handler = EnumHandler.of(Category.class);

	public static Category environment;

	private Categories() {}

	public static void load() {
		environment = addCategory("environment");
	}

	public static Category addCategory(String name) {
		Category category = handler.addEnumItemTail(name);
		FieldHandler.setObjectStatic(Category.class, "all", Category.values());
		return category;
	}
}
