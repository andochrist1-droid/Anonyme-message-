const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write users, sessions, or messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "Alice",
      email: "alice@example.com",
      uniqueLinkSlug: "alice_123",
      linkActive: true,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("messages").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("sessions").get()
  );
});

test("UserProfile: owner can create and partially update profile; ghost fields and cross-user access rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "Alice",
      email: "alice@example.com",
      profilePicUri: "preset_1",
      bio: "Laissez-moi un message !",
      uniqueLinkSlug: "alice_123",
      linkActive: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial profile update matching AppRepository.updateProfile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      username: "Alice_Updated",
      bio: "Nouvelle bio",
      profilePicUri: "preset_2",
      linkActive: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field / shadow update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt / email mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      email: "spoofed@example.com",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user isolation & no blanket list
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
  await assertFails(aliceDb.collection("users").get());
});

test("AnonymousMessage: owner create, filtered list, action updates, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = Date.now();
  const ts = new Date(now - 5000);

  // Mirror AppRepository.sendAnonymousMessage payload (NO @DocumentId `id` field)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("messages")
      .doc("msg_1")
      .set({
        userId: ALICE_UID,
        messageText: "Coucou secret !",
        receivedAtMillis: now - 5000,
        unlockAtMillis: now + 345600000,
        senderPseudo: "Mystere",
        senderCoordinates: "@mystere",
        senderDeviceInfo: "Pixel 9",
        tag: "Secret",
        read: false,
        favorite: false,
        revealedManually: false,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Mirror Kotlin query: orderBy("receivedAtMillis", DESC)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("messages")
      .orderBy("receivedAtMillis", "desc")
      .get()
  );

  // Action 1: Mark read
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("messages").doc("msg_1").update({
      read: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action 2: Toggle favorite
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("messages").doc("msg_1").update({
      favorite: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action 3: Unlock immediately
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("messages").doc("msg_1").update({
      revealedManually: true,
      unlockAtMillis: Date.now(),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject future server timestamp spoofing
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("messages")
      .doc("msg_future")
      .set({
        userId: ALICE_UID,
        messageText: "Bad time",
        receivedAtMillis: now,
        unlockAtMillis: now + 1000,
        senderPseudo: "Anon",
        tag: "Secret",
        read: false,
        favorite: false,
        revealedManually: false,
        createdAt: new Date(Date.now() + 60000),
        updatedAt: new Date(Date.now() + 60000),
      })
  );

  // Cross-user isolation
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("messages").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("messages").doc("msg_1").delete()
  );
});

test("SessionLog: owner create, revoke active=false, and terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sessions")
      .doc("sess_1")
      .set({
        userId: ALICE_UID,
        username: "Alice",
        deviceName: "Google Pixel 9",
        osVersion: "Android 15 (API 35)",
        ipAddress: "192.168.1.42",
        locationEstimate: "Google Sign-In • Cloud Firestore",
        active: true,
        sessionToken: "sess_abcdef123456",
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Mirror AppRepository.observeSessions query
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sessions")
      .orderBy("createdAt", "desc")
      .get()
  );

  // Revoke session (active: true -> false)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("sessions").doc("sess_1").update({
      active: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: cannot re-activate a revoked session
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("sessions").doc("sess_1").update({
      active: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user isolation
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("sessions").get()
  );
});
