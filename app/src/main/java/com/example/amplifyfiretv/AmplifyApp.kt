package com.example.amplifyfiretv

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.amplifyfiretv.data.CardDataProvider

class AmplifyApp : Application() {
    companion object {
        private const val TAG = "AmplifyApp"
    }

    override fun onCreate() {
        super.onCreate()
        
        CardDataProvider.initialize(applicationContext)
        
        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(applicationContext)
        } catch (error: AmplifyException) {
            Log.e(TAG, "Error initializing Amplify", error)
        } catch (error: Exception) {
            Log.e(TAG, "Unexpected error during initialization", error)
        }
    }
} 