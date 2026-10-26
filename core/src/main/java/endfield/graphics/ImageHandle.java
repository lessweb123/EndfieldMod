package endfield.graphics;

import arc.files.Fi;
import arc.graphics.Pixmap;

public interface ImageHandle {
	default Pixmap decode(Fi file) {
		return new Pixmap(file);
	}

	default Pixmap decode(byte[] data) {
		return new Pixmap(data);
	}

	default boolean isSupported(String mimeType) {
		return "image/png".equals(mimeType);
	}
}
