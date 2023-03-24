sealed class DESpec {
    abstract fun validate(data: String): Boolean

    object A : DESpec() {
        override fun validate(data: String): Boolean {
            val regex = Regex("[a-zA-Z ]+")
            return regex.matches(data)
        }
    }

    object N : DESpec() {
        override fun validate(data: String): Boolean {
            return data.all { it.isDigit() }
        }
    }

    object S : DESpec() {
        override fun validate(data: String): Boolean {
            val regex = Regex("[^a-zA-Z\\d ]+")
            return regex.matches(data)
        }
    }

    object AN : DESpec() {
        override fun validate(data: String): Boolean {
            val regex = Regex("[a-zA-Z\\d ]+")
            return regex.matches(data)
        }
    }

    object AS : DESpec() {
        override fun validate(data: String): Boolean {
            val regex = Regex("\\D")
            return regex.matches(data)
        }
    }

    object NS : DESpec() {
        override fun validate(data: String): Boolean {
            val regex = Regex("[^a-zA-Z ]+")
            return regex.matches(data)
        }
    }

    object ANS : DESpec() {
        override fun validate(data: String): Boolean {
            return data.all { it.isLetterOrDigit() }
        }
    }

    object B : DESpec() {
        override fun validate(data: String): Boolean {
            return data.all { it.isDigit() && (it == '0' || it == '1') }
        }
    }

    object Z : DESpec() {
        override fun validate(data: String): Boolean {
            return data.all { it.isLetterOrDigit() }
        }
    }
}

/**
 * Only support primary bitmap and secondary bitmap, that mean the data elements up to 128 elements.
 * Binary and hexadecimal is encoded by ASCII, not EBCDIC.
 */
sealed class DataElement {
    abstract val spec: DESpec
    abstract val length: UShort
    abstract val isFixed: Boolean
    abstract val indexCode: UByte
    abstract val description: String

    /**
     * Save data after decode, or hold raw data to used to encode later
     */
    abstract var data: String

    /**
     * Cut the data element from raw message, save the decoded data into [data], then return the cut off string
     */
    fun cutAndDecode(source: String): String {
        var cutOffSource: String
        if (isFixed) {
            if (this.spec is DESpec.B) {
                data = source.take(length.toInt() / 4).hexToBinary()
                cutOffSource = source.drop(length.toInt() / 4)
            } else {
                data = source.take(length.toInt())
                cutOffSource = source.drop(length.toInt())
            }
        } else {
            val lengthIndicator = when (length.toInt()) {
                in 0..9 -> 1
                in 10..99 -> 2
                else -> 3
            }
            val realLength = source.take(lengthIndicator).toIntOrNull() ?: throw Exception("This $this can not parse")
            cutOffSource = source.drop(lengthIndicator)
            data = cutOffSource.take(realLength)
            cutOffSource = cutOffSource.drop(realLength)
        }
        if (spec.validate(data).not()) data = ""
        return cutOffSource
    }

    fun encode(): String {
        require(spec.validate(data)) { "DE${indexCode}: data ($data) not valid" }
        return if (isFixed) {
            require(data.length == length.toInt()) { "This DE${indexCode} is fixed length $length and the $data is not fit" }
            if (this.spec is DESpec.B) {
                data.binaryToHex()
            } else {
                data
            }
        } else {
            require(data.length <= length.toInt()) { "This DE${indexCode} is flexible length $length and the $data is not fit" }
            val lengthIndicator = when (length.toInt()) {
                in 0..9 -> 1
                in 10..99 -> 2
                else -> 3
            }
            val realLength = data.length.toString().padStart(lengthIndicator, '0')
            realLength + data
        }
    }

//--------------------------- Region Primary bitmap-----------------------------------------

    //Secondary bitmap
    data class DE01(
        override val indexCode: UByte = 1u,
        override val length: UShort = 64U,
        override val spec: DESpec = DESpec.B,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Secondary bitmap",
    ) : DataElement()

    //Primary account number (PAN)
    data class DE02(
        override val indexCode: UByte = 2u,
        override val length: UShort = 19U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Primary account number (PAN)",
    ) : DataElement()

    //Processing code
    data class DE03(
        override val indexCode: UByte = 3u,
        override val length: UShort = 6U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Processing code",
    ) : DataElement()

    //Amount, transaction
    data class DE04(
        override val indexCode: UByte = 4u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount of transaction",
    ) : DataElement()

