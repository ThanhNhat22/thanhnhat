const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendMessageNotification = functions.database
    .ref("/messages/{conversationId}/{messageId}")
    .onCreate(async (snapshot, context) => {
      const message = snapshot.val();

      const receiverId = message.receiverId;

      if (!receiverId) {
        return null;
      }

      const userSnapshot = await admin.database()
          .ref(`/users/${receiverId}/fcmToken`)
          .once("value");

      const token = userSnapshot.val();

      if (!token) {
        return null;
      }

      const payload = {
        notification: {
          title: message.senderName || "Tin nhắn mới",
          body: message.text || "Bạn có tin nhắn mới",
        },
        token: token,
      };

      return admin.messaging().send(payload);
    });
