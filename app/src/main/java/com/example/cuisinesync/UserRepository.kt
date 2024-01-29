package com.example.cuisinesync

import android.util.Log
import com.example.cuisinesync.ui.theme.data.model.UserProfile
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.isManaged
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.sync.SyncConfiguration
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

    private suspend fun storeUserDataInMongoDB(user: User, userData: UserData) {
        withContext(Dispatchers.IO) {
            val config = SyncConfiguration.Builder(user, setOf(UserProfile::class)).build()
            val realm = Realm.open(config)

            try {
                realm.writeBlocking {
                    // Query for existing user
                    val existingUser = query<UserProfile>("email == $0", userData.email).first().find()

                    // Create new user or update existing one
                    val userProfile = existingUser ?: UserProfile().apply {
                        this._id = ObjectId() // Assign a new ObjectId
                        this.email = userData.email
                        this.firstName = userData.firstName
                        this.lastName = userData.lastName
                        this.dateOfBirth = userData.dateOfBirth
                    }

                    // Add the new user to Realm if it's a new object
                    if (!userProfile.isManaged()) {
                        copyToRealm(userProfile)
                    }
                }
            } catch (e: Exception) {
                // Handle error, log it or notify the user
                Log.e("Realm", "Error storing user data in MongoDB: ${e.message}")
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

    // Format this as needed
        // Add other fields as necessary
    )



}

