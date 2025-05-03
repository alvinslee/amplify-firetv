package com.example.amplifyfiretv

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Consumer

class SignInActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SignInActivity"
    }

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var submitButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        errorText = findViewById(R.id.error_text)
        submitButton = findViewById(R.id.submit_button)

        submitButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isBlank() || password.isBlank()) {
                errorText.text = "Please enter both email and password"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            Amplify.Auth.signIn(
                email,
                password,
                { result: AuthSignInResult ->
                    Log.d(TAG, "Sign in result: $result")
                    Log.d(TAG, "Is signed in: ${result.isSignedIn}")
                    Log.d(TAG, "Next step: ${result.nextStep}")
                    if (result.isSignedIn) {
                        Log.i(TAG, "Sign in succeeded")
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e(TAG, "Sign in not complete")
                        runOnUiThread {
                            errorText.text = "Sign in not complete"
                            errorText.visibility = View.VISIBLE
                        }
                    }
                },
                { error: AuthException ->
                    Log.e(TAG, "Sign in failed", error)
                    Log.e(TAG, "Error message: ${error.message}")
                    Log.e(TAG, "Error cause: ${error.cause}")
                    Log.e(TAG, "Recovery suggestion: ${error.recoverySuggestion}")
                    runOnUiThread {
                        errorText.text = "Sign in failed: ${error.message}"
                        errorText.visibility = View.VISIBLE
                    }
                }
            )
        }
    }
} 