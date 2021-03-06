package co.uk.tusksolutions.tchat.android.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.packet.ChatStateExtension;

import android.util.Log;
import co.uk.tusksolutions.tchat.android.TChatApplication;
import co.uk.tusksolutions.tchat.android.constants.Constants;
import co.uk.tusksolutions.tchat.android.models.ChatMessagesModel;

public class XMPPChatMessageManager {

	private static ChatMessagesModel mChatMessageModel;
	private static ChatStateExtension cm;

	public static void sendMessage(final String to, String buddyName,
			final String message, int isGroupMessage, String messageType, String mid) {
		if (mChatMessageModel == null) {
			mChatMessageModel = new ChatMessagesModel();
		}
		Message msg,msg1;
		
		
			msg = new Message(to, Message.Type.chat);
			msg1 = new Message(TChatApplication.getCurrentJid(), Message.Type.chat);
			
		

		msg.setBody(message);
		msg.setPacketID(mid);
		msg1.setBody(to+"|s|"+message);
		msg1.setPacketID(mid);
		/*
		DeliveryReceiptManager
		.addDeliveryReceiptRequest(msg);*/
		if (TChatApplication.connection != null) {
			try {
				mChatMessageModel.saveMessageToDB(to,
						TChatApplication.getCurrentJid(),
						Constants.XMPP_RESOURCE, buddyName, message,
						isGroupMessage, messageType,
						System.currentTimeMillis(), 1, mid);
				DeliveryReceiptManager.addDeliveryReceiptRequest(msg);
				TChatApplication.connection.sendPacket(msg);
				TChatApplication.connection.sendPacket(msg1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// online offline message
			mChatMessageModel.saveMessageToDB(to,
					TChatApplication.getCurrentJid(),
					Constants.XMPP_RESOURCE, buddyName, message,
					isGroupMessage, messageType, System.currentTimeMillis(), 1, mid);
		}
	}

	public static void sendComposing(String to, String packetId) {

		cm = new ChatStateExtension(ChatState.composing);
		Message msg = new Message(to, Message.Type.chat);
		msg.addExtension(cm);

		try {
			if (TChatApplication.connection != null) {
				TChatApplication.connection.sendPacket(msg);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sendPaused(String to, String packetId) {
		cm = new ChatStateExtension(ChatState.paused);
		Message msg = new Message(to, Message.Type.chat);
		msg.addExtension(cm);

		try {
			if (TChatApplication.connection != null) {
				TChatApplication.connection.sendPacket(msg);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}