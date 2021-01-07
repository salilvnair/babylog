package com.salilvnair.babylog.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.salilvnair.babylog.model.BabyLogModel
import java.util.stream.Collectors

class FireBaseDataHandler {

    private val db = Firebase.firestore

    fun db(): FirebaseFirestore {
        return db
    }
    fun add(model: BabyLogModel) {
        val log = hashMapOf(
            "lastFed" to Timestamp(model.lastFed!!)
        )
        db.collection("babylogs")
            .add(log)
            .addOnSuccessListener { documentReference ->
                Log.d("FB_ADD", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FB_ADD_FAILED", "Error adding document", e)
            }
    }

    fun update(babyLogModel: BabyLogModel) {
        val log = hashMapOf(
            "lastFed" to Timestamp(babyLogModel.lastFed!!)
        )
        db
            .collection("babylogs")
            .document(babyLogModel.key!!)
            .set(log)

    }




    fun remove(babyLogModel: BabyLogModel) {
        db
            .collection("babylogs")
            .document(babyLogModel.key!!)
            .delete()
    }

}