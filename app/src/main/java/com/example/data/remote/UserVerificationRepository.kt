package com.example.data.remote

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

@IgnoreExtraProperties
data class UserVerificationDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val otpCode: String = "",
    @get:PropertyName("isVerified")
    val isVerified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class UserVerificationRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth
        get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun generateSixDigitCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    private fun parseSnapshot(snapshot: DocumentSnapshot): UserVerificationDoc? {
        if (!snapshot.exists()) return null
        val mapped = snapshot.toObject(
            UserVerificationDoc::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ) ?: return null
        val rawVerified = snapshot.getBoolean("isVerified") ?: mapped.isVerified
        return mapped.copy(isVerified = rawVerified)
    }

    suspend fun createInitialVerificationRecord(
        email: String,
        displayName: String,
        otpCode: String = generateSixDigitCode()
    ): Result<String> {
        val uid = requireUserId()
        val safeEmail = email.trim().ifBlank { "user@gmail.com" }.take(254)
        val safeName = displayName.trim().ifBlank { safeEmail.substringBefore("@").ifBlank { "Jissr User" } }.take(120)
        val normalizedOtp = otpCode.filter { it.isDigit() }.padStart(6, '0').take(6)
        val docRef = db.collection(COLLECTION_NAME).document(uid)

        return try {
            val payload = mapOf(
                "userId" to uid,
                "email" to safeEmail,
                "displayName" to safeName,
                "otpCode" to normalizedOtp,
                "isVerified" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(normalizedOtp)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun refreshVerificationCode(
        newOtpCode: String = generateSixDigitCode()
    ): Result<String> {
        val uid = requireUserId()
        val normalizedOtp = newOtpCode.filter { it.isDigit() }.padStart(6, '0').take(6)
        val docRef = db.collection(COLLECTION_NAME).document(uid)

        return try {
            val updates = mapOf(
                "otpCode" to normalizedOtp,
                "isVerified" to false,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(normalizedOtp)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markEmailCodeVerified(): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection(COLLECTION_NAME).document(uid)
        return try {
            val updates = mapOf(
                "isVerified" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getVerificationByUserId(targetUserId: String = requireUserId()): Result<UserVerificationDoc?> {
        val docRef = db.collection(COLLECTION_NAME).document(targetUserId)
        return try {
            val snapshot = docRef.get().await()
            Result.success(parseSnapshot(snapshot))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserVerification(userId: String = requireUserId()): Flow<UserVerificationDoc?> {
        val docRef = db.collection(COLLECTION_NAME).document(userId)
        return docRef.snapshots()
            .map { snapshot -> parseSnapshot(snapshot) }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    companion object {
        const val COLLECTION_NAME = "user_verifications"
    }
}
