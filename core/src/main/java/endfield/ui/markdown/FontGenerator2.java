package endfield.ui.markdown;

import arc.Core;
import arc.files.Fi;
import arc.freetype.FreeType;
import arc.freetype.FreeType.Face;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.DistanceFieldFont;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Font.FontData;
import arc.graphics.g2d.Font.Glyph;
import arc.graphics.g2d.GlyphLayout.GlyphRun;
import arc.graphics.g2d.PixmapPacker;
import arc.graphics.g2d.PixmapPacker.GuillotineStrategy;
import arc.graphics.g2d.PixmapPacker.PackStrategy;
import arc.graphics.g2d.PixmapPacker.SkylineStrategy;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Buffers;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.io.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Generates {@link Font} and {@link FontData} instances from TrueType, OTF, and other FreeType supported fonts.
 * <p>Usage example:
 * <pre>
 * FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Core.files.internal(&quot;myfont.ttf&quot;));
 * BitmapFont font = gen.generateFont(16);
 * gen.dispose(); // Don't dispose if doing incremental glyph generation.
 * </pre>
 * The generator has to be disposed once it is no longer used. The returned {@link Font} instances are managed by the user
 * and have to be disposed as usual.
 *
 * @author mzechner
 * @author Nathan Sweet
 * @author Rob Rendell
 */
public class FontGenerator2 implements Disposable {
	public static final String DEFAULT_CHARS = "\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*\u007F\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF";

	/** A hint to scale the texture as needed, without capping it at any maximum size */
	public static final int NO_MAXIMUM = -1;

	/**
	 * The maximum texture size allowed by generateData, when storing in a texture atlas. Multiple texture pages will be created
	 * if necessary. Default is 1024.
	 *
	 * @see .setMaxTextureSize
	 */
	public static int maxTextureSize = 1024;

	public FreeType.Library library;
	public Face face;
	public String name;
	public boolean bitmapped = false;

	/**
	 * Creates a new generator from the given font file. Uses {@code Fi.length} to determine the file size. If the file
	 * length could not be determined (it was 0), an extra copy of the font bytes is performed. Throws a
	 *
	 * @throws ArcRuntimeException if loading did not succeed.
	 * @see arc.freetype.FreeTypeFontGenerator
	 */
	public FontGenerator2(Fi fontFile, int faceIndex) {
		name = fontFile.pathWithoutExtension();

		int fileSize = (int) fontFile.length();

		library = FreeType.initFreeType();

		ByteBuffer buffer = null;

		try {
			buffer = fontFile.map();
		} catch (ArcRuntimeException e) {
			// Silently error, certain platforms do not support file mapping.
		}

		if (buffer == null) {
			try (InputStream input = fontFile.read()) {
				if (fileSize == 0) {
					// Copy to a byte[] to get the file size, then copy to the buffer.
					byte[] data = Streams.copyBytes(input, 1024 * 16);
					buffer = Buffers.newUnsafeByteBuffer(data.length);
					Buffers.copy(data, 0, buffer, data.length);
				} else {
					// Trust the specified file size.
					buffer = Buffers.newUnsafeByteBuffer(fileSize);
					Streams.copy(input, buffer);
				}
			} catch (IOException ex) {
				throw new ArcRuntimeException(ex);
			}
		}

		face = library.newMemoryFace(buffer, faceIndex);

		if (!checkForBitmapFont()) setPixelSizes(0, 15);
	}

	public FontGenerator2(Fi fontFile) {
		this(fontFile, 0);
	}

	int getLoadingFlags(FontParameter2 parameter) {
		return switch (parameter.hinting) {
			case none -> FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_HINTING;
			case slight -> FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_TARGET_LIGHT;
			case medium -> FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_TARGET_NORMAL;
			case full -> FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_TARGET_MONO;
			case autoSlight ->
					FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_FORCE_AUTOHINT | FreeType.FT_LOAD_TARGET_LIGHT;
			case autoMedium ->
					FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_FORCE_AUTOHINT | FreeType.FT_LOAD_TARGET_NORMAL;
			case autoFull -> FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_FORCE_AUTOHINT | FreeType.FT_LOAD_TARGET_MONO;
		};
	}

