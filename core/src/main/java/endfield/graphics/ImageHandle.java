package endfield.graphics;

import arc.graphics.Pixmap;

import java.io.File;

public interface ImageHandle {
	Pixmap decode(File file);

	Pixmap decode(byte[] data);

	boolean isSupported(String mimeType);
}
