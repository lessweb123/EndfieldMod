package endfield.graphics;

import arc.files.Fi;
import arc.graphics.Pixmap;

public interface ImageHandle {
	Pixmap decode(Fi file);

	Pixmap decode(byte[] data);

	boolean isSupported(String mimeType);
}
