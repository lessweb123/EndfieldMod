package endfield.android;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.Source;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import endfield.graphics.ImageHandle;

import java.io.File;
import java.io.IOException;

import static endfield.util.GetKt.sneakyThrow;

public class AndroidImageHandle implements ImageHandle {
	@Override
	public Pixmap decode(File file) {
		try {
			Source source = ImageDecoder.createSource(file);
			Bitmap bitmap = ImageDecoder.decodeBitmap(source);
			return bitmapToPixmap(bitmap);
		} catch (IOException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public Pixmap decode(byte[] data) {
		try {
			Source source = ImageDecoder.createSource(data);
			Bitmap bitmap = ImageDecoder.decodeBitmap(source);
			return bitmapToPixmap(bitmap);
		} catch (IOException e) {
			throw sneakyThrow(e);
		}
	}

	@Override
	public boolean isSupported(String mimeType) {
		return ImageDecoder.isMimeTypeSupported(mimeType);
	}

	static Pixmap bitmapToPixmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Pixmap pixmap = new Pixmap(width, height);

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int argb = pixels[y * width + x];
				int a = (argb >> 24) & 0xff;
				int r = (argb >> 16) & 0xff;
				int g = (argb >> 8) & 0xff;
				int b = argb & 0xff;
				pixmap.set(x, y, Color.rgba8888(r / 255f, g / 255f, b / 255f, a / 255f));
			}
		}
		return pixmap;
	}
}
