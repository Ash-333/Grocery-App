package com.ashish.grocery.user.models

class CartItemModel() {
    var id:String=""
    var pId:String=""
    var name:String=""
    var price:String=""
    var cost:String=""
    var quantity:String=""
    var image:String=""

    constructor(name:String,pId:String,price:String,cost:String,quantity:String,image:String) : this() {
        this.pId=pId
        this.name=name
        this.price=price
        this.cost=cost
        this.quantity=quantity
        this.image=image
    }


}