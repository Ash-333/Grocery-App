package com.ashish.grocery.user.models

class PopularItemModel() {
    var productName:String=""
    var productImage:String=""
    var originalPrice:String=""
    var discountPrice:String=""
    var discountNote:String=""
    var discountAvailable:String=""
    var productDescription:String=""
    var productQuantity:String=""
    var productId:String=""
    constructor(productName:String,productImage:String,originalPrice:String,discountPrice:String,discountNote:String,discountAvailable:String,productDescription:String,productQuantity:String,productId:String) : this() {
        this.productName=productName
        this.productImage=productImage
        this.originalPrice=originalPrice
        this.discountPrice=discountPrice
        this.discountNote=discountNote
        this.discountAvailable=discountAvailable
        this.productDescription=productDescription
        this.productQuantity=productQuantity
        this.productId=productId
    }

}