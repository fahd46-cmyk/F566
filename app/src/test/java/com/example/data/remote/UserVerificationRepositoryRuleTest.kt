package com.example.data.remote

import android.os.Looper
import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.Shadows
import java.util.UUID

class UserVerificationRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndVerifyOtp_authenticatedOwner_succeeds() = runBlocking {
        val email = "alice_${UUID.randomUUID()}@test.com"
        val uid = signInTestUser(email)
        val repository = UserVerificationRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createInitialVerificationRecord(
                email = email,
                displayName = "Alice Tester",
                otpCode = "584920"
            )
        }
        assertTrue(
            "Expected createInitialVerificationRecord to succeed, got: ${createResult.exceptionOrNull()}",
            createResult.isSuccess
        )
        assertEquals("584920", createResult.getOrThrow())

        val docResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getVerificationByUserId(uid)
        }
        assertTrue(
            "Expected getVerificationByUserId to succeed, got: ${docResult.exceptionOrNull()}",
            docResult.isSuccess
        )
        val doc = docResult.getOrThrow()
        assertNotNull(doc)
        assertEquals(false, doc?.isVerified)
        assertEquals("584920", doc?.otpCode)

        val verifyResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.markEmailCodeVerified()
        }
        assertTrue(
            "Expected markEmailCodeVerified to succeed, got: ${verifyResult.exceptionOrNull()}",
            verifyResult.isSuccess
        )

        val verifiedDoc = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getVerificationByUserId(uid).getOrThrow()
        }
        assertEquals(true, verifiedDoc?.isVerified)
    }

    @Test
    fun getVerificationByUserId_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceEmail = "alice_${UUID.randomUUID()}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = UserVerificationRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createInitialVerificationRecord(
                email = aliceEmail,
                displayName = "Alice",
                otpCode = "123456"
            ).getOrThrow()
        }

        val bobEmail = "bob_${UUID.randomUUID()}@test.com"
        signInTestUser(bobEmail)
        val bobRepo = UserVerificationRepository(firestore)

        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getVerificationByUserId(aliceUid)
        }
        assertTrue(crossReadResult.isFailure)
        val fsError = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun observeUserVerification_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceEmail = "alice_${UUID.randomUUID()}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val repository = UserVerificationRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createInitialVerificationRecord(
                email = aliceEmail,
                displayName = "Alice",
                otpCode = "654321"
            ).getOrThrow()
        }

        auth.signOut()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        delay(150)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        var thrownException: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeUserVerification(aliceUid).first()
            }
        } catch (e: Throwable) {
            thrownException = e
        }

        assertNotNull(thrownException)
        val fsException = thrownException?.unwrapFirestoreException()
        assertNotNull(fsException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException?.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
