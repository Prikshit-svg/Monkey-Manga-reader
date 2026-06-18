package com.example.myapplication

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object Injection{
    private var instance: FirebaseFirestore? = null
    fun instance(): FirebaseFirestore {
        val currentInstance=instance
        if (currentInstance!=null) {
            return currentInstance
        }
        val newInstance= Firebase.firestore
        instance=newInstance
        return newInstance


        /*This line returns the value of the private val instance property.
        •If this is the first time Injection.instance() is called (or the first time the internal
        instance property is accessed), the by lazy block will execute, FirebaseFirestore.getInstance()
         will be called, and that result will be stored in the instance property and then returned.
        •On subsequent calls to Injection.instance(), the already created and stored FirebaseFirestore
        object will be returned directly.
        */
    }



}