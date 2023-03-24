import com.soywiz.kds.BitSet

class Bitmap private constructor(
    val data: BitSet
) {
    /**
     * Build a bitmap (primary, secondary) or an element with binary type, for example DE52, DE64...
     */
    class Builder(private val size: Int = 64) {
        private val bitArray = BitSet(size)

        fun onBit(index: Int): Builder {
            require(index in 0 until size) { "Index out of bounds 0-${size - 1}" }
            bitArray.set(index)
            return this
        }

        fun offBit(index: Int): Builder {
            require(index in 0 until size) { "Index out of bounds 0-${size - 1}" }
            bitArray.unset(index)
            return this
        }

        fun build(): Bitmap = Bitmap(data = bitArray)
    }

    companion object {
        fun from(source: String): Bitmap {
            require(source.length == 16) { "Bitmap has 64bit, so it is 16 ASCII characters" }
            return Bitmap(
                data = source.hexToBinary().binaryToBitArray()
            )
        }
    }

    override fun toString(): String {
        return this.data.bitArrayToBinary().binaryToHex()
    }
}