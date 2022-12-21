package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthLiveData : LiveData<FirebaseUser?>(){

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = FirebaseAuth.getInstance().currentUser
    }

    override fun onActive() {
       FirebaseAuth.getInstance().addAuthStateListener (authListener)
    }
    override fun onInactive() {
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }

}