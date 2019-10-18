package com.example.firebasechatingapp.data.firebase

import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.firebasechatingapp.data.model.Chat
import com.example.firebasechatingapp.data.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Completable
import io.reactivex.Observable


class FirebaseSource {
    private val ROOT_REF: String = "users"
    private val IMAGE_REF: String = "images"
    private val CHAT_REF:String = "user_messages"
    private val LATEST_MSG_REF:String = "latest_messages"
    private val OVERWRITE_DOC = "latest"
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val fireStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firebaseRTDb: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    fun registerUser(email: String, pwd: String) = Completable.create { emitter ->
        firebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener { task ->
            if (!emitter.isDisposed) {
                if (task.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.onError(task.exception!!)
                }
            }
        }
    }

    fun logInUser(email: String, pwd: String) = Completable.create { emitter ->
        firebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener { task ->
            if (!emitter.isDisposed) {
                if (task.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.onError(task.exception!!)
                }
            }
        }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun signOutUser() = firebaseAuth.signOut()

    fun getUserData(): Observable<List<User>> = Observable.create {emitter ->
        val listOfUser = ArrayList<User>()
        fireStore.collection("users").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(!emitter.isDisposed){
                if(firebaseFirestoreException!=null){
                    emitter.onError(FirebaseException(firebaseFirestoreException.toString()))
                }else{
                    for (docSnapshot in querySnapshot!!){
                        listOfUser.add(docSnapshot.toObject(User::class.java))
                    }
                    emitter.onNext(listOfUser)
                }
            }
        }
    }

    fun addUser(user: User) = Completable.create {emitter->
        if(getCurrentUser()!=null) {
            user.id = getCurrentUser()!!.uid
            if (user.email == null) user.email = getCurrentUser()!!.email!!
            fireStore.collection(ROOT_REF).document(getCurrentUser()!!.uid).set(user, SetOptions.merge())
                .addOnSuccessListener {
                    if(!emitter.isDisposed){
                        emitter.onComplete()
                    }
                }
                .addOnFailureListener{
                    if(!emitter.isDisposed){
                        emitter.onError(it)
                    }
                }
        }
    }

    fun updateUser(userName:String?,email:String?)= Completable.create{emitter ->
        val uid = getCurrentUser()?.uid
        if(uid!=null){
            var data = mutableMapOf<String,String>()
            if(userName!=null && email!=null){
                data.put("userName",userName)
                data.put("email",email)
            }else if(userName==null && email!=null){
                data.put("email",email)
            }else if(email==null && userName!=null){
                data.put("userName",userName)
            }

            if(!data.isEmpty()){
                fireStore.collection(ROOT_REF).document(uid).update(data as Map<String, Any>).addOnSuccessListener {
                    if(!emitter.isDisposed){
                        emitter.onComplete()
                    }
                }.addOnFailureListener {
                    if(!emitter.isDisposed){
                        emitter.onError(it)
                    }
                }
            }
        }
    }

    fun addChat(chat:Chat) = Completable.create {emitter ->
        val fromRef = fireStore.collection(CHAT_REF).document(chat.fromId!!).collection(chat.toId!!).document(chat.id!!)
        fromRef.set(chat).addOnSuccessListener {
            val toRef = fireStore.collection(CHAT_REF).document(chat.toId!!).collection(chat.fromId!!).document(chat.id!!)
            toRef.set(chat).addOnSuccessListener {
                if(!emitter.isDisposed){
                    emitter.onComplete()
                }
            }.addOnFailureListener {
                if(!emitter.isDisposed){
                    emitter.onError(it)
                }
            }
        }.addOnFailureListener {
            if(!emitter.isDisposed){
                emitter.onError(it)
            }
        }
    }
    
