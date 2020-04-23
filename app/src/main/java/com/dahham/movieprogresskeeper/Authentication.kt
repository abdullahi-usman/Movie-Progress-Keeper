package com.dahham.movieprogresskeeper

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

class Authentication {

    data class User (var name: String, var id: String);

    private lateinit var mAuth: FirebaseAuth
    private var user: User? = null

    public fun isInitialize(): Boolean{
        return mAuth.currentUser != null
    }

    public fun getUser(): User?{
        if (isInitialize() && user == null){
            user = User(mAuth.currentUser?.displayName!!, mAuth.currentUser?.uid!!)
        }

        return user
    }

    public fun getIntent(activity: Activity): Intent {

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestEmail().requestId().build()

        return GoogleSignIn.getClient(activity, googleSignInOptions).signInIntent
    }

    fun handleIntent(data: Intent, completionListener: (User?, String?) -> Unit){
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = task.getResult(ApiException::class.java)
            user = User(account?.displayName!!, account.id!!)
            completionListener(user, null);
        }catch (e: ApiException){
            completionListener(null, e.message)
        }
    }

    fun signOut(){
        mAuth.signOut()
    }

    companion object {
        private var instance: Authentication? = null;
        fun getInstance(): Authentication{

            if (instance == null){
                instance = Authentication();
            }

            if (instance!!::mAuth.isInitialized.not()){
                instance!!.mAuth = FirebaseAuth.getInstance()
            }

            return instance!!
        }
    }
}