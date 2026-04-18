package com.app.findback.data.source.remote

import com.app.findback.domain.models.CircleZone
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseCircleZoneDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("circle_zones")
    private var listener: ValueEventListener? = null

    //create circle zone
    fun createCircleZone(onSuccess: (Boolean) -> Unit, circleZone: CircleZone,userId:String) {
       if(circleZone.id == null) return
        database.child(userId).child(circleZone.id).setValue(circleZone.toMap())
            .addOnSuccessListener {
                onSuccess(true)
            }
            .addOnFailureListener { error ->
                onSuccess(false)
            }
    }
    //get circle zone
    fun getCircleZones(onData: (List<CircleZone>) -> Unit) {
        if (listener != null) return
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val circleZones = snapshot.children.mapNotNull {
                    (it.value as? Map<String, Any?>)?.let(CircleZone::fromMap)
                }
                onData(circleZones)
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        database.addValueEventListener(listener!!)
    }
    fun getCircleZonesByUserId(onData: (List<CircleZone>) -> Unit, userId: String){
        if (listener != null) return
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val circleZones = snapshot.children.mapNotNull {
                    (it.value as? Map<String, Any?>)?.let(CircleZone::fromMap)
                }
                onData(circleZones)
            }
            override fun onCancelled(p0: DatabaseError) {}
        }
        database.child(userId).addValueEventListener(listener!!)
    }
    //stop circle zone
    fun stopListening() {
        listener?.let {
            database.removeEventListener(it)
            listener = null
        }
    }

    //xóa circle zone
    fun deleteCircleZone(onSuccess: (Boolean) -> Unit, circleZone: CircleZone,userId:String) {
        if(circleZone.id == null) return
        database.child(userId).child(circleZone.id).removeValue()
            .addOnSuccessListener {
                onSuccess(true)
            }
            .addOnFailureListener { error ->
                onSuccess(false)
            }
    }
    //update circle zone
    fun updateCircleZone(onSuccess: (Boolean) -> Unit, circleZone: CircleZone,userId:String) {
        if(circleZone.id == null) return
        database.child(userId).child(circleZone.id).setValue(circleZone.toMap())
            .addOnSuccessListener {
                onSuccess(true)
            }
            .addOnFailureListener { error ->
                onSuccess(false)
            }
    }
}