	public boolean loadChar(int c) {
		return loadChar(c, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_FORCE_AUTOHINT);
	}

	public boolean loadChar(int c, int flags) {
		return face.loadChar(c, flags);
	}

	public boolean checkForBitmapFont() {
		int faceFlags = face.getFaceFlags();
		if (((faceFlags & FreeType.FT_FACE_FLAG_FIXED_SIZES) == FreeType.FT_FACE_FLAG_FIXED_SIZES)
				&& ((faceFlags & FreeType.FT_FACE_FLAG_HORIZONTAL) == FreeType.FT_FACE_FLAG_HORIZONTAL)) {
			if (loadChar(32)) {
				FreeType.GlyphSlot slot = face.getGlyph();
				if (slot.getFormat() == 1651078259) {
					bitmapped = true;
				}
			}
		}
		return bitmapped;
	}

	public Font generateFont(FontParameter2 parameter) {
		return generateFont(parameter, new FreeTypeFontData2());
	}

	/**
	 * Generates a new {@link Font}. The size is expressed in pixels. Throws a ArcRuntimeException if the font could not be
	 * generated. Using big sizes might cause such an exception.
	 *
	 * @param parameter configures how the font is generated
	 */
	public Font generateFont(FontParameter2 parameter, FreeTypeFontData2 data) {
		boolean updateTextureRegions = data.regions == null && parameter.packer != null;
		if (updateTextureRegions) data.regions = new Seq<>(TextureRegion.class);
		generateData(parameter, data);
		if (updateTextureRegions) parameter.packer.updateTextureRegions(
				data.regions,
				parameter.minFilter,
				parameter.magFilter,
				parameter.genMipMaps
		);
		if (data.regions.isEmpty()) throw new ArcRuntimeException("Unable to create a font with no texture regions.");

		data.padLeft += parameter.distanceFieldSpread;
		Font font = parameter.distanceFieldSpread > 0 ? new DistanceFieldFont(data, data.regions, true) : new Font(data, data.regions, true);
		font.setOwnsTexture(parameter.packer == null);

		return font;
	}

	/**
	 * Uses ascender and descender of font to calculate real height that makes all glyphs to fit in given pixel size. Source:
	 * <a href="http://nothings.org/stb/stb_truetype.h">stb_truetype.h</a> / stbtt_ScaleForPixelHeight
	 */
	public int scaleForPixelHeight(int height) {
		setPixelSizes(0, height);
		FreeType.SizeMetrics fontMetrics = face.getSize().getMetrics();
		int ascent = FreeType.toInt(fontMetrics.getAscender());
		int descent = FreeType.toInt(fontMetrics.getDescender());
		return height * height / (ascent - descent);
	}

	/**
	 * Uses max advance, ascender and descender of font to calculate real height that makes any n glyphs to fit in given pixel
	 * width.
	 *
	 * @param width    the max width to fit (in pixels)
	 * @param numChars max number of characters that to fill width
	 */
	public int scaleForPixelWidth(int width, int numChars) {
		FreeType.SizeMetrics fontMetrics = face.getSize().getMetrics();
		int advance = FreeType.toInt(fontMetrics.getMaxAdvance());
		int ascent = FreeType.toInt(fontMetrics.getAscender());
		int descent = FreeType.toInt(fontMetrics.getDescender());
		int unscaledHeight = ascent - descent;
		int height = unscaledHeight * width / (advance * numChars);
		setPixelSizes(0, height);
		return height;
	}

	public int scaleToFitSquare(int width, int height, int numChars) {
		return Math.min(scaleForPixelHeight(height), scaleForPixelWidth(width, numChars));
	}

