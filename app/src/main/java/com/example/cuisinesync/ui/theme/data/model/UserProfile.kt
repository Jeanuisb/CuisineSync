package com.example.cuisinesync.ui.theme.data.model

import io.realm.kotlin.mongodb.User
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class UserProfile : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var dateOfBirth: String = ""

    var email: String = ""

    var firstName: String = ""

    var lastName: String = ""

}





