package com.example.mobdev

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Firestore CRUD for users/{uid} and users/{uid}/sounds/{soundId}.
 */
object FirestoreCrud {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    data class UserRecord(
        val firstName: String,
        val lastName: String,
        val middleName: String?,
        val email: String,
        val bio: String?
    ) {
        fun displayName(): String {
            val parts = mutableListOf<String>()
            if (firstName.isNotBlank()) parts.add(firstName)
            if (!middleName.isNullOrBlank()) parts.add(middleName)
            if (lastName.isNotBlank()) parts.add(lastName)
            return parts.joinToString(" ")
        }
    }

    data class SoundRecord(
        val id: String,
        val title: String,
        val uri: String,
        val category: String,
        val duration: String
    )

    private fun currentUid(): String? = auth.currentUser?.uid

    // --- User CRUD (collection: users) ---

    fun createUser(
        firstName: String,
        lastName: String,
        middleName: String?,
        email: String,
        bio: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        db.collection("users").document(uid)
            .set(userMap(firstName, lastName, middleName, email, bio))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Create failed.") }
    }

    fun readUser(
        onSuccess: (UserRecord) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = userFromDocument(doc)
                if (user != null) onSuccess(user) else onError("Profile not found.")
            }
            .addOnFailureListener { e -> onError(e.message ?: "Read failed.") }
    }

    fun updateUser(
        firstName: String,
        lastName: String,
        middleName: String?,
        email: String,
        bio: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        db.collection("users").document(uid)
            .set(userMap(firstName, lastName, middleName, email, bio), SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Update failed.") }
    }

    fun deleteUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        deleteAllSoundsThenUser(uid, onSuccess, onError)
    }

    private fun deleteAllSoundsThenUser(
        uid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("users").document(uid).collection("sounds").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    db.collection("users").document(uid).delete()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e.message ?: "Delete failed.") }
                    return@addOnSuccessListener
                }
                var pending = snapshot.size()
                var failed = false
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                        .addOnCompleteListener {
                            pending--
                            if (!it.isSuccessful) failed = true
                            if (pending == 0) {
                                if (failed) {
                                    onError("Could not delete all sounds.")
                                } else {
                                    db.collection("users").document(uid).delete()
                                        .addOnSuccessListener { onSuccess() }
                                        .addOnFailureListener { e ->
                                            onError(e.message ?: "Delete failed.")
                                        }
                                }
                            }
                        }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Delete failed.") }
    }

    // --- Sound CRUD (subcollection: users/{uid}/sounds) ---

    fun createSound(
        title: String,
        uri: String,
        category: String,
        duration: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        val soundId = "import-${System.currentTimeMillis()}"
        val data = hashMapOf(
            "title" to title,
            "uri" to uri,
            "category" to category,
            "duration" to duration,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).collection("sounds").document(soundId)
            .set(data)
            .addOnSuccessListener { onSuccess(soundId) }
            .addOnFailureListener { e -> onError(e.message ?: "Create sound failed.") }
    }

    fun readSounds(
        onSuccess: (List<SoundRecord>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        db.collection("users").document(uid).collection("sounds")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { soundFromDocument(it) }
                onSuccess(list)
            }
            .addOnFailureListener { e -> onError(e.message ?: "Read sounds failed.") }
    }

    fun updateSound(
        soundId: String,
        title: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        db.collection("users").document(uid).collection("sounds").document(soundId)
            .update("title", title)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Update sound failed.") }
    }

    fun deleteSound(
        soundId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        db.collection("users").document(uid).collection("sounds").document(soundId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Delete sound failed.") }
    }

    private fun userMap(
        firstName: String,
        lastName: String,
        middleName: String?,
        email: String,
        bio: String?
    ): HashMap<String, Any> {
        val data = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )
        if (!middleName.isNullOrBlank()) data["middleName"] = middleName
        if (!bio.isNullOrBlank()) data["bio"] = bio
        return data
    }

    private fun userFromDocument(doc: DocumentSnapshot): UserRecord? {
        if (!doc.exists()) return null
        return UserRecord(
            firstName = doc.getString("firstName") ?: "",
            lastName = doc.getString("lastName") ?: "",
            middleName = doc.getString("middleName"),
            email = doc.getString("email") ?: auth.currentUser?.email.orEmpty(),
            bio = doc.getString("bio")
        )
    }

    private fun soundFromDocument(doc: DocumentSnapshot): SoundRecord? {
        if (!doc.exists()) return null
        return SoundRecord(
            id = doc.id,
            title = doc.getString("title") ?: "Sound",
            uri = doc.getString("uri") ?: return null,
            category = doc.getString("category") ?: "Imports",
            duration = doc.getString("duration") ?: ""
        )
    }
}
