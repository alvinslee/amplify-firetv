package com.example.amplifyfiretv

import android.app.Application
import android.util.Log
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.example.amplifyfiretv.data.CardDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class AmplifyApp : Application() {
    private val TAG = "AmplifyApp"
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== Starting AmplifyApp initialization ===")
        
        try {
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            val outputs = AmplifyOutputs.fromResource(R.raw.amplify_outputs)
            Amplify.configure(outputs, applicationContext)
            
            applicationScope.launch {
                try {
                    CardDataProvider.initialize(applicationContext)
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing card data", e)
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during AmplifyApp initialization", e)
            e.printStackTrace()
        }
        Log.d(TAG, "=== Completed AmplifyApp initialization ===")
    }

    override fun onTerminate() {
        Log.d(TAG, "Terminating AmplifyApp")
        super.onTerminate()
        applicationScope.cancel()
    }
} 