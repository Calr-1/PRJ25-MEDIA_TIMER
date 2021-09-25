package com.example.mediatimerjp.database

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.mediatimerjp.model.Timer
import com.example.mediatimerjp.model.TimerToSave
import com.example.mediatimerjp.model.TimerWrapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask

object Database {
    private var user: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var userId: String? = null
    private lateinit var userProfile: User
    lateinit var dbRefTimers: DatabaseReference

    private lateinit var ref: DatabaseReference
    private lateinit var mediaStorageReference: StorageReference
    private lateinit var mediaDatabaseReference: DatabaseReference
    private lateinit var task: StorageTask<*>

    fun getDatabase() {

        user = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference!!.keepSynced(true)
        if (userId == null) {
            userId = user?.uid
        }
        ref = reference!!.child(userId!!)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("erro", "falhou")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                userProfile = snapshot.getValue(User::class.java)!!
            }
        })
        dbRefTimers = FirebaseDatabase.getInstance().getReference("Timers")
        mediaStorageReference = FirebaseStorage.getInstance().getReference("Media")
        mediaDatabaseReference = FirebaseDatabase.getInstance().getReference("media")
    }

    fun getKey(key: String): String {
        var uploadId = ""
        if (key == "") uploadId = dbRefTimers.push().key!!
        else {
            dbRefTimers.orderByKey().equalTo(key)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        uploadId = if (dataSnapshot.exists()) {
                            key
                        } else {
                            dbRefTimers.push().key.toString() //cria a entrada // na base de dados
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Getting Post failed, log a message
                    }
                })
        }
        return uploadId
    }

    fun saveTimer(timer: TimerToSave) {
        dbRefTimers.child(timer.id!!).setValue(timer).addOnSuccessListener {
            Log.d("savedToFirebase", "timer ${timer.name} saved successfully")
        }.addOnFailureListener {
            Log.d("savedToFirebase", "timer ${timer.name} could not be saved")
        }
    }

    lateinit var timerToSave: TimerToSave

    fun getTimer(code: String, context: Context) {
        dbRefTimers.child(code).addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    timerToSave = snapshot.getValue(TimerToSave::class.java)!!
                    timerToSave.id =
                        getKey("")//so we dont override the database entry, and create a new one
                    TimerWrapper.getInstance().currentModel.setUploadTimer(timerToSave)
                } else {
                    Log.e("database access error", "access failed")
                    Toast.makeText(context, "Timer does not exist!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun removeTimer(code: String) {
        dbRefTimers.child(code).removeValue { error, ref -> Log.e("timer removed", code) }
    }

    private fun getFileExtension(uri: Uri, context: Context): String? {
        val cR: ContentResolver = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    fun uploadFile(uri:Uri, context: Context, timer: Timer):String {

        var url = ""
        val fileReference: StorageReference = mediaStorageReference.child(
            System.currentTimeMillis().toString() + "." + getFileExtension(uri, context)
        )
        task = fileReference.putFile(uri).addOnSuccessListener { taskSnapshot ->
            val task = taskSnapshot.metadata!!.reference!!.downloadUrl
            task.addOnSuccessListener { uri ->
                url = uri.toString()
                val uploadId: String? = mediaDatabaseReference.push().key //cria a entrada
                mediaDatabaseReference.child(uploadId!!).setValue(url) // na base de dados
                timer.upload = url
            }

        }.addOnFailureListener { e ->
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
        return url
    }

    fun uploadSounds(uri:Uri, context: Context, timer: Timer):String {

        var url = ""
        val fileReference: StorageReference = mediaStorageReference.child(
            System.currentTimeMillis().toString() + "." + getFileExtension(uri, context)
        )
        task = fileReference.putFile(uri).addOnSuccessListener { taskSnapshot ->
            val task = taskSnapshot.metadata!!.reference!!.downloadUrl
            task.addOnSuccessListener { uri ->
                url = uri.toString()
                val uploadId: String? = mediaDatabaseReference.push().key //cria a entrada
                mediaDatabaseReference.child(uploadId!!).setValue(url) // na base de dados
                timer.sounds.add(Uri.parse(url))
            }

        }.addOnFailureListener { e ->
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
        return url
    }

    fun uploadSound(uri:Uri, context: Context, timer: Timer):String {

        var url = ""
        val fileReference: StorageReference = mediaStorageReference.child(
            System.currentTimeMillis().toString() + "." + getFileExtension(uri, context)
        )
        task = fileReference.putFile(uri).addOnSuccessListener { taskSnapshot ->
            val task = taskSnapshot.metadata!!.reference!!.downloadUrl
            task.addOnSuccessListener { uri ->
                url = uri.toString()
                val uploadId: String? = mediaDatabaseReference.push().key //cria a entrada
                mediaDatabaseReference.child(uploadId!!).setValue(url) // na base de dados
                timer.alarmUri = Uri.parse(url)
            }

        }.addOnFailureListener { e ->
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
        return url
    }



    fun setUserId(ref: String) {
        this.userId = ref
    }

    fun getUserId(): String? {
        return userId
    }

    fun getUser(): User {
        return userProfile
    }

    fun setUser(new_user: User) {
        reference!!.child(user!!.uid)
            .setValue(new_user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("user saved", "successful")
                } else {
                    Log.e("user saved", "failed")
                }
            }
    }
}