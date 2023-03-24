/**
 * Our message structure just have MTI + Primary bitmap + Date elements
 * Support up to 2 bitmaps
 */
object ISO8583 {

    data class Data(
        val mti: MTI,
        val primaryBitmap: Bitmap,
        val dataElements: List<DataElement>
    )

    fun decode(message: String): Data {
        var source = message

        val mti = MTI.from(source.take(4))
        source = source.drop(4)

        val primaryBitmap = Bitmap.from(source.take(16))
        source = source.drop(16)

        val dataElements = mutableListOf<DataElement>()
        primaryBitmap.data.forEachIndexed { index, isBitOn ->
            if (isBitOn) {
                val element = DataElement.getElementByIndex(index).also {
                    source = it.cutAndDecode(source)
                }
                dataElements.add(element)
            }
        }

        //process secondary bitmap
        dataElements[0].data.forEachIndexed { index, c ->
            if (c == '1') {
                val element = DataElement.getElementByIndex(64 + index).also {
                    source = it.cutAndDecode(source)
                }
                dataElements.add(element)
            }
        }

        return Data(
            mti = mti,
            primaryBitmap = primaryBitmap,
            dataElements = dataElements
        )
    }

    fun encode(data: Data): String {
        val mti = data.mti.toString()
        val primaryBitmap = data.primaryBitmap.toString()
        val dataElements = data.dataElements
            .sortedBy { it.indexCode }
            .joinToString("") { it.encode() }

        return mti + primaryBitmap + dataElements
    }
}