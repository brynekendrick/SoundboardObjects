package com.example.mobdev

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * SharedPreferences session + Firebase Auth/Firestore (users collection).
 */
object UserSession {
    const val PREFS_NAME = "user_prefs"

    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_BIO = "bio"
    private const val KEY_MEMBER_SINCE = "member_since"
    private const val KEY_CREATED_SOUNDS = "created_sounds_count"
    private const val KEY_CUSTOM_SOUNDS_JSON = "custom_sounds_json"

    private val firebaseAuth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getUsername(ctx: Context): String =
        prefs(ctx).getString(KEY_USERNAME, null)?.takeIf { it.isNotBlank() } ?: "Guest"

    fun getEmail(ctx: Context): String =
        prefs(ctx).getString(KEY_EMAIL, "") ?: ""

    fun getBio(ctx: Context): String =
        prefs(ctx).getString(KEY_BIO, "") ?: ""

    fun getMemberSinceLabel(ctx: Context): String {
        val stored = prefs(ctx).getString(KEY_MEMBER_SINCE, null)
        if (!stored.isNullOrBlank()) return stored
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return "Member since: ${fmt.format(Date())}"
    }

    fun getCreatedSoundsCount(ctx: Context): Int =
        prefs(ctx).getInt(KEY_CREATED_SOUNDS, 0)

    fun onLogin(ctx: Context, username: String) {
        val p = prefs(ctx)
        val editor = p.edit().putString(KEY_USERNAME, username.trim())
        if (p.getString(KEY_MEMBER_SINCE, null).isNullOrBlank()) {
            val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            editor.putString(KEY_MEMBER_SINCE, "Member since: ${fmt.format(Date())}")
        }
        editor.apply()
    }

    fun onFirebaseAuthSuccess(
        ctx: Context,
        email: String,
        displayName: String,
        bio: String? = null
    ) {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val editor = prefs(ctx).edit()
            .putString(KEY_USERNAME, displayName.trim().ifBlank { email.substringBefore("@") })
            .putString(KEY_EMAIL, email.trim())
            .putString(KEY_MEMBER_SINCE, "Member since: ${fmt.format(Date())}")
        if (!bio.isNullOrBlank()) {
            editor.putString(KEY_BIO, bio.trim())
        }
        editor.apply()
    }

    fun onRegister(ctx: Context, username: String) {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        prefs(ctx).edit()
            .putString(KEY_USERNAME, username.trim())
            .putString(KEY_MEMBER_SINCE, "Member since: ${fmt.format(Date())}")
            .putString(KEY_EMAIL, "")
            .putString(KEY_BIO, "")
            .apply()
    }

    fun onGuestContinue(ctx: Context) {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        prefs(ctx).edit()
            .putString(KEY_USERNAME, "Guest")
            .putString(KEY_MEMBER_SINCE, "Member since: ${fmt.format(Date())}")
            .apply()
    }

    fun saveProfile(ctx: Context, username: String, email: String, bio: String) {
        prefs(ctx).edit()
            .putString(KEY_USERNAME, username.trim())
            .putString(KEY_EMAIL, email.trim())
            .putString(KEY_BIO, bio.trim())
            .apply()
    }

    fun incrementCreatedSounds(ctx: Context) {
        val n = getCreatedSoundsCount(ctx) + 1
        prefs(ctx).edit().putInt(KEY_CREATED_SOUNDS, n).apply()
    }

    fun getCustomSoundsJson(ctx: Context): String =
        prefs(ctx).getString(KEY_CUSTOM_SOUNDS_JSON, "[]") ?: "[]"

    fun saveCustomSoundsJson(ctx: Context, json: String) {
        prefs(ctx).edit().putString(KEY_CUSTOM_SOUNDS_JSON, json).apply()
    }

    data class CustomSoundEntry(val id: String, val title: String, val uriString: String)

    fun loadCustomSoundEntries(ctx: Context): List<CustomSoundEntry> {
        val raw = getCustomSoundsJson(ctx)
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        CustomSoundEntry(
                            o.getString("id"),
                            o.getString("title"),
                            o.getString("uri")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun appendCustomSound(ctx: Context, title: String, uriString: String) {
        val arr = try {
            JSONArray(getCustomSoundsJson(ctx))
        } catch (_: Exception) {
            JSONArray()
        }
        val o = JSONObject().apply {
            put("id", "import-${UUID.randomUUID()}")
            put("title", title)
            put("uri", uriString)
        }
        arr.put(o)
        saveCustomSoundsJson(ctx, arr.toString())
        incrementCreatedSounds(ctx)
    }

    // --- Firebase ---

    fun isFirebaseSignedIn(): Boolean = firebaseAuth.currentUser != null

    fun firebaseSignOut() {
        firebaseAuth.signOut()
    }

    fun firebaseRegister(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onError("Registration failed: no user id.")
                    return@addOnSuccessListener
                }
                writeFirestoreUser(uid, firstName, lastName, middleName, email, null, onSuccess, onError)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Registration failed.")
            }
    }

    fun firebaseLogin(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Login failed.")
            }
    }

    fun firebaseSaveProfile(
        firstName: String,
        lastName: String,
        email: String,
        bio: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            onError("Not signed in.")
            return
        }
        writeFirestoreUser(uid, firstName, lastName, null, email, bio, onSuccess, onError)
    }

    private fun writeFirestoreUser(
        uid: String,
        firstName: String,
        lastName: String,
        middleName: String?,
        email: String,
        bio: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )
        if (!middleName.isNullOrBlank()) {
            data["middleName"] = middleName
        }
        if (!bio.isNullOrBlank()) {
            data["bio"] = bio
        }
        firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Could not save to Firestore.")
            }
    }
}
