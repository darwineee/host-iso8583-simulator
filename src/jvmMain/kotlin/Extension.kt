import com.soywiz.kds.BitSet
import kotlin.math.roundToInt

/**
 * Convert from hex string to binary string, by effective way.
 * Alternative: it.digitToInt(16).toString(2).padStart(4, '0')
 */
fun String.hexToBinary(): String {
    val translationTable = mapOf(
        '0' to "0000",
        '1' to "0001",
        '2' to "0010",
        '3' to "0011",
        '4' to "0100",
        '5' to "0101",
        '6' to "0110",
        '7' to "0111",
        '8' to "1000",
        '9' to "1001",
        'A' to "1010",
        'B' to "1011",
        'C' to "1100",
        'D' to "1101",
        'E' to "1110",
        'F' to "1111",
    )
    return this.map { translationTable[it] }.joinToString(separator = "")
}

/**
 * Convert from binary string to hex string
 */
fun String.binaryToHex(): String {
    val translationTable = mapOf(
        "0000" to '0',
        "0001" to '1',
        "0010" to '2',
        "0011" to '3',
        "0100" to '4',
        "0101" to '5',
        "0110" to '6',
        "0111" to '7',
        "1000" to '8',
        "1001" to '9',
        "1010" to 'A',
        "1011" to 'B',
        "1100" to 'C',
        "1101" to 'D',
        "1110" to 'E',
        "1111" to 'F',
    )
    val fullLength = (length / 4f).roundToInt() * 4
    val source = padStart( fullLength, '0')
    return source
        .chunked(4)
        .map { translationTable[it] }
        .joinToString(separator = "")
}

/**
 * Convert from binary string to an array of bit.
 */
fun String.binaryToBitArray(): BitSet {
    val bitArray = BitSet(64)
    forEachIndexed { index, c ->
        bitArray[index] = c == '1'
    }
    return bitArray
}

/**
 * Convert from array of bit to binary string.
 */
fun BitSet.bitArrayToBinary(): String {
    return joinToString(separator = "") { if (it) "1" else "0" }
}