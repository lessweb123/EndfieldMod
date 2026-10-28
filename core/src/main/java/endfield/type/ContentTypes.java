package endfield.type;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import endfield.util.FieldAccessor;
import endfield.util.Reflections;
import endfield.util.handler.EnumHandler;
import endfield.util.handler.FieldHandler;
import mindustry.Vars;
import mindustry.core.ContentLoader;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;

import static endfield.util.GetKt.sneakyThrow;

public final class ContentTypes {
	static final EnumHandler<ContentType> handler = EnumHandler.of(ContentType.class);

	static final FieldAccessor contentNameMapAccessor, contentMapAccessor;

	static {
		try {
			contentNameMapAccessor = Reflections.getFieldAccessor(ContentLoader.class.getDeclaredField("contentNameMap"));
			contentMapAccessor = Reflections.getFieldAccessor(ContentLoader.class.getDeclaredField("contentMap"));
		} catch (NoSuchFieldException e) {
			throw sneakyThrow(e);
		}
	}

	private ContentTypes() {}

	public static ContentType addContentType(String name, Class<? extends Content> contentClass) {
		ContentType value = handler.addEnumItemTail(name, contentClass);

		FieldHandler.setObjectStatic(ContentType.class, "all", ContentType.values());

		ObjectMap<String, MappableContent>[] contentNameMap = contentNameMapAccessor.getObject(Vars.content);
		Seq<Content>[] contentMap = contentMapAccessor.getObject(Vars.content);

		Seq<ObjectMap<String, MappableContent>> contentNameMapList = new Seq<>(contentNameMap);
		Seq<Seq<Content>> contentMapList = new Seq<>(contentMap);

		contentNameMapList.insert(value.ordinal(), new ObjectMap<>());
		contentMapList.insert(value.ordinal(), new Seq<>(Content.class));

		contentNameMapAccessor.setObject(Vars.content, contentNameMapList.toArray(ObjectMap.class));
		contentMapAccessor.setObject(Vars.content, contentMapList.toArray(Seq.class));

		return value;
	}
}
