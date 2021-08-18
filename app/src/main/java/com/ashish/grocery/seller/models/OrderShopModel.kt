package com.ashish.grocery.seller.models

class OrderShopModel() {
    var orderId: String = ""
    var orderTime: String = ""
    var orderStatus: String = ""
    var orderBy: String = ""
    var orderTo: String = ""
    var orderCost: String = ""
    var latitude: String = ""
    var longitude: String = ""
    var deliveryFees: String = ""

    constructor(
        orderId: String,
        orderTime: String,
        orderStatus: String,
        orderBy: String,
        orderTo: String,
        orderCost: String,
        latitude: String,
        longitude: String,
        deliveryFees: String
    ) : this() {

    }
}