    //Amount, settlement
    data class DE05(
        override val indexCode: UByte = 5u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount of settlement",
    ) : DataElement()

    //Amount, cardholder billing
    data class DE06(
        override val indexCode: UByte = 6u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount, cardholder billing",
    ) : DataElement()

    //Transmission date & time
    data class DE07(
        override val indexCode: UByte = 7u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Transmission date & time",
    ) : DataElement()

    //Amount, cardholder billing fee
    data class DE08(
        override val indexCode: UByte = 8u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount, cardholder billing fee",
    ) : DataElement()

    //Conversion rate, settlement
    data class DE09(
        override val indexCode: UByte = 9u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Conversion rate, settlement",
    ) : DataElement()

    //Conversion rate, cardholder billing
    data class DE10(
        override val indexCode: UByte = 10u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Conversion rate, cardholder billing",
    ) : DataElement()

    //System trace audit number (STAN)
    data class DE11(
        override val indexCode: UByte = 11u,
        override val length: UShort = 6U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "System trace audit number (STAN)",
    ) : DataElement()

    //Local transaction time (hhmmss)
    data class DE12(
        override val indexCode: UByte = 12u,
        override val length: UShort = 6U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Local transaction time (hhmmss)",
    ) : DataElement()

    //Local transaction date (MMDD)
    data class DE13(
        override val indexCode: UByte = 13u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Local transaction date (MMDD)",
    ) : DataElement()

    //Expiration date (YYMM)
    data class DE14(
        override val indexCode: UByte = 14u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Expiration date (YYMM)",
    ) : DataElement()

    //Settlement date
    data class DE15(
        override val indexCode: UByte = 15u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Settlement date",
    ) : DataElement()

    //Currency conversion date
    data class DE16(
        override val indexCode: UByte = 16u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Currency conversion date",
    ) : DataElement()

    //Capture date
    data class DE17(
        override val indexCode: UByte = 17u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Capture date",
    ) : DataElement()

    //Merchant type, or merchant category code
    data class DE18(
        override val indexCode: UByte = 18u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Merchant type, or merchant category code"
    ) : DataElement()

    //Acquiring institution (country code)
    data class DE19(
        override val indexCode: UByte = 19u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Acquiring institution (country code)",
    ) : DataElement()

    //PAN extended (country code)
    data class DE20(
        override val indexCode: UByte = 20u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "PAN extended (country code)",
    ) : DataElement()

    //Forwarding institution (country code)
    data class DE21(
        override val indexCode: UByte = 21u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Forwarding institution (country code)"
    ) : DataElement()

    //Point of service entry mode
    data class DE22(
        override val indexCode: UByte = 22u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Point of service entry mode"
    ) : DataElement()

    //Application PAN number
    data class DE23(
        override val indexCode: UByte = 23u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Application PAN sequence number"
    ) : DataElement()

    //Network International identifier (NII)
    data class DE24(
        override val indexCode: UByte = 24u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Network international identifier (NII)"
    ) : DataElement()

    //Point of service condition code
    data class DE25(
        override val indexCode: UByte = 25u,
        override val length: UShort = 2U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Point of service condition code"
    ) : DataElement()

    //Point of service capture code
    data class DE26(
        override val indexCode: UByte = 26u,
        override val length: UShort = 2U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Point of service capture code"
    ) : DataElement()

    //Authorizing identification response length
    data class DE27(
        override val indexCode: UByte = 27u,
        override val length: UShort = 1U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Authorizing identification response length"
    ) : DataElement()

    //Amount, transaction fee
    data class DE28(
        override val indexCode: UByte = 28u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount, transaction fee"
    ) : DataElement()

    //Amount, settlement fee
    data class DE29(
        override val indexCode: UByte = 29u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount, settlement fee"
    ) : DataElement()

    //Amount, transaction processing fee
    data class DE30(
        override val indexCode: UByte = 30u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount, transaction processing fee"
    ) : DataElement()

    //Amount, settlement processing fee
    data class DE31(
        override val indexCode: UByte = 31u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Amount, settlement processing fee"
    ) : DataElement()

    //Acquiring institution identification code
    data class DE32(
        override val indexCode: UByte = 32u,
        override val length: UShort = 11U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Acquiring institution identification code"
    ) : DataElement()