	/**
	 * Returns null if glyph was not found. If there is nothing to render, for example with various space characters, then bitmap
	 * is null.
	 */
	public GlyphAndBitmap generateGlyphAndBitmap(int c, int size, boolean flip) {
		setPixelSizes(0, size);

		FreeType.SizeMetrics fontMetrics = face.getSize().getMetrics();
		int baseline = FreeType.toInt(fontMetrics.getAscender());

		// Check if character exists in this font.
		// 0 means 'undefined character code'
		if (face.getCharIndex(c) == 0) {
			return null;
		}

		// Try to load character
		if (!loadChar(c)) {
			throw new ArcRuntimeException("Unable to load character!");
		}

		FreeType.GlyphSlot slot = face.getGlyph();

		// Try to render to bitmap
		FreeType.Bitmap bitmap;
		if (bitmapped) {
			bitmap = slot.getBitmap();
		} else if (!slot.renderGlyph(FreeType.FT_RENDER_MODE_NORMAL)) {
			bitmap = null;
		} else {
			bitmap = slot.getBitmap();
		}

		FreeType.GlyphMetrics metrics = slot.getMetrics();

		Font.Glyph glyph = new Font.Glyph();
		if (bitmap != null) {
			glyph.width = bitmap.getWidth();
			glyph.height = bitmap.getRows();
		} else {
			glyph.width = 0;
			glyph.height = 0;
		}
		glyph.xoffset = slot.getBitmapLeft();
		glyph.yoffset = flip ? -slot.getBitmapTop() + baseline : -(glyph.height - slot.getBitmapTop()) - baseline;
		glyph.xadvance = FreeType.toInt(metrics.getHoriAdvance());
		glyph.srcX = 0;
		glyph.srcY = 0;
		glyph.id = c;

		GlyphAndBitmap result = new GlyphAndBitmap();
		result.glyph = glyph;
		result.bitmap = bitmap;
		return result;
	}

	/**
	 * Generates a new {@link FontData} instance, expert usage only. Throws a ArcRuntimeException if something went wrong.
	 *
	 * @param size the size in pixels
	 */
	public FreeTypeFontData2 generateData(int size) {
		FontParameter2 parameter = new FontParameter2();
		parameter.size = size;
		return generateData(parameter);
	}

	public void setPixelSizes(int pixelWidth, int pixelHeight) {
		if (!bitmapped && !face.setPixelSizes(pixelWidth, pixelHeight))
			throw new ArcRuntimeException("Couldn't set size for font");
	}

	public FreeTypeFontData2 generateData(FontParameter2 parameter) {
		return generateData(parameter, new FreeTypeFontData2());
	}

