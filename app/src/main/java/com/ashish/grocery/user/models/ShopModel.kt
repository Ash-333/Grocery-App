package com.ashish.grocery.user.models

class ShopModel() {
    var uid: String = ""
    var email:String = ""
    var name:String =""
    var shopName: String = ""
    var phone: String =""
    var deliveryFees: String = ""
    var longitude: String = ""
    var latitude: String = ""
    var online: String= ""
    var profileImage: String = ""
    var shopOpen: String= ""

    constructor(uid:String,email:String,name:String,shopName: String,phone: String,deliveryFees: String,
                longitude: String,latitude: String,online: String,profileImage: String,shopOpen: String) : this() {
                    this.uid=uid
        this.email=email
        this.name=name
        this.shopName=shopName
        this.phone=phone
        this.deliveryFees=deliveryFees
        this.longitude=longitude
        this.latitude=latitude
        this.online=online
        this.profileImage=profileImage
        this.shopOpen=shopOpen
    }


}