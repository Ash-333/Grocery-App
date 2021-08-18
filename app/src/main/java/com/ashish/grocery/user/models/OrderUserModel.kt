package com.ashish.grocery.user.models

class OrderUserModel() {
    var orderId:String=""
    var orderTime:String=""
    var orderStatus:String=""
    var orderBy:String=""
    var orderTo:String=""
    var orderCost:String=""
    constructor(orderId:String,orderTime:String,orderStatus:String,orderBy:String,orderTo:String,orderCost:String) : this() {
        this.orderId=orderId
        this.orderTime=orderTime
        this.orderStatus=orderStatus
        this.orderBy=orderBy
        this.orderTo=orderTo
        this.orderCost=orderCost
    }
}