	/**
	 * Generates a new {@link FontData} instance, expert usage only. Throws a ArcRuntimeException if something went wrong.
	 *
	 * @param parameter configures how the font is generated
	 */
	public FreeTypeFontData2 generateData(FontParameter2 parameter, FreeTypeFontData2 data) {
		parameter = new FontParameter2();
		char[] characters = parameter.characters.toCharArray();
		int charactersLength = characters.length;
		boolean incremental = parameter.incremental;
		int flags = getLoadingFlags(parameter);

		setPixelSizes(0, parameter.size);

		// set general font data
		FreeType.SizeMetrics fontMetrics = face.getSize().getMetrics();
		data.flipped = parameter.flip;
		data.ascent = FreeType.toInt(fontMetrics.getAscender());
		data.descent = FreeType.toInt(fontMetrics.getDescender());
		data.lineHeight = FreeType.toInt(fontMetrics.getHeight());
		float baseLine = data.ascent;

		// if bitmapped
		if (bitmapped && (data.lineHeight == 0f)) {
			for (int c = 32; c < (32 + face.getNumGlyphs()); c++) {
				if (loadChar(c, flags)) {
					int lh = FreeType.toInt(face.getGlyph().getMetrics().getHeight());
					data.lineHeight = lh > data.lineHeight ? lh : data.lineHeight;
				}
			}
		}
		data.lineHeight += parameter.spaceY;

		// determine space width
		if (loadChar(' ', flags) || loadChar('l', flags)) {
			data.spaceXadvance = FreeType.toInt(face.getGlyph().getMetrics().getHoriAdvance());
		} else {
			data.spaceXadvance = face.getMaxAdvanceWidth(); // Possibly very wrong.
		}

		// determine x-height
		for (char xChar : data.xChars) {
			if (!loadChar(xChar, flags)) continue;
			data.xHeight = FreeType.toInt(face.getGlyph().getMetrics().getHeight());
			if (data.xHeight > 0) break;
		}
		if (data.xHeight == 0f) throw new ArcRuntimeException("No x-height character found in font");

		// determine cap height
		for (char capChar : data.capChars) {
			if (!loadChar(capChar, flags)) continue;
			data.capHeight =
					(FreeType.toInt(face.getGlyph().getMetrics().getHeight()));
			break;
		}
		if (!bitmapped && data.capHeight == 1f) throw new ArcRuntimeException("No cap character found in font");

		data.ascent -= data.capHeight;
		data.down = -data.lineHeight;
		if (parameter.flip) {
			data.ascent = -data.ascent;
			data.down = -data.down;
		}

		boolean ownsAtlas = false;

		PixmapPacker packer = parameter.packer;

		if (packer == null) {
			// Create a packer.
			int size;
			PackStrategy packStrategy;
			if (incremental) {
				size = maxTextureSize;
				packStrategy = new GuillotineStrategy();
			} else {
				int maxGlyphHeight = (int) Math.ceil(data.lineHeight);
				size = Mathf.nextPowerOfTwo((int) Math.sqrt(maxGlyphHeight * maxGlyphHeight * charactersLength));
				if (maxTextureSize > 0) size = Math.min(size, maxTextureSize);
				packStrategy = new SkylineStrategy();
			}
			ownsAtlas = true;
			packer = new PixmapPacker(size, size, 1, false, packStrategy);
			packer.setTransparentColor(parameter.color);
			packer.getTransparentColor().a = 0f;
			if (parameter.borderWidth > 0) {
				packer.setTransparentColor(parameter.borderColor);
				packer.getTransparentColor().a = 0f;
			}
		}

		if (incremental) data.glyphs = new Seq<>(true, charactersLength + 32, Font.Glyph.class);

		FreeType.Stroker stroker = null;
		if (parameter.borderWidth > 0) {
			stroker = library.createStroker();
			stroker.set(
					(int) (parameter.borderWidth * 64f),
					parameter.borderStraight ? FreeType.FT_STROKER_LINECAP_BUTT : FreeType.FT_STROKER_LINECAP_ROUND,
					parameter.borderStraight ? FreeType.FT_STROKER_LINEJOIN_MITER_FIXED : FreeType.FT_STROKER_LINEJOIN_ROUND,
					0
			);
		}

		// Create glyphs largest height first for best packing.
		int[] heights = new int[charactersLength];
		for (int i = 0; i < charactersLength; i++) {
			char c = characters[i];

			int height = loadChar(c, flags) ? FreeType.toInt(face.getGlyph().getMetrics().getHeight()) : 0;
			heights[i] = height;

			if (c == '\u0000') {
				Font.Glyph missingGlyph = createGlyph('\u0000', data, parameter, stroker, baseLine, packer);
				if (missingGlyph != null && missingGlyph.width != 0 && missingGlyph.height != 0) {
					data.setGlyph('\u0000', missingGlyph);
					data.missingGlyph = missingGlyph;
					if (incremental) data.glyphs.add(missingGlyph);
				}
			}
		}
		int heightsCount = heights.length;
		while (heightsCount > 0) {
			int best = 0;
			int maxHeight = heights[0];
			for (int i = 1; i < heightsCount; i++) {
				int height = heights[i];
				if (height > maxHeight) {
					maxHeight = height;
					best = i;
				}
			}

			char c = characters[best];
			if (data.getGlyph(c) == null) {
				Font.Glyph glyph = createGlyph(c, data, parameter, stroker, baseLine, packer);
				if (glyph != null) {
					data.setGlyph(c, glyph);
					if (incremental) data.glyphs.add(glyph);
				}
			}

			heightsCount--;
			heights[best] = heights[heightsCount];
			char tmpChar = characters[best];
			characters[best] = characters[heightsCount];
			characters[heightsCount] = tmpChar;
		}

		if (stroker != null && !incremental) stroker.dispose();

		if (incremental) {
			data.generator = this;
			data.parameter = parameter;
			data.stroker = stroker;
			data.packer = packer;
		}

		// Generate kerning.
		parameter.kerning = parameter.kerning & face.hasKerning();
		if (parameter.kerning) {
			for (int i = 0; i < charactersLength; i++) {
				char firstChar = characters[i];
				Font.Glyph first = data.getGlyph(firstChar);

				if (first == null) continue;

				int firstIndex = face.getCharIndex(firstChar);
				for (int ii = i; ii < charactersLength; ii++) {
					char secondChar = characters[ii];
					Glyph second = data.getGlyph(secondChar);

					if (second == null) continue;

					int secondIndex = face.getCharIndex(secondChar);

					int kerning = face.getKerning(firstIndex, secondIndex, 0); // FT_KERNING_DEFAULT (scaled then rounded).
					if (kerning != 0) first.setKerning(secondChar, FreeType.toInt(kerning));

					kerning = face.getKerning(secondIndex, firstIndex, 0); // FT_KERNING_DEFAULT (scaled then rounded).
					if (kerning != 0) second.setKerning(firstChar, FreeType.toInt(kerning));
				}
			}
		}

		// Generate texture regions.
		if (ownsAtlas) {
			data.regions = new Seq<>(TextureRegion.class);
			packer.updateTextureRegions(data.regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);
		}

		// Set space glyph.
		Font.Glyph spaceGlyph = data.getGlyph(' ');
		if (spaceGlyph == null) {
			spaceGlyph = new Font.Glyph();
			spaceGlyph.xadvance = (int) data.spaceXadvance + parameter.spaceX;
			spaceGlyph.id = ' ';
			data.setGlyph(' ', spaceGlyph);
		}
		if (spaceGlyph.width == 0) spaceGlyph.width = (int) (spaceGlyph.xadvance + data.padRight);

		return data;
	}

