package com.jkjk.quicknote.settings

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class GDriveHelper(private val context: Context, private val fragment: Fragment) {

    companion object {
        private const val REQUEST_SIGN_IN = 1
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK && data != null) {
                handleSignInResult(data)
            }
            else {
                Log.d(javaClass.simpleName, "Signin request failed")
            }
            return true
        }
        return false
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // .requestScopes(Drive.SCOPE_FILE)
                // .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(Scope(DriveScopes.DRIVE))
                .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { googleAccount ->
                    Log.d(javaClass.simpleName, "Signin successful")

                    // Use the authenticated account to sign in to the Drive service.
//                    val credential = GoogleAccountCredential.usingOAuth2(
//                            this, listOf(DriveScopes.DRIVE_FILE)
//                    )
//                    credential.selectedAccount = googleAccount.account
//                    val googleDriveService = Drive.Builder(
//                            AndroidHttp.newCompatibleTransport(),
//                            JacksonFactory.getDefaultInstance(),
//                            credential)
//                            .setApplicationName(getString(R.string.app_name))
//                            .build()

                    // https://developers.google.com/drive/api/v3/search-files
                    // https://developers.google.com/drive/api/v3/search-parameters
                    // https://developers.google.com/drive/api/v3/mime-types

                }
                .addOnFailureListener { e ->
                    Log.e(javaClass.simpleName, "Signin error", e)
                }
    }

    fun requestSignIn() {
        val client = buildGoogleSignInClient()
        fragment.startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }
}