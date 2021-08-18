package com.ashish.grocery.user.models

class ReviewModel() {
    var uid:String=""
    var ratings:String=""
    var review:String=""
    var timeStamp:String=""
    constructor(uid:String,ratings:String,review:String,timeStamp:String) : this() {
        this.uid=uid
        this.ratings=ratings
        this.review=review
        this.timeStamp=timeStamp
    }
}