package com.ashish.grocery.seller.models



class ProductsModel() {
    var productName: String=""
    var productDescription: String=""
    var productCategory: String=""
    var productQuantity: String=""
    var originalPrice: String=""
    var discountPrice: String=""
    var discountNote: String=""
    var discountAvailable: String=""
    var productId: String=""
    var productImage: String=""
    var timestamp: String=""

    constructor(productName: String,productDescription: String,productCategory: String,productQuantity: String
                ,originalPrice: String,discountPrice: String,discountNote: String,discountAvailable: Boolean,productId: String
                ,timestamp: String) : this() {
        this.productName=productName
        this.productDescription=productDescription
        this.productCategory=productCategory
        this.productQuantity=productQuantity
        this.originalPrice=originalPrice
        this.discountPrice=discountPrice
        this.discountNote=discountNote
        this.discountAvailable= discountAvailable.toString()
        this.productId=productId
        this.timestamp=timestamp

    }

}
