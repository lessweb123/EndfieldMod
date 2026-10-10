package endfield.desktop;

import arc.graphics.Pixmap;
import endfield.graphics.ImageHandle;
import kotlin.NotImplementedError;

import java.io.File;

public class DesktopImageHandle implements ImageHandle {
	@Override
	public Pixmap decode(File file) {
		throw new NotImplementedError();
	}

	@Override
	public Pixmap decode(byte[] data) {
		throw new NotImplementedError();
	}

	@Override
	public boolean isSupported(String mimeType) {
		return false;
	}
}
