package ucenfotec.ac.cr.flydevs.domain.model

data class PickedImage(
    val bytes: ByteArray,
    val mimeType: String,
    val extension: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedImage) return false
        return bytes.contentEquals(other.bytes) &&
            mimeType == other.mimeType &&
            extension == other.extension
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + extension.hashCode()
        return result
    }
}
