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
import co.uk.tusksolutions.tchat.android.models.ChatMessagesModel;
import co.uk.tusksolutions.tchat.android.xmpp.notifications.XMPPNotificationManager;

public class XMPPGroupChatMessageListener implements PacketListener {

	private static final String TAG = "XMPPGroupChatMessageListener";
	Context mContext = TChatApplication.getContext();
	public static final String EXTRA_CHAT_STATE = "chatState";
	public static final String ACTION_XMPP_CHAT_STATE_CHANGED = "XMPP_CHAT_STATE_CHANGED";
	public static final String EXTRA_CHAT_BUDDY_NAME = "roomJid";

	/*
	 * @Override public void processPacket(Packet packet) { Message message =
	 * (Message) packet;
	 * 
	 * if (message.getBody() != null) { String fromName =
	 * StringUtils.parseBareAddress(message.getFrom()); String toName =
	 * StringUtils.parseBareAddress(message.getTo()); Log.i(TAG,
	 * "New group message fromUser " + fromName + ", To: " + toName + " : " +
	 * message.getBody()); }
	 * 
	 * }
	 */

	@Override
	public void processPacket(Packet packet) {

		Message message = (Message) packet;
		if (message.getType() == Message.Type.groupchat) {
			if (message.getBody().length() > 0) {

				String roomJid = StringUtils
						.parseBareAddress(message.getFrom());
				String roomName = TChatApplication.getGroupsModel()
						.getGroupName(StringUtils.parseName(message.getFrom()));
				String senderJid = StringUtils.parseResource(message.getFrom())
						+ "@" + Constants.CURRENT_SERVER;
				String senderName = TChatApplication.getRosterModel()
						.getBuddyName(senderJid);

				Log.i(TAG, "New group message From Room Jid: " + roomJid
						+ ", Room Name: " + roomName + ", and sender: "
						+ senderJid + ", and Sender Name: " + senderName
						+ " Message: " + message.getBody());
				
				if (TChatApplication.getChatActivityStatus() == CHAT_STATUS_ENUM.VISIBLE
						&& TChatApplication.chatSessionBuddy
								.equalsIgnoreCase(roomJid)) {

					// 1. Visible and chatting with buddy

					// Save to DB
					saveMessageToDb(packet, message);

				} else if (TChatApplication.getChatActivityStatus() == CHAT_STATUS_ENUM.VISIBLE
						&& !TChatApplication.chatSessionBuddy
								.equalsIgnoreCase(roomJid)) {

					// 2. Visible and not chatting with buddy
					/*
					 * Prepare message bundle.
					 */
					sendNotification(packet, message);

				} else if (TChatApplication.getChatActivityStatus() == CHAT_STATUS_ENUM.NOT_VISIBLE) {

					// 3. Not Visible and not chatting
					/*
					 * Prepare message bundle.
					 */
					sendNotification(packet, message);

				}
			}
		}
	}

	private void sendNotification(Packet packet, Message message) {
		
		String roomJid = StringUtils
				.parseBareAddress(message.getFrom());
		String roomName = TChatApplication.getGroupsModel()
				.getGroupName(StringUtils.parseName(message.getFrom()));
		String senderJid = StringUtils.parseResource(message.getFrom())
				+ "@" + Constants.CURRENT_SERVER;
		String senderName = TChatApplication.getRosterModel()
				.getBuddyName(senderJid);
		
		Bundle b = new Bundle();
		b.putString("roomJid", roomJid);
		b.putString("roomName", roomName);
		b.putString("senderJid", senderJid);
		b.putString("senderName", senderName);
		b.putString("message", message.getBody());

		Intent intent = new Intent();
		intent.putExtra("groupChatFromRoomBundle", b);

		// Send TO_USER notification manager
		new XMPPNotificationManager().sendGroupChatNotification(intent);

		// Save to DB
		saveMessageToDb(packet, message);
	}

	private void saveMessageToDb(Packet packet, Message message) {
		/*
		 * Insert received message to db
		 */
		ChatMessagesModel mChatMessageModel = new ChatMessagesModel();
		mChatMessageModel.saveMessageToDB(TChatApplication.getCurrentJid(),
				StringUtils.parseBareAddress(packet.getFrom()),
				StringUtils.parseName(packet.getFrom()), message.getBody(),
				System.currentTimeMillis(), 1);
	}

}
