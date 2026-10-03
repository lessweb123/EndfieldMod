package endfield.graphics

import arc.graphics.Pixmap
import arc.graphics.PixmapIO
import arc.util.serialization.Base64Coder

fun pixmapToBase64(pixmap: Pixmap): String = String(Base64Coder.encode(PixmapIO.writePngBytes(pixmap)))