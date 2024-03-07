const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.androidPushNotification = functions.firestore.document("announcements/{anmtId}").onCreate(
    (snapshot, context) => {
        admin.messaging().sendToTopic(
            snapshot.data().eventId,  // Topic to send to (eventId, attendees that check-in to events will already be subscribed to this topic)
            {
                notification: {
                    // anmtId: snapshot.data().anmtId,
                    title: snapshot.data().title,
                    body: snapshot.data().body
                }
            }
        );
        // admin.firestore().collection('users').
    }
);