	/**
	 * @return null if glyph was not found.
	 */
	public Font.Glyph createGlyph(char c, FreeTypeFontData2 data, FontParameter2 parameter, FreeType.Stroker stroker, float baseLine, PixmapPacker packer) {
		boolean missing = face.getCharIndex(c) == 0 && c != 0;
		if (missing) return null;

		if (!loadChar(c, getLoadingFlags(parameter))) return null;

		FreeType.GlyphSlot slot = face.getGlyph();
		FreeType.Glyph mainGlyph = slot.getGlyph();
		try {
			mainGlyph.toBitmap(parameter.mono ? FreeType.FT_RENDER_MODE_MONO : FreeType.FT_RENDER_MODE_NORMAL);
		} catch (ArcRuntimeException e) {
			mainGlyph.dispose();
			Log.infoTag("FreeTypeFontGenerator", "Couldn't render char: $c");
			return null;
		}
		FreeType.Bitmap mainBitmap = mainGlyph.getBitmap();
		Pixmap mainPixmap = mainBitmap.getPixmap(parameter.color, parameter.gamma);

		if (mainBitmap.getWidth() != 0 && mainBitmap.getRows() != 0) {
			int offsetX;
			int offsetY;
			if (parameter.borderWidth > 0) {
				// execute stroker; this generates a glyph "extended" along the outline
				int top = mainGlyph.getTop();
				int left = mainGlyph.getLeft();
				FreeType.Glyph borderGlyph = slot.getGlyph();
				borderGlyph.strokeBorder(stroker, false);
				borderGlyph.toBitmap(parameter.mono ? FreeType.FT_RENDER_MODE_MONO : FreeType.FT_RENDER_MODE_NORMAL);
				offsetX = left - borderGlyph.getLeft();
				offsetY = -(top - borderGlyph.getTop());

				// Render border (pixmap is bigger than main).
				FreeType.Bitmap borderBitmap = borderGlyph.getBitmap();
				Pixmap borderPixmap = borderBitmap.getPixmap(parameter.borderColor, parameter.borderGamma);

				// Draw main glyph on top of border.
				int i = 0;
				int n = parameter.renderCount;
				while (i < n) {
					borderPixmap.draw(mainPixmap, offsetX, offsetY, true);
					i++;
				}

				mainPixmap.dispose();
				mainGlyph.dispose();
				mainPixmap = borderPixmap;
				mainGlyph = borderGlyph;
			}

			if (parameter.borderWidth == 0f) {
				// No shadow and no border, draw glyph additional times.
				int i = 0;
				int n = parameter.renderCount - 1;
				while (i < n) {
					mainPixmap.draw(mainPixmap, 0, 0, true);
					i++;
				}
			}

			if (parameter.padTop > 0 || parameter.padLeft > 0 || parameter.padBottom > 0 || parameter.padRight > 0) {
				Pixmap padPixmap = new Pixmap(
						mainPixmap.width + parameter.padLeft + parameter.padRight,
						mainPixmap.height + parameter.padTop + parameter.padBottom
				);
				padPixmap.draw(mainPixmap, parameter.padLeft, parameter.padTop, true);
				mainPixmap.dispose();
				mainPixmap = padPixmap;
			}

			if (parameter.distanceFieldSpread > 0) {
				int spread = parameter.distanceFieldSpread;
				Pixmap spreadPixmap = new Pixmap(
						mainPixmap.width + spread * 2,
						mainPixmap.height + spread * 2
				);
				spreadPixmap.draw(mainPixmap, spread, spread, true);

				Pixmap distPixmap = generateDistanceField(
						parameter.distanceFieldDownscale,
						parameter.distanceFieldSpread,
						parameter.distanceFieldColor,
						spreadPixmap
				);
				mainPixmap.dispose();
				mainPixmap = distPixmap;
			}
		}

		FreeType.GlyphMetrics metrics = slot.getMetrics();
		Font.Glyph glyph = new Font.Glyph();
		glyph.id = c;
		glyph.width = mainPixmap.width; //- parameter.distanceFieldSpread*2
		glyph.height = mainPixmap.height; //- parameter.distanceFieldSpread*2
		glyph.xoffset = mainGlyph.getLeft(); //+ parameter.distanceFieldSpread
		if (parameter.flip) glyph.yoffset = -mainGlyph.getTop() + (int) baseLine;
		else glyph.yoffset = -(glyph.height - mainGlyph.getTop()) - (int) baseLine;
		glyph.xadvance = FreeType.toInt(metrics.getHoriAdvance()) + (int) parameter.borderWidth + parameter.spaceX;

		if (bitmapped) {
			mainPixmap.fill(Color.clearRgba);
			ByteBuffer buf = mainBitmap.getBuffer();
			int whiteIntBits = Color.white.abgr();
			int clearIntBits = Color.clear.abgr();
			for (int h = 0; h < glyph.height; h++) {
				int idx = h * mainBitmap.getPitch();
				for (int w = 0; h < (glyph.width + glyph.xoffset); w++) {
					int bit = ((int) buf.get(idx + (w / 8)) >>> (7 - (w % 8))) & 1;
					mainPixmap.set(w, h, (bit == 1 ? whiteIntBits : clearIntBits));
				}
			}
		}

		Rect rect = packer.pack(mainPixmap);
		glyph.page = packer.getPages().size - 1; // Glyph is always packed into the last page for now.
		glyph.srcX = (int) rect.x;
		glyph.srcY = (int) rect.y;

		// If a page was added, create a new texture region for the incrementally added glyph.
		if (parameter.incremental && data.regions != null && data.regions.size <= glyph.page)
			packer.updateTextureRegions(
					data.regions,
					parameter.minFilter,
					parameter.magFilter,
					parameter.genMipMaps
			);

		mainPixmap.dispose();
		mainGlyph.dispose();

		return glyph;
	}

