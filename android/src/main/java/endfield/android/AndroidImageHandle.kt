package endfield.android

import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.ImageInfo
import android.graphics.ImageDecoder.OnHeaderDecodedListener
import arc.files.Fi
import arc.graphics.Pixmap
import endfield.graphics.ImageHandle

class AndroidImageHandle : ImageHandle {
	override fun decode(file: Fi): Pixmap {
		val source = ImageDecoder.createSource(file.file())
		val bitmap = ImageDecoder.decodeBitmap(source, listener)
		val pixmap: Pixmap = bitmapToPixmap(bitmap)
		bitmap.recycle()
		return pixmap
	}

	override fun decode(data: ByteArray): Pixmap {
		val source = ImageDecoder.createSource(data)
		val bitmap = ImageDecoder.decodeBitmap(source, listener)
		val pixmap: Pixmap = bitmapToPixmap(bitmap)
		bitmap.recycle()
		return pixmap
	}

	override fun isSupported(mimeType: String): Boolean {
		return ImageDecoder.isMimeTypeSupported(mimeType)
	}

	companion object {
		@JvmField
		val listener: OnHeaderDecodedListener =
			OnHeaderDecodedListener { decoder: ImageDecoder, _: ImageInfo, _: ImageDecoder.Source ->
				decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)
				decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.LINEAR_SRGB))
			}

		@JvmStatic
		fun bitmapToPixmap(bitmap: Bitmap): Pixmap {
			val width = bitmap.width
			val height = bitmap.height
			val pixmap = Pixmap(width, height)

			val pixels = IntArray(width * height)
			bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

			for (y in 0..<height) {
				for (x in 0..<width) {
					val argb = pixels[y * width + x]
					val a = (argb shr 24) and 0xff
					val r = (argb shr 16) and 0xff
					val g = (argb shr 8) and 0xff
					val b = argb and 0xff

					pixmap.setRaw(x, y, r shl 24 or (g shl 16) or (b shl 8) or a)
				}
			}

			return pixmap
		}
	}
}
