package endfield.desktop;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import endfield.graphics.ImageHandle;
import kotlin.NotImplementedError;

public class DesktopImageHandle implements ImageHandle {
	@Override
	public Pixmap decode(Fi file) {
		return PixmapIO.readPNG(file);
	}

	@Override
	public Pixmap decode(byte[] data) {
		return PixmapIO.readPNG(data);
	}

	@Override
	public boolean isSupported(String mimeType) {
		return "image/png".equals(mimeType);
	}
}
