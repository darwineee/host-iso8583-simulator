fun getAuthoriseSuccessRsp(): String {
    return "0110722000000A008000165372820460010317001200000000000253060611332912345600000000078300840"
}

fun getCreateTxnSuccessRsp(): String {
    return """
        {
             "code": "0000",
             "data": {
                 "currency": "USD",
                 "description": "transaction description",
                 "country_code": "VN",
                 "client_created_at": "2023-03-15 08:10:11",
                 "total_discount_amount": 10.0,
                 "total_tax_amount": 10.3,
                 "total_amount": 50.3,
                 "total_tip_amount": 0.0,
                 "status": "pending",
                 "created_at": "2023-06-06 03:35:54",
                 "transaction_public_id": "629647ea99a1e35c",
                 "remaining_settle_amount": 50.3,
                 "items": [
                     {
                         "name": "Item1",
                         "quantity": 1,
                         "base_amount": 25,
                         "total_amount": 30,
                         "total_tax_amount": 5,
                         "total_discount_amount": 0
                     }
                 ],
                 "discounts": [
                     {
                         "name": "Discount1",
                         "type": "AMOUNT",
                         "value": 5
                     }
                 ],
                 "taxes": [
                     {
                         "name": "taxes1",
                         "type": "ADDITIVE",
                         "percentage": 4
                     }
                 ],
                 "signature": null,
                 "payments": [
                     {
                         "id": 774,
                         "client_payment_id": null,
                         "payment_option_id": 1,
                         "payment_ref_id": null,
                         "total_amount": "50.30",
                         "status": "pending",
                         "refunded_amount": "0.00",
                         "paid_amount": "0.00",
                         "paid_currency": null,
                         "payment_method": "Card Payment"
                     }
                 ]
             }
         }
    """.trimIndent()
}