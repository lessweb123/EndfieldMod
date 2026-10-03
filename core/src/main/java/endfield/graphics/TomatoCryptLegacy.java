package endfield.graphics;

import arc.graphics.Pixmap;
import arc.struct.IntSeq;

import java.nio.IntBuffer;

public final class TomatoCryptLegacy {
	static IntSeq tmp = new IntSeq();

	private TomatoCryptLegacy() {}

	public static Pixmap encrypt(Pixmap input) {
		int w = input.getWidth(), h = input.getHeight();
		int total = w * h;
		Pixmap output = new Pixmap(w, h);

		int[] curve = buildCurveIndices(w, h);
		int offset = (int) Math.round((Math.sqrt(5) - 1) / 2 * total);

		IntBuffer src = input.getPixels().asIntBuffer();
		IntBuffer dst = output.getPixels().asIntBuffer();

		for (int i = 0; i < total; i++) {
			int oldP = curve[i];
			int newP = curve[(i + offset) % total];
			dst.put(newP, src.get(oldP));
		}
		return output;
	}

	public static Pixmap decrypt(Pixmap input) {
		int w = input.getWidth(), h = input.getHeight();
		int total = w * h;
		Pixmap output = new Pixmap(w, h);

		int[] curve = buildCurveIndices(w, h);
		int offset = (int) Math.round((Math.sqrt(5) - 1) / 2 * total);

		IntBuffer src = input.getPixels().asIntBuffer();
		IntBuffer dst = output.getPixels().asIntBuffer();

		for (int i = 0; i < total; i++) {
			int oldP = curve[i];
			int newP = curve[(i + offset) % total];
			dst.put(oldP, src.get(newP));
		}
		return output;
	}

	static int[] buildCurveIndices(int width, int height) {
		//IntSeq coords = new IntSeq(width * height * 2);
		IntSeq coords = tmp;
		coords.clear();
		if (width >= height) {
			generate2d(0, 0, width, 0, 0, height, coords);
		} else {
			generate2d(0, 0, 0, height, width, 0, coords);
		}
		int n = width * height;
		int[] idx = new int[n];
		for (int i = 0; i < n; i++) {
			int x = coords.get(i * 2);
			int y = coords.get(i * 2 + 1);
			idx[i] = x + y * width;
		}
		return idx;
	}

	static void generate2d(int x, int y, int ax, int ay, int bx, int by, IntSeq out) {
		int width = Math.abs(ax + ay);
		int height = Math.abs(bx + by);

		int dax = Integer.signum(ax), day = Integer.signum(ay);
		int dbx = Integer.signum(bx), dby = Integer.signum(by);

		if (height == 1) {
			for (int i = 0; i < width; i++) {
				out.add(x); out.add(y);
				x += dax; y += day;
			}
			return;
		}
		if (width == 1) {
			for (int i = 0; i < height; i++) {
				out.add(x); out.add(y);
				x += dbx; y += dby;
			}
			return;
		}

		int ax2 = Math.floorDiv(ax, 2), ay2 = Math.floorDiv(ay, 2);
		int bx2 = Math.floorDiv(bx, 2), by2 = Math.floorDiv(by, 2);

		int w2 = Math.abs(ax2 + ay2);
		int h2 = Math.abs(bx2 + by2);

		if (2 * width > 3 * height) {
			if ((w2 & 1) != 0 && width > 2) {
				ax2 += dax; ay2 += day;
			}
			generate2d(x, y, ax2, ay2, bx, by, out);
			generate2d(x + ax2, y + ay2, ax - ax2, ay - ay2, bx, by, out);
		} else {
			if ((h2 & 1) != 0 && height > 2) {
				bx2 += dbx; by2 += dby;
			}
			generate2d(x, y, bx2, by2, ax2, ay2, out);
			generate2d(x + bx2, y + by2, ax, ay, bx - bx2, by - by2, out);
			generate2d(
					x + (ax - dax) + (bx2 - dbx),
					y + (ay - day) + (by2 - dby),
					-bx2, -by2, -(ax - ax2), -(ay - ay2), out
			);
		}
	}
}
