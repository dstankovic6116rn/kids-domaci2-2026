package servent.handler;

import app.Ad;
import app.AppConfig;
import servent.message.AdFetchMessage;
import servent.message.AdFetchReplyMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

/**
 * Handles an AD_FETCH message on the owner node.
 *
 * A searching node sent this after receiving a SEARCH_LOOKUP_REPLY — it wants
 * the live details of a specific ad. We look the ad up in myAds and reply
 * directly to originPort with an AdFetchReplyMessage.
 *
 * ad may be null if the item was removed between index lookup and this fetch
 * (race condition). The null case is passed on to the reply so the receiver
 * can log an appropriate message.
 */
public class AdFetchHandler implements MessageHandler {

	private final Message clientMessage;

	public AdFetchHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.AD_FETCH) {
			AppConfig.timestampedErrorPrint("AdFetchHandler got wrong type");
			return;
		}
		AdFetchMessage msg = (AdFetchMessage) clientMessage;

		// Look up the master ad by itemId (null if we no longer have it).
		Ad ad = AppConfig.chordState.getMyAd(msg.getItemId());

		// Reply directly to whoever asked — includes the ad (or null).
		AdFetchReplyMessage reply = new AdFetchReplyMessage(
				AppConfig.myServentInfo.getListenerPort(), msg.getOriginPort(),
				ad, msg.getItemId());
		MessageUtil.sendMessage(reply);
	}
}
