//package com.jkjk.quicknote.settings
//
//import android.app.Activity.RESULT_OK
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.Scope
//import com.google.api.client.extensions.android.http.AndroidHttp
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.client.json.jackson2.JacksonFactory
//import com.google.api.services.drive.Drive
//import com.google.api.services.drive.DriveScopes
//import com.jkjk.quicknote.R
//import java.util.*
//
//
//class GoogleDriveHelper(private val context: Context, private val fragment: Fragment) {
//
//    private var mDriveServiceHelper: DriveServiceHelper? = null
//
//    companion object {
//        private const val REQUEST_SIGN_IN = 1
//    }
//
//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
//        if (requestCode == REQUEST_SIGN_IN) {
//            if (resultCode == RESULT_OK && data != null) {
//                handleSignInResult(data)
//            }
//            else {
//                Log.d(javaClass.simpleName, "Signin request failed")
//            }
//            return true
//        }
//        return false
//    }
//
//    private fun buildGoogleSignInClient(): GoogleSignInClient {
//        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
//                .build()
//        return GoogleSignIn.getClient(context.applicationContext, signInOptions)
//    }
//
//    private fun handleSignInResult(result: Intent) {
//        GoogleSignIn.getSignedInAccountFromIntent(result)
//                .addOnSuccessListener { googleAccount ->
//                    Log.d(javaClass.simpleName, "Signin successful")
//
//                    // Use the authenticated account to sign in to the Drive service.
//                    mDriveServiceHelper = DriveServiceHelper(getGoogleDriveService(googleAccount))
//
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(context, "Couldn't login", Toast.LENGTH_SHORT).show()
//                    Log.e(javaClass.simpleName, "Signin error", e)
//                }
//    }
//
//
//    fun requestSignIn() {
//        val account = GoogleSignIn.getLastSignedInAccount(context)
//        if (account == null) {
//            val client = buildGoogleSignInClient()
//            fragment.startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
//        } else {
//            mDriveServiceHelper = DriveServiceHelper(getGoogleDriveService(account))
//            val fileId = mDriveServiceHelper!!.createFile().addOnSuccessListener {fileI ->
//                mDriveServiceHelper!!.saveFile(fileI, "test", "ok!").addOnSuccessListener {
//                    mDriveServiceHelper!!.readFile(fileI).addOnSuccessListener { r ->
//                        Log.d(r.first, r.second ?: "")
//                    }.addOnFailureListener {
//                        it.printStackTrace()
//                    }
//                }.addOnFailureListener { it.printStackTrace() }
//            }.addOnFailureListener {
//                it.printStackTrace()
//            }
//        }
//    }
//
//    private fun getGoogleDriveService(account: GoogleSignInAccount): Drive {
//        val credential = GoogleAccountCredential.usingOAuth2(
//                context, Collections.singleton(DriveScopes.DRIVE_FILE))
//        credential.selectedAccount = account.account
//        return com.google.api.services.drive.Drive.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                JacksonFactory(),
//                credential)
//                .setApplicationName(context.getString(R.string.app_name))
//                .build()
//    }
//}