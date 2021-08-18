package com.ashish.grocery.user.models

class OrderItemModel (){
    var pId:String=""
    var id:String=""
    var cost:String=""
    var name:String=""
    var price:String=""
    var quantity:String=""
    constructor(pId:String,id:String,cost:String,name:String,price:String,quantity:String) : this() {
        this.pId=pId
        this.id=id
        this.cost=cost
        this.name=name
        this.price=price
        this.quantity=quantity
    }
}