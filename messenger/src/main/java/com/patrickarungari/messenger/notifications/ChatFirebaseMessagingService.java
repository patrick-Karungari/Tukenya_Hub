package com.patrickarungari.messenger.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.users.models.ChatUser;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.messages.activities.MessageListActivity;
import com.patrickarungari.messenger.utils.DebugConstants;
import com.patrickarungari.messenger.utils.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.patrickarungari.messenger.ui.ChatUI.BUNDLE_CHANNEL_TYPE;

/**
 * Created by stefanodp91 on 06/02/18.
 */

public class ChatFirebaseMessagingService extends FirebaseMessagingService {

    // There are two types of messages data messages and notification messages. Data messages are handled
    // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
    // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
    // is in the foreground. When the app is in the background an automatically generated notification is displayed.
    // When the user taps on the notification they are returned to the app. Messages containing both notification
    // and data payloads are treated as notification messages. The Firebase console always sends notification
    // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(DebugConstants.DEBUG_NOTIFICATION, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(DebugConstants.DEBUG_NOTIFICATION, "Message data payload: " + remoteMessage.getData());

            String sender = remoteMessage.getData().get("sender");
            String senderFullName = remoteMessage.getData().get("sender_fullname");
            String channelType = remoteMessage.getData().get("channel_type");
            String text = remoteMessage.getData().get("text");
            String timestamp = remoteMessage.getData().get("timestamp");
            String recipientFullName = remoteMessage.getData().get("recipient_fullname");
            String recipient = remoteMessage.getData().get("recipient");

            String currentOpenConversationId = ChatManager.getInstance()
                    .getConversationsHandler()
                    .getCurrentOpenConversationId();

            if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {

                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(sender)) {
                    sendDirectNotification(sender, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendDirectNotification(sender, senderFullName, text, channelType);
                    }
                }
            } else if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(recipient)) {
                    sendGroupNotification(recipient, recipientFullName, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendGroupNotification(recipient, recipientFullName, senderFullName, text, channelType);
                    }
                }
            } else {
                // default case
                if(StringUtils.isValid(currentOpenConversationId) && !currentOpenConversationId.equals(sender)) {
                    sendDirectNotification(sender, senderFullName, text, channelType);
                } else {
                    if(!StringUtils.isValid(currentOpenConversationId)) {
                        sendDirectNotification(sender, senderFullName, text, channelType);
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(DebugConstants.DEBUG_NOTIFICATION, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            example DIRECT:
//            Message Notification Body: Foreground
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        DatabaseReference tokenNode = FirebaseDatabase.getInstance()
                .getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/instances");
        Map<String, Object> token = new HashMap<>();
        token.put("userToken",s);
        tokenNode.setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    /**
     * Create and show a direct notification containing the received FCM message.
     *
     * @param sender         the id of the message's sender
     * @param senderFullName the display name of the message's sender
     * @param text           the message text
     */
    private void sendDirectNotification(String sender, String senderFullName, String text, String channel) {

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(sender, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channel)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(senderFullName)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo fix
        String channelId = channel;
        String channelName = channel;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        int notificationId = (int) new Date().getTime();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Create and show a group notification containing the received FCM message.
     *
     * @param sender         the id of the message's sender
     * @param senderFullName the display name of the message's sender
     * @param text           the message text
     */
    private void sendGroupNotification(String sender, String senderFullName, String recipientFullName, String text, String channel) {

        String title = recipientFullName + " @" + senderFullName;

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(sender, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channel)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo fix
        String channelId = channel;
        String channelName = channel;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        int notificationId = (int) new Date().getTime();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}