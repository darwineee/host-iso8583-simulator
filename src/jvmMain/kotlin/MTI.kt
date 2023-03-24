class MTI private constructor(
    val version: Version,
    val messageClass: MessageClass,
    val messageFunction: MessageFunction,
    val messageOrigin: MessageOrigin
) {
    enum class Version(val code: Char) {
        ISO1987('0'),
        ISO1993('1'),
        ISO2003('2'),
        REVERSED(' ');

        companion object {
            fun from(code: Char): Version {
                return when (code) {
                    '0' -> ISO1987
                    '1' -> ISO1993
                    '2' -> ISO2003
                    else -> REVERSED
                }
            }
        }
    }

    enum class MessageClass(val code: Char) {
        AUTHORIZATION('1'),
        FINANCIAL('2'),
        FILE_ACTION('3'),
        REVERSAL('4'),
        RECONCILIATION('5'),
        ADMINISTRATIVE('6'),
        FEE_COLLECTION('7'),
        NETWORK_MANAGEMENT('8'),
        REVERSED(' ');

        companion object {
            fun from(code: Char): MessageClass {
                return when (code) {
                    '1' -> AUTHORIZATION
                    '2' -> FINANCIAL
                    '3' -> FILE_ACTION
                    '4' -> REVERSAL
                    '5' -> RECONCILIATION
                    '6' -> ADMINISTRATIVE
                    '7' -> FEE_COLLECTION
                    '8' -> NETWORK_MANAGEMENT
                    else -> REVERSED
                }
            }
        }
    }

    enum class MessageFunction(val code: Char) {
        REQUEST('0'),
        REQUEST_RESPONSE('1'),
        ADVISE('2'),
        ADVISE_RESPONSE('3'),
        NOTIFICATION('4'),
        NOTIFICATION_ACK('5'),
        INSTRUCTION('6'),
        INSTRUCTION_ACK('7'),
        REVERSED(' ');

        companion object {
            fun from(code: Char): MessageFunction {
                return when (code) {
                    '0' -> REQUEST
                    '1' -> REQUEST_RESPONSE
                    '2' -> ADVISE
                    '3' -> ADVISE_RESPONSE
                    '4' -> NOTIFICATION
                    '5' -> NOTIFICATION_ACK
                    '6' -> INSTRUCTION
                    '7' -> INSTRUCTION_ACK
                    else -> REVERSED
                }
            }
        }
    }

    enum class MessageOrigin(val code: Char) {
        ACQUIRER('0'),
        ACQUIRER_REPEAT('1'),
        ISSUER('2'),
        ISSUER_REPEAT('3'),
        OTHER('4'),
        REVERSED(' ');

        companion object {
            fun from(code: Char): MessageOrigin {
                return when (code) {
                    '0' -> ACQUIRER
                    '1' -> ACQUIRER_REPEAT
                    '2' -> ISSUER
                    '3' -> ISSUER_REPEAT
                    '4' -> OTHER
                    else -> REVERSED
                }
            }
        }
    }

    class Builder {
        private var _version = Version.ISO1987
        private var _messageClass = MessageClass.AUTHORIZATION
        private var _messageFunction = MessageFunction.REQUEST
        private var _messageOrigin = MessageOrigin.ACQUIRER

        fun addVersion(version: Version): Builder {
            _version = version
            return this
        }

        fun addMessageClass(messageClass: MessageClass): Builder {
            _messageClass = messageClass
            return this
        }

        fun addMessageFunction(messageFunction: MessageFunction): Builder {
            _messageFunction = messageFunction
            return this
        }

        fun addMessageOrigin(messageOrigin: MessageOrigin): Builder {
            _messageOrigin = messageOrigin
            return this
        }

        fun build(): MTI = MTI(
            version = _version,
            messageClass = _messageClass,
            messageFunction = _messageFunction,
            messageOrigin = _messageOrigin,
        )
    }

    companion object {
        fun from(source: String): MTI {
            require(source.length == 4) { "MTI has 4 ASCII characters" }
            return MTI(
                version = Version.from(source[0]),
                messageClass = MessageClass.from(source[1]),
                messageFunction = MessageFunction.from(source[2]),
                messageOrigin = MessageOrigin.from(source[3]),
            )
        }
    }

    override fun toString(): String {
        return "${this.version.code}${this.messageClass.code}${this.messageFunction.code}${this.messageOrigin.code}"
    }
}