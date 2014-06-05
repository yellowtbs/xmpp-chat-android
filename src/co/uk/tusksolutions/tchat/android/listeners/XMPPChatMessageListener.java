package co.uk.tusksolutions.tchat.android.listeners;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import co.uk.tusksolutions.tchat.android.TChatApplication;
import co.uk.tusksolutions.tchat.android.TChatApplication.CHAT_STATUS_ENUM;
import co.uk.tusksolutions.tchat.android.constants.Constants;

public class XMPPChatMessageListener implements PacketListener {

	private static final String TAG = "XMPPChatMessageListener";
	Context mContext = TChatApplication.getContext();

	@Override
	public void processPacket(Packet packet) {

		Message message = (Message) packet;
		if (message.getType() == Message.Type.chat) {
			if (message.getBody() == null) {
				Log.i(TAG, "Composing...: ");
			} else if (message.getBody().length() == 0) {
				Log.i(TAG, "Stopped composing...: ");
			} else if (message.getBody().length() > 0) {
				if (TChatApplication.getChatActivityStatus() == CHAT_STATUS_ENUM.VISIBLE) {
					sendBundleToWakeLock(message);

				} else if (TChatApplication.getChatActivityStatus() == CHAT_STATUS_ENUM.NOT_VISIBLE) {
					sendBundleToWakeLock(message);

				} else {
					Log.i(TAG, "Unknown state to display message received!");
				}
			}
		}
	}

	private void sendBundleToWakeLock(Message message) {

		// Prepare message bundle.

		Bundle b = new Bundle();
		b.putString("fromName", StringUtils.parseName(StringUtils
				.parseBareAddress(message.getFrom())));
		b.putString("message", message.getBody());

		Intent intent = new Intent();
		intent.putExtra("chatMessageBundle", b);
		intent.setAction(Constants.CHAT_MESSAGE_RECEIVED);

		// Send a WakeLock Broadcast to XMPPNotificationManager
		mContext.sendBroadcast(intent);
	}

}
