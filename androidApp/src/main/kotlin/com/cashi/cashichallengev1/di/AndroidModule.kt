package com.cashi.cashichallengev1.di

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val androidModule = module {
    single<FirebaseFirestore?> {
        try {
            val context = get<android.content.Context>()
            FirebaseApp.initializeApp(context)
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("AndroidModule", "Firebase is not initialized. Using in-memory fallback.", e)
            null
        }
    }
}