	int squareDist(int x1, int y1, int x2, int y2) {
		int dx = x1 - x2;
		int dy = y1 - y2;
		return dx * dx + dy * dy;
	}

	public Pixmap generateDistanceField(int downscale, int spread, Color color, Pixmap inImage) {
		int inWidth = inImage.width;
		int inHeight = inImage.height;
		int outWidth = inWidth / downscale;
		int outHeight = inHeight / downscale;
		Pixmap outImage = new Pixmap(outWidth, outHeight);
		boolean[][] bitmap = new boolean[inHeight][inWidth];

		outImage.fill(Color.clear);

		for (int y = 0; y < inHeight; y++) {
			for (int x = 0; x < inWidth; x++) {
				bitmap[y][x] = isInside(inImage.get(x, y));
			}
		}

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				int centerX = x * downscale + downscale / 2;
				int centerY = y * downscale + downscale / 2;
				float signedDistance = findSignedDistance(spread, centerX, centerY, bitmap);
				outImage.set(x, y, distanceToRGB(spread, inImage.get(centerX, centerY), signedDistance));
			}
		}

		return outImage;
	}

	boolean isInside(int rgba) {
		return Color.ai(rgba) > 32;
	}

	int distanceToRGB(int spread, int color, float signedDistance) {
		float alpha = 0.5f + 0.5f * (signedDistance / spread);
		float a = Color.ai(color) / 255f;
		alpha = Mathf.clamp(alpha) * a;
		int alphaByte = (int) (alpha * 255.0f);
		return alphaByte | (color & 0xffffff00);
	}

	float findSignedDistance(int spread, int centerX, int centerY, boolean[][] bitmap) {
		int width = bitmap[0].length;
		int height = bitmap.length;
		boolean base = bitmap[centerY][centerX];
		int startX = Math.max(0, centerX - spread);
		int endX = Math.min(width - 1, centerX + spread);
		int startY = Math.max(0, centerY - spread);
		int endY = Math.min(height - 1, centerY + spread);
		int closestSquareDist = spread * spread;

		for (int y = startY; y <= endY; y++) {
			for (int x = startX; x <= endX; x++) {
				if (base != bitmap[y][x]) {
					int squareDist = squareDist(centerX, centerY, x, y);
					if (squareDist < closestSquareDist) {
						closestSquareDist = squareDist;
					}
				}
			}
		}

		float closestDist = (float) Math.sqrt(closestSquareDist);
		return (base ? 1 : -1) * Math.min(closestDist, spread);
	}

	/** Cleans up all resources of the generator. Call this if you no longer use the generator. */
	@Override
	public void dispose() {
		face.dispose();
		library.dispose();
	}

	/** Font smoothing algorithm. */
	public enum Hinting2 {
		/** Disable hinting. Generated glyphs will look blurry. */
		none,
		/** Light hinting with fuzzy edges, but close to the original shape */
		slight,
		/** Average hinting */
		medium,
		/** Strong hinting with crisp edges at the expense of shape fidelity */
		full,
		/** Light hinting with fuzzy edges, but close to the original shape. Uses the FreeType auto-hinter. */
		autoSlight,
		/** Average hinting. Uses the FreeType auto-hinter. */
		autoMedium,
		/** Strong hinting with crisp edges at the expense of shape fidelity. Uses the FreeType auto-hinter. */
		autoFull,
	}

	/**
	 * [FontData] used for fonts generated via the [UnkFontGenerator]. The texture storing the glyphs is
	 * held in memory, thus the [.getImagePaths] and [.getFontFile] methods will return null.
	 *
	 * @author mzechner
	 * @author Nathan Sweet
	 */
	public static class FreeTypeFontData2 extends FontData implements Disposable {
		/** Set to true to disable font caching. Only use if you know what you're doing. */
		public static boolean ignoreDirty;

		public Seq<TextureRegion> regions;

		// Fields for incremental glyph generation.
		public FontGenerator2 generator;
		public FontParameter2 parameter;
		public FreeType.Stroker stroker;
		public PixmapPacker packer;
		public Seq<Glyph> glyphs;

		boolean dirty;

		@Override
		public Glyph getGlyph(char ch) {
			Font.Glyph glyph = super.getGlyph(ch);
			if (glyph == null && generator != null) {
				generator.setPixelSizes(0, parameter.size);
				float baseline = ((flipped ? -ascent : ascent) + capHeight) / scaleY;
				glyph = generator.createGlyph(ch, this, parameter, stroker, baseline, packer);
				if (glyph == null) return missingGlyph;

				setGlyphRegion(glyph, regions.get(glyph.page));

				setGlyph(ch, glyph);
				glyphs.add(glyph);
				dirty = true;

				FreeType.Face face = generator.face;
				if (parameter.kerning) {
					int glyphIndex = face.getCharIndex(ch);
					int i = 0;
					int n = glyphs.size;
					while (i < n) {
						Font.Glyph other = glyphs.get(i);
						int otherIndex = face.getCharIndex(other.id);

						int kerning = face.getKerning(glyphIndex, otherIndex, 0);
						if (kerning != 0) glyph.setKerning(other.id, FreeType.toInt(kerning));

						kerning = face.getKerning(otherIndex, glyphIndex, 0);
						if (kerning != 0) other.setKerning(ch, FreeType.toInt(kerning));
						i++;
					}
				}
			}
			return glyph;
		}

		@Override
		public void getGlyphs(GlyphRun run, CharSequence str, int start, int end, Glyph lastGlyph) {
			if (packer != null)
				packer.setPackToTexture(true);// All glyphs added after this are packed directly to the texture.

			super.getGlyphs(run, str, start, end, lastGlyph);

			if (dirty && !ignoreDirty) {
				//queuing font updates fixes a crash on iOS.
				Core.app.post(() -> {
					if (dirty) {
						dirty = false;
						packer.updateTextureRegions(regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);
					}
				});
			}
		}

		@Override
		public void dispose() {
			if (stroker != null) stroker.dispose();
			if (packer != null) packer.dispose();
		}
	}

	/**
	 * Parameter container class that helps configure how [FreeTypeFontData] and [Font] instances are
	 * generated.
	 * <p>The packer field is for advanced usage, where it is necessary to pack multiple BitmapFonts (i.e. styles, sizes, families)
	 * into a single Texture atlas. If no packer is specified, the generator will use its own PixmapPacker to pack the glyphs into
	 * a power-of-two sized texture, and the resulting [FreeTypeFontData] will have a valid [TextureRegion] which
	 * can be used to construct a new [Font].
	 *
	 * @author siondream
	 * @author Nathan Sweet
	 */
	public static class FontParameter2 {
		/** The size in pixels */
		public int size = 16;

		/** If true, font smoothing is disabled. */
		public boolean mono = false;

		/** Strength of hinting */
		public Hinting2 hinting = Hinting2.autoMedium;

		/** Foreground color (required for non-black borders) */
		public Color color = Color.white;

		/** Glyph gamma. Values > 1 reduce antialiasing. */
		public float gamma = 1.8f;

		/** Number of times to render the glyph. Useful with a shadow or border, so it doesn't show through the glyph. */
		public int renderCount = 2;

		/** Border width in pixels, 0 to disable */
		public float borderWidth;

		/** Border color; only used if borderWidth > 0 */
		public Color borderColor = Color.black;

		/** true for straight (mitered), false for rounded borders */
		public boolean borderStraight;

		/** Values < 1 increase the border size. */
		public float borderGamma = 1.8f;

		public Color distanceFieldColor = Color.white;
		public int distanceFieldDownscale = 1;
		public int distanceFieldSpread = 1;

		/** Pixels to add to glyph spacing when text is rendered. Can be negative. */
		public int spaceX, spaceY;

		/** Pixels to add to the glyph in the texture. Can be negative. */
		public int padTop, padLeft, padBottom, padRight;

		/** The characters the font should contain. If '\0' is not included then {@link FontData#missingGlyph} is not set. */
		public String characters = DEFAULT_CHARS;

		/** Whether the font should include kerning */
		public boolean kerning = true;

		/**
		 * The optional PixmapPacker to use for packing multiple fonts into a single texture.
		 *
		 * @see FontParameter2
		 */
		public PixmapPacker packer;

		/** Whether to flip the font vertically */
		public boolean flip;

		/** Whether to generate mip maps for the resulting texture */
		public boolean genMipMaps;

		/** Minification filter */
		public TextureFilter minFilter = TextureFilter.nearest;

		/** Magnification filter */
		public TextureFilter magFilter = TextureFilter.nearest;

		/**
		 * When true, glyphs are rendered on the fly to the font's glyph page textures as they are needed. The
		 * FreeTypeFontGenerator must not be disposed until the font is no longer needed. The FreeTypeBitmapFontData must be
		 * disposed (separately from the generator) when the font is no longer needed. The FreeTypeFontParameter should not be
		 * modified after creating a font. If a PixmapPacker is not specified, the font glyph page textures will use
		 * [UnkFontGenerator.getMaxTextureSize].
		 */
		public boolean incremental;
	}

	public static class GlyphAndBitmap {
		public Font.Glyph glyph;
		public FreeType.Bitmap bitmap;
	}
}