    //Forwarding institution identification code
    data class DE33(
        override val indexCode: UByte = 33u,
        override val length: UShort = 11U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Forwarding institution identification code"
    ) : DataElement()

    //Primary account number, extended
    data class DE34(
        override val indexCode: UByte = 34u,
        override val length: UShort = 28U,
        override val spec: DESpec = DESpec.NS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Primary account number, extended"
    ) : DataElement()

    //Track 2 data
    data class DE35(
        override val indexCode: UByte = 35u,
        override val length: UShort = 37U,
        override val spec: DESpec = DESpec.Z,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Track 2 data"
    ) : DataElement()

    //Track 3 data
    data class DE36(
        override val indexCode: UByte = 36u,
        override val length: UShort = 104U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Track 3 data"
    ) : DataElement()

    //Retrieval reference number
    data class DE37(
        override val indexCode: UByte = 37u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Retrieval reference number"
    ) : DataElement()

    //Authorization identification response
    data class DE38(
        override val indexCode: UByte = 38u,
        override val length: UShort = 6U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Authorization identification response"
    ) : DataElement()

    //Response code
    data class DE39(
        override val indexCode: UByte = 39u,
        override val length: UShort = 2U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Response code"
    ) : DataElement()

    //Service restriction code
    data class DE40(
        override val indexCode: UByte = 40u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Service restriction code"
    ) : DataElement()

    //Card acceptor terminal identification
    data class DE41(
        override val indexCode: UByte = 41u,
        override val length: UShort = 8U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Card acceptor terminal identification"
    ) : DataElement()

    //Card acceptor identification code
    data class DE42(
        override val indexCode: UByte = 42u,
        override val length: UShort = 15U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Card acceptor identification code"
    ) : DataElement()

    //Card acceptor name/location (1–23 street address, –36 city, –38 state, 39–40 country)
    data class DE43(
        override val indexCode: UByte = 43u,
        override val length: UShort = 40U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Card acceptor name/location (1–23 street address, –36 city, –38 state, 39–40 country)"
    ) : DataElement()

    //Additional response data
    data class DE44(
        override val indexCode: UByte = 44u,
        override val length: UShort = 25U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Additional response data"
    ) : DataElement()

    //Track 1 Data
    data class DE45(
        override val indexCode: UByte = 45u,
        override val length: UShort = 76U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Track 1 Data"
    ) : DataElement()

    //Additional data (ISO)
    data class DE46(
        override val indexCode: UByte = 46u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Additional data (ISO)"
    ) : DataElement()

    //Additional data (national)
    data class DE47(
        override val indexCode: UByte = 47u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Additional data (national)"
    ) : DataElement()

    //Additional data (private)
    data class DE48(
        override val indexCode: UByte = 48u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Additional data (private)"
    ) : DataElement()

    //Currency code, transaction
    data class DE49(
        override val indexCode: UByte = 49u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Currency code, transaction"
    ) : DataElement()

    //Currency code, settlement
    data class DE50(
        override val indexCode: UByte = 50u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Currency code, settlement"
    ) : DataElement()

    //Currency code, cardholder billing
    data class DE51(
        override val indexCode: UByte = 51u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Currency code, cardholder billing"
    ) : DataElement()

    //Personal Identification Number data (PIN)
    data class DE52(
        override val indexCode: UByte = 52u,
        override val length: UShort = 64U,
        override val spec: DESpec = DESpec.B,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Personal Identification Number data (PIN)"
    ) : DataElement()

    //Security related control information
    data class DE53(
        override val indexCode: UByte = 53u,
        override val length: UShort = 16U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Security related control information"
    ) : DataElement()

    //Additional amounts
    data class DE54(
        override val indexCode: UByte = 54u,
        override val length: UShort = 120U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Additional amounts"
    ) : DataElement()

    //ICC data – EMV having multiple tags
    data class DE55(
        override val indexCode: UByte = 55u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "ICC data – EMV having multiple tags"
    ) : DataElement()

