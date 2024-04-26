package com.example.cuisinesync

import android.util.Log
import com.example.cuisinesync.ui.theme.data.model.UserProfile
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.isManaged
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.annotations.ExperimentalFlexibleSyncApi
import io.realm.kotlin.mongodb.ext.subscribe
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.mongodb.syncSession
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

class UserRepository(private val realmApp: App) {


    suspend fun registerUser(userData: UserData): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Perform registration using email and password
                realmApp.emailPasswordAuth.registerUser(userData.email, userData.password)
                val user = realmApp.login(Credentials.emailPassword(userData.email, userData.password))

                // Store additional user data
                storeUserDataInMongoDB(user, userData)

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    @OptIn(ExperimentalFlexibleSyncApi::class)
    private suspend fun storeUserDataInMongoDB(user: User, userData: UserData) {
        withContext(Dispatchers.IO) {
            val config = SyncConfiguration.Builder(user, setOf(UserProfile::class)).build()
            val realm = Realm.open(config)

            try {
                // Create a subscription for the UserProfile class
                realm.query<UserProfile>().subscribe()

                realm.writeBlocking {
                    // Query for existing user
                    val existingUser = query<UserProfile>("email == $0", userData.email).first().find()

                    if (existingUser != null) {
                        // Update existing user
                        existingUser.firstName = userData.firstName
                        existingUser.lastName = userData.lastName
                        existingUser.dateOfBirth = userData.dateOfBirth
                    } else {
                        // Create new user
                        val userProfile = UserProfile().apply {
                            this._id = ObjectId() // Assign a new ObjectId
                            this.email = userData.email
                            this.firstName = userData.firstName
                            this.lastName = userData.lastName
                            this.dateOfBirth = userData.dateOfBirth
                        }
                        copyToRealm(userProfile)
                    }
                }

                realm.syncSession.uploadAllLocalChanges()

            }  catch (e: Exception) {
                // Handle error, log it or notify the user
                Log.e("Realm", "Error storing user data in MongoDB: ${e.message}")
                // If an existing user with the same email is found, throw a specific exception
                if (e.message?.contains("duplicate key error") == true) {
                    throw Exception("User with email ${userData.email} already exists")
                } else {
                    throw e
                }
            } finally {
                realm.close() // Ensure to close the Realm instance when done
            }
        }
    }

    data class UserData(
        val email: String,
        val firstName: String,
        val lastName: String,
        val dateOfBirth: String,
        val password: String
    )



    class UserProfile : RealmObject {
        @PrimaryKey
        var _id: ObjectId = ObjectId()

        var dateOfBirth: String = ""

        var email: String = ""

        var firstName: String = ""

        var lastName: String = ""
    }


}