    fun getChat(fromId:String, toId:String): Observable<List<Chat>> = Observable.create {emitter ->
        fireStore.collection(CHAT_REF).document(fromId).collection(toId).orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(!emitter.isDisposed){
                var chatList = ArrayList<Chat>()
                if(firebaseFirestoreException!=null){
                    emitter.onError(FirebaseException(firebaseFirestoreException.toString()))
                }else{
                    for (docSnapshot in querySnapshot!!){
                        chatList.add(docSnapshot.toObject(Chat::class.java))
                    }
                    emitter.onNext(chatList)
                }
            }
        }
    }

    fun getChatAdded(fromId:String, toId:String) : Observable<Chat> = Observable.create {emitter ->
        fireStore.collection(CHAT_REF).document(fromId).collection(toId).orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException!=null){
                if(!emitter.isDisposed){
                    emitter.onError(FirebaseException(firebaseFirestoreException.toString()))
                }
            }else{
                if(!emitter.isDisposed){
                    for (dc in querySnapshot!!.documentChanges ){
                        when(dc.type){
                            DocumentChange.Type.ADDED -> { emitter.onNext(dc.document.toObject(Chat::class.java))}
                            DocumentChange.Type.MODIFIED -> {}
                            DocumentChange.Type.REMOVED -> {}
                        }
                    }
                }
            }

        }
    }

    fun addToLatestMessage(chat:Chat) = Completable.create { emitter ->
        val hashMap1 = hashMapOf<String,Chat>(chat.toId!! to chat)
        val hashMap2 = hashMapOf<String,Chat>(chat.fromId!! to chat)
        fireStore.collection(LATEST_MSG_REF).document(chat.fromId!!).collection(OVERWRITE_DOC).document(chat.toId!!).set(chat).addOnSuccessListener {
            fireStore.collection(LATEST_MSG_REF).document(chat.toId!!).collection(OVERWRITE_DOC).document(chat.fromId!!).set(chat).addOnSuccessListener {
                if(!emitter.isDisposed){
                    emitter.onComplete()
                }
            }.addOnFailureListener {
                if(!emitter.isDisposed){
                    emitter.onError(it)
                }
            }
        }.addOnFailureListener {
            if(!emitter.isDisposed){
                emitter.onError(it)
            }
        }
    }

    fun getLatestMessage(fromId: String):Observable<List<Chat>> = Observable.create { emitter ->
        fireStore.collection(LATEST_MSG_REF).document(fromId).collection(OVERWRITE_DOC).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(!emitter.isDisposed){
                var chatList = ArrayList<Chat>()
                if(firebaseFirestoreException!=null){
                    emitter.onError(FirebaseException(firebaseFirestoreException.toString()))
                }else{
                    for (docSnapshot in querySnapshot!!){
                        chatList.add(docSnapshot.toObject(Chat::class.java))
                    }
                    emitter.onNext(chatList)
                }
            }
        }
    }

    fun addPhotoToStorage(imageUri:Uri, fileExtension:String) = Completable.create{emitter->
        val ref = fireStorage.getReference(IMAGE_REF).child(System.currentTimeMillis().toString() + fileExtension)
        ref.putFile(imageUri).addOnSuccessListener {taskSnapshot ->
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {uri ->
                val uid = getCurrentUser()?.uid
                if(uid!=null){
                    val downloadLink = hashMapOf("imageLink" to uri.toString())
                    fireStore.collection(ROOT_REF).document(uid).set(downloadLink, SetOptions.merge()).addOnSuccessListener {
                        if(!emitter.isDisposed){
                            emitter.onComplete()
                        }
                    }.addOnFailureListener{
                        if(!emitter.isDisposed){
                            emitter.onError(it)
                        }
                    }
                }else{

                }
            }.addOnFailureListener {
                if(!emitter.isDisposed){
                    emitter.onError(it)
                }
            }
        }.addOnFailureListener {
            if(!emitter.isDisposed){
                emitter.onError(it)
            }
        }
    }

    fun getUserInfo(uid:String): Observable<User> = Observable.create {emitter ->
        var user:User?=null
        if(uid!=null){
            if(!emitter.isDisposed){
                fireStore.collection(ROOT_REF).document(uid).get().addOnSuccessListener {documentSnapshot ->
                    user = documentSnapshot.toObject(User::class.java)
                    emitter.onNext(user!!)
                }.addOnFailureListener {
                    emitter.onError(it)
                }
            }
        }
    }

    fun getUserDataRTDb(): Observable<List<User>> = Observable.create { subscriber->
        val listOfUser = ArrayList<User>()
        val ref = firebaseRTDb.getReference(ROOT_REF)
        ref.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children){
                    listOfUser.add(snapshot.getValue(User::class.java)!!)
                }
                if(!subscriber.isDisposed){
                    subscriber.onNext(listOfUser)
                }
            }

            override fun onCancelled(e: DatabaseError) {
                if(!subscriber.isDisposed){
                    subscriber.onError(FirebaseException(e.message))
                }
            }

        })
    }

    fun addUserRTDb(user: User) = Completable.create {emitter ->
        val ref = firebaseRTDb.getReference(ROOT_REF)
        if(getCurrentUser()!=null){
            user.id = getCurrentUser()!!.uid
            if(user.email==null)user.email = getCurrentUser()!!.email!!
            ref.child(getCurrentUser()!!.uid).setValue(user).addOnCompleteListener {task->
                if(!emitter.isDisposed){
                    if(task.isSuccessful){
                        emitter.onComplete()
                    }else{
                        emitter.onError(task.exception!!)
                    }
                }

            }
        }

    }
}