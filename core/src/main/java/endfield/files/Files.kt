package endfield.files

import arc.files.Fi
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest

fun getMD5(file: Fi): String {
	val buffer = ByteArray(8192)
	val md = MessageDigest.getInstance("MD5")
	val input = FileInputStream(file.file())
	var data: Int
	while ((input.read(buffer).also { data = it }) != -1) {
		md.update(buffer, 0, data)
	}
	input.close()
	return BigInteger(1, md.digest()).toString(16)
}