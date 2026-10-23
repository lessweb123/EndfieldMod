package endfield.type;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import endfield.util.handler.EnumHandler;
import endfield.util.handler.FieldHandler;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;

public final class ContentTypes {
	static final EnumHandler<ContentType> handler = EnumHandler.of(ContentType.class);

	private ContentTypes() {}

	public static ContentType addContentType(String name, Class<? extends Content> contentClass) {
		ContentType value = handler.addEnumItemTail(name, contentClass);

		FieldHandler.setObjectStatic(ContentType.class, "all", ContentType.values());

		ObjectMap<String, MappableContent>[] contentNameMap = FieldHandler.getObject(Vars.content, "contentNameMap");
		Seq<Content>[] contentMap = FieldHandler.getObject(Vars.content, "contentMap");

		Seq<ObjectMap<String, MappableContent>> contentNameMapList = new Seq<>(contentNameMap);
		Seq<Seq<Content>> contentMapList = new Seq<>(contentMap);

		contentNameMapList.insert(value.ordinal(), new ObjectMap<>());
		contentMapList.insert(value.ordinal(), new Seq<>());

		FieldHandler.setObject(Vars.content, "contentNameMap", contentNameMapList.toArray(ObjectMap.class));
		FieldHandler.setObject(Vars.content, "contentMap", contentMapList.toArray(Seq.class));

		return value;
	}
}
