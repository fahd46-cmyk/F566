# Jissr VPN Security Specification (`security_spec.md`)

## 1. Data Invariants
1. **Authenticated Ownership**: Every `/user_verifications/{userId}` document can only be created, read (`get`, `list`), updated, or deleted by the authenticated user whose `request.auth.uid == userId` and `resource.data.userId == request.auth.uid`.
2. **Schema & Key Strictness**: Documents in `/user_verifications/{userId}` must strictly have keys `['userId', 'email', 'displayName', 'otpCode', 'isVerified', 'createdAt', 'updatedAt']` on create, with no ghost or unvalidated keys permitted.
3. **Immutable Identity & Creation Timestamp**: On update, `userId`, `email`, and `createdAt` are strictly immutable (`incoming().userId == existing().userId`, `incoming().email == existing().email`, `incoming().createdAt == existing().createdAt`).
4. **Bounded String & OTP Format**: `otpCode` must be a 6-digit numeric string (`^[0-9]{6}$`), `email` must be between 3 and 254 characters, and `displayName` must be between 1 and 120 characters.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read (`get` / `list`)**: `auth = null` attempting to read `/user_verifications/alice_123`.
2. **Cross-User Read (`get`)**: `auth.uid = "bob_456"` attempting to read `/user_verifications/alice_123`.
3. **Cross-User Query (`list`)**: `auth.uid = "bob_456"` querying `/user_verifications` without `where("userId", "==", "bob_456")` or querying Alice's records.
4. **Identity Spoofing on Create**: `auth.uid = "alice_123"` creating `/user_verifications/alice_123` with `userId: "bob_456"`.
5. **Path Mismatch on Create**: `auth.uid = "alice_123"` creating `/user_verifications/bob_456` with `userId: "alice_123"`.
6. **Shadow Field Injection on Create**: Including an extra key `isAdmin: true` in `/user_verifications/alice_123`.
7. **Shadow Field Injection on Update**: Updating `/user_verifications/alice_123` with an undeclared key `role: "superuser"`.
8. **Invalid OTP Format**: Creating or updating with `otpCode: "12345"` (5 digits) or `"ABCDEF"`.
9. **Immutable Field Mutation (`createdAt`)**: Updating `createdAt` to a new timestamp after creation.
10. **Immutable Field Mutation (`userId` / `email`)**: Updating `userId` or `email` to another user's identity.
11. **Oversized String Resource Poisoning**: Submitting a `displayName` > 120 chars or `email` > 254 chars.
12. **Future Timestamp Spoofing**: Submitting `createdAt` or `updatedAt` in the future (`> request.time`).
