package endfield.android;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.ColorSpace.Named;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.OnHeaderDecodedListener;
import android.graphics.ImageDecoder.Source;
import arc.graphics.Pixmap;
import arc.util.Log;
import endfield.graphics.ImageHandle;
import endfield.graphics.Pixmaps2;

import java.io.File;
import java.io.IOException;

public class AndroidImageHandle implements ImageHandle {
	static final OnHeaderDecodedListener listener = (decoder, info, source) -> {
		decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
		decoder.setTargetColorSpace(ColorSpace.get(Named.LINEAR_SRGB));
	};

	@Override
	public Pixmap decode(File file) {
		try {
			Source source = ImageDecoder.createSource(file);
			Bitmap bitmap = ImageDecoder.decodeBitmap(source, listener);
			Pixmap pixmap = bitmapToPixmap(bitmap);
			bitmap.recycle();
			return pixmap;
		} catch (IOException e) {
			Log.err(e);
			return Pixmaps2.error;
		}
	}

	@Override
	public Pixmap decode(byte[] data) {
		try {
			Source source = ImageDecoder.createSource(data);
			Bitmap bitmap = ImageDecoder.decodeBitmap(source, listener);
			Pixmap pixmap = bitmapToPixmap(bitmap);
			bitmap.recycle();
			return pixmap;
		} catch (IOException e) {
			Log.err(e);
			return Pixmaps2.error;
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

				pixmap.setRaw(x, y, r << 24 | g << 16 | b << 8 | a);
			}
		}

		return pixmap;
	}
}
