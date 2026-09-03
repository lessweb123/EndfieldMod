package endfield.type;

import endfield.util.FieldAccessor;
import endfield.util.Reflects;
import endfield.util.handler.ClassHandler;
import endfield.util.handler.EnumHandler;
import mindustry.type.Category;

public final class Categories {
	public static final EnumHandler<Category> HANDLER = new EnumHandler<>(Category.class);
	public static final FieldAccessor ALL_ACCESSOR = Reflects.newFieldAccessor(ClassHandler.getField(Category.class, "all"));

	public static Category environment;

	private Categories() {}

	public static void load() {
		environment = addCategory("environment");
	}

	public static Category addCategory(String name) {
		for (Category category : Category.all) {
			if (category.name().equals(name)) return category;
		}

		Category newCategory = HANDLER.addEnumItemTail(name);

		ALL_ACCESSOR.set(null, HANDLER.values());

		return newCategory;
	}

	public static Category getCategory(int ordinal) {
		return HANDLER.values()[ordinal];
	}

	public static Category getCategory(String name) {
		for (Category category : HANDLER.values()) {
			if (category.name().equals(name)) return category;
		}

		return null;
	}
}