    //Reserved (ISO)
    data class DE56(
        override val indexCode: UByte = 56u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved (national)
    data class DE57(
        override val indexCode: UByte = 57u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved (national)
    data class DE58(
        override val indexCode: UByte = 58u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved (national)
    data class DE59(
        override val indexCode: UByte = 59u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved (national) (e.g. settlement request: batch number, advice transactions: original transaction amount, batch upload: original MTI plus original RRN plus original STAN, etc.)
    data class DE60(
        override val indexCode: UByte = 60u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved (private) (e.g. CVV2/service code   transactions)
    data class DE61(
        override val indexCode: UByte = 61u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()

    //Reserved (private) (e.g. transactions: invoice number, key exchange transactions: TPK key, etc.)
    data class DE62(
        override val indexCode: UByte = 62u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()

    //Reserved (private)
    data class DE63(
        override val indexCode: UByte = 63u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()

    //Message authentication code (MAC)
    data class DE64(
        override val indexCode: UByte = 64u,
        override val length: UShort = 64U,
        override val spec: DESpec = DESpec.B,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Message authentication code (MAC)"
    ) : DataElement()

//---------------------------Secondary bitmap-----------------------------------------

    //Ternary bitmap indicator, never show up in ISO8583:1987
    //You should not use this field
    data class DE65(
        override val indexCode: UByte = 65u,
        override val length: UShort = 1U,
        override val spec: DESpec = DESpec.B,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Ternary bitmap indicator"
    ) : DataElement()

    //Settlement code
    data class DE66(
        override val indexCode: UByte = 66u,
        override val length: UShort = 1U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Settlement code"
    ) : DataElement()

    //Extended payment code
    data class DE67(
        override val indexCode: UByte = 67u,
        override val length: UShort = 2U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Extended payment code"
    ) : DataElement()

    //Receiving institution country code
    data class DE68(
        override val indexCode: UByte = 68u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Receiving institution country code"
    ) : DataElement()

    //Settlement institution county code
    data class DE69(
        override val indexCode: UByte = 69u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Settlement institution county code"
    ) : DataElement()

    //Network management information code
    data class DE70(
        override val indexCode: UByte = 70u,
        override val length: UShort = 3U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Network management information code"
    ) : DataElement()

    //Message number
    data class DE71(
        override val indexCode: UByte = 71u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Message number"
    ) : DataElement()

    //Last message's number
    data class DE72(
        override val indexCode: UByte = 72u,
        override val length: UShort = 4U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Last message's number"
    ) : DataElement()

    //Action date (YYMMDD)
    data class DE73(
        override val indexCode: UByte = 73u,
        override val length: UShort = 6U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Action date (YYMMDD)"
    ) : DataElement()

    //Number of credits
    data class DE74(
        override val indexCode: UByte = 74u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Number of credits"
    ) : DataElement()

    //Credits, reversal number
    data class DE75(
        override val indexCode: UByte = 75u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Credits, reversal number"
    ) : DataElement()

    //Number of debits
    data class DE76(
        override val indexCode: UByte = 76u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Number of debits"
    ) : DataElement()

    //Debits, reversal number
    data class DE77(
        override val indexCode: UByte = 77u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Debits, reversal number"
    ) : DataElement()

    //Transfer number
    data class DE78(
        override val indexCode: UByte = 78u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Transfer number"
    ) : DataElement()

    //Transfer, reversal number
    data class DE79(
        override val indexCode: UByte = 79u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Transfer, reversal number"
    ) : DataElement()

    //Number of inquiries
    data class DE80(
        override val indexCode: UByte = 80u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Number of inquiries"
    ) : DataElement()

    //Number of authorizations
    data class DE81(
        override val indexCode: UByte = 81u,
        override val length: UShort = 10U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Number of authorizations"
    ) : DataElement()

    //Credits, processing fee amount
    data class DE82(
        override val indexCode: UByte = 82u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Credits, processing fee amount"
    ) : DataElement()

    //Credits, transaction fee amount
    data class DE83(
        override val indexCode: UByte = 83u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Credits, transaction fee amount"
    ) : DataElement()

    //Debits, processing fee amount
    data class DE84(
        override val indexCode: UByte = 84u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Debits, processing fee amount"
    ) : DataElement()

    //Debits, transaction fee amount
    data class DE85(
        override val indexCode: UByte = 85u,
        override val length: UShort = 12U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Debits, transaction fee amount"
    ) : DataElement()

    //Total amount of credits
    data class DE86(
        override val indexCode: UByte = 86u,
        override val length: UShort = 16U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Total amount of credits"
    ) : DataElement()

    //Credits, reversal amount
    data class DE87(
        override val indexCode: UByte = 87u,
        override val length: UShort = 16U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Credits, reversal amount"
    ) : DataElement()

    //Total amount of debits
    data class DE88(
        override val indexCode: UByte = 88u,
        override val length: UShort = 15U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Total amount of debits"
    ) : DataElement()

    //Debits, reversal amount
    data class DE89(
        override val indexCode: UByte = 89u,
        override val length: UShort = 16U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Debits, reversal amount"
    ) : DataElement()

    //Original data elements
    data class DE90(
        override val indexCode: UByte = 90u,
        override val length: UShort = 42U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Original data elements"
    ) : DataElement()

    //File update code
    data class DE91(
        override val indexCode: UByte = 91u,
        override val length: UShort = 1U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "File update code"
    ) : DataElement()

    //File security code
    data class DE92(
        override val indexCode: UByte = 92u,
        override val length: UShort = 2U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "File security code"
    ) : DataElement()

    //Response indicator
    data class DE93(
        override val indexCode: UByte = 93u,
        override val length: UShort = 5U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Response indicator"
    ) : DataElement()

    //Service indicator
    data class DE94(
        override val indexCode: UByte = 94u,
        override val length: UShort = 7U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Service indicator"
    ) : DataElement()

    //Replacement amounts
    data class DE95(
        override val indexCode: UByte = 95u,
        override val length: UShort = 42U,
        override val spec: DESpec = DESpec.AN,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Replacement amounts"
    ) : DataElement()

    //Message security code
    data class DE96(
        override val indexCode: UByte = 96u,
        override val length: UShort = 64U,
        override val spec: DESpec = DESpec.B,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Message security code"
    ) : DataElement()

    //Net settlement amount
    data class DE97(
        override val indexCode: UByte = 97u,
        override val length: UShort = 16U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Net settlement amount"
    ) : DataElement()

    //Payee
    data class DE98(
        override val indexCode: UByte = 98u,
        override val length: UShort = 25U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Payee"
    ) : DataElement()

    //Settlement institution identification code
    data class DE99(
        override val indexCode: UByte = 99u,
        override val length: UShort = 11U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Settlement institution identification code"
    ) : DataElement()

    //Receiving institution identification code
    data class DE100(
        override val indexCode: UByte = 100u,
        override val length: UShort = 11U,
        override val spec: DESpec = DESpec.N,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Receiving institution identification code"
    ) : DataElement()

    //File name
    data class DE101(
        override val indexCode: UByte = 101u,
        override val length: UShort = 17U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "File name"
    ) : DataElement()

    //Account identification 1
    data class DE102(
        override val indexCode: UByte = 102u,
        override val length: UShort = 28U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Account identification 1"
    ) : DataElement()

    //Account identification 2
    data class DE103(
        override val indexCode: UByte = 103u,
        override val length: UShort = 28U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Account identification 2"
    ) : DataElement()

    //Transaction description
    data class DE104(
        override val indexCode: UByte = 104u,
        override val length: UShort = 100U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Transaction description"
    ) : DataElement()

    //Reserved for ISO use
    data class DE105(
        override val indexCode: UByte = 105u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved for ISO use
    data class DE106(
        override val indexCode: UByte = 106u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved for ISO use
    data class DE107(
        override val indexCode: UByte = 107u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved for ISO use
    data class DE108(
        override val indexCode: UByte = 108u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved for ISO use
    data class DE109(
        override val indexCode: UByte = 109u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved for ISO use
    data class DE110(
        override val indexCode: UByte = 110u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved for ISO use
    data class DE111(
        override val indexCode: UByte = 111u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (ISO)"
    ) : DataElement()

    //Reserved for national use
    data class DE112(
        override val indexCode: UByte = 112u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for national use
    data class DE113(
        override val indexCode: UByte = 113u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for national use
    data class DE114(
        override val indexCode: UByte = 114u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for national use
    data class DE115(
        override val indexCode: UByte = 115u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for national use
    data class DE116(
        override val indexCode: UByte = 116u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for national use
    data class DE117(
        override val indexCode: UByte = 117u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for national use
    data class DE118(
        override val indexCode: UByte = 118u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for national use
    data class DE119(
        override val indexCode: UByte = 119u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (national)"
    ) : DataElement()

    //Reserved for private use
    data class DE120(
        override val indexCode: UByte = 120u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()


    data class DE121(
        override val indexCode: UByte = 121u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()


    data class DE122(
        override val indexCode: UByte = 122u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()


    data class DE123(
        override val indexCode: UByte = 123u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()


    data class DE124(
        override val indexCode: UByte = 124u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()

    data class DE125(
        override val indexCode: UByte = 125u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()

    //Issuer trace id
    data class DE126(
        override val indexCode: UByte = 126u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()

    //Reserved for private use
    data class DE127(
        override val indexCode: UByte = 127u,
        override val length: UShort = 999U,
        override val spec: DESpec = DESpec.ANS,
        override val isFixed: Boolean = false,
        override var data: String = "",
        override val description: String = "Reserved (private)"
    ) : DataElement()

    //Message authentication code
    data class DE128(
        override val indexCode: UByte = 128u,
        override val length: UShort = 64U,
        override val spec: DESpec = DESpec.B,
        override val isFixed: Boolean = true,
        override var data: String = "",
        override val description: String = "Message authentication code"
    ) : DataElement()

    companion object {
        fun getElementByIndex(index: Int) = when (index) {
            0 -> DE01()
            1 -> DE02()
            2 -> DE03()
            3 -> DE04()
            4 -> DE05()
            5 -> DE06()
            6 -> DE07()
            7 -> DE08()
            8 -> DE09()
            9 -> DE10()
            10 -> DE11()
            11 -> DE12()
            12 -> DE13()
            13 -> DE14()
            14 -> DE15()
            15 -> DE16()
            16 -> DE17()
            17 -> DE18()
            18 -> DE19()
            19 -> DE20()
            20 -> DE21()
            21 -> DE22()
            22 -> DE23()
            23 -> DE24()
            24 -> DE25()
            25 -> DE26()
            26 -> DE27()
            27 -> DE28()
            28 -> DE29()
            29 -> DE30()
            30 -> DE31()
            31 -> DE32()
            32 -> DE33()
            33 -> DE34()
            34 -> DE35()
            35 -> DE36()
            36 -> DE37()
            37 -> DE38()
            38 -> DE39()
            39 -> DE40()
            40 -> DE41()
            41 -> DE42()
            42 -> DE43()
            43 -> DE44()
            44 -> DE45()
            45 -> DE46()
            46 -> DE47()
            47 -> DE48()
            48 -> DE49()
            49 -> DE50()
            50 -> DE51()
            51 -> DE52()
            52 -> DE53()
            53 -> DE54()
            54 -> DE55()
            55 -> DE56()
            56 -> DE57()
            57 -> DE58()
            58 -> DE59()
            59 -> DE60()
            60 -> DE61()
            61 -> DE62()
            62 -> DE63()
            63 -> DE64()
            64 -> DE65()
            65 -> DE66()
            66 -> DE67()
            67 -> DE68()
            68 -> DE69()
            69 -> DE70()
            70 -> DE71()
            71 -> DE72()
            72 -> DE73()
            73 -> DE74()
            74 -> DE75()
            75 -> DE76()
            76 -> DE77()
            77 -> DE78()
            78 -> DE79()
            79 -> DE80()
            80 -> DE81()
            81 -> DE82()
            82 -> DE83()
            83 -> DE84()
            84 -> DE85()
            85 -> DE86()
            86 -> DE87()
            87 -> DE88()
            88 -> DE89()
            89 -> DE90()
            90 -> DE91()
            91 -> DE92()
            92 -> DE93()
            93 -> DE94()
            94 -> DE95()
            95 -> DE96()
            96 -> DE97()
            97 -> DE98()
            98 -> DE99()
            99 -> DE100()
            100 -> DE101()
            101 -> DE102()
            102 -> DE103()
            103 -> DE104()
            104 -> DE105()
            105 -> DE106()
            106 -> DE107()
            107 -> DE108()
            108 -> DE109()
            109 -> DE110()
            110 -> DE111()
            111 -> DE112()
            112 -> DE113()
            113 -> DE114()
            114 -> DE115()
            115 -> DE116()
            116 -> DE117()
            117 -> DE118()
            118 -> DE119()
            119 -> DE120()
            120 -> DE121()
            121 -> DE122()
            122 -> DE123()
            123 -> DE124()
            124 -> DE125()
            125 -> DE126()
            126 -> DE127()
            127 -> DE128()
            else -> throw IllegalArgumentException("No element supported from this bitmap")
        }
    }
}