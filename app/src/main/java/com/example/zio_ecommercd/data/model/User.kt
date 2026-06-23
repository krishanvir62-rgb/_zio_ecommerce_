package com.example.zio_ecommercd.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("userid")
    @set:PropertyName("userid")
    var id: String = "",
    
    var name: String = "",
    var email: String = "",
    
    @get:PropertyName("address")
    @set:PropertyName("address")
    var phone: String = "", // Reusing phone field for address as per user's firestore screenshot
    
    var photoUrl: String = ""
)
