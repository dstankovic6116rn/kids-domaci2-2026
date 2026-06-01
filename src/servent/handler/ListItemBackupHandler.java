package servent.handler;

import app.Ad;
import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.ListItemBackupMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

/**
 * Handles a LIST_ITEM_BACKUP message.
 *
 * Goal: store a backup copy of the ad on the node whose Chord key range
 * contains hash(itemId).
 *
 * Decision logic:
 *   - If THIS node owns the key hash(itemId) -> store the backup locally.
 *   - Otherwise -> forward the message one hop closer to the owner using
 *     getNextNodeForKey, which consults the finger table.
 *
 * This is a standard Chord DHT "put" pattern: keep forwarding until you
 * reach the responsible node, then store.
 */
public class ListItemBackupHandler implements MessageHandler {

	private final Message clientMessage;

	public ListItemBackupHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.LIST_ITEM_BACKUP) {
			AppConfig.timestampedErrorPrint("ListItemBackupHandler got wrong type");
			return;
		}
		ListItemBackupMessage msg = (ListItemBackupMessage) clientMessage;
		Ad ad = msg.getAd();

		// Map itemId into the Chord keyspace [0, CHORD_SIZE).
		int itemKey = ChordState.keyHash(ad.getItemId());

		if (AppConfig.chordState.isKeyMine(itemKey)) {
			// This node is responsible for itemKey — store the backup here.
			AppConfig.chordState.storeBackupAd(ad);
		} else {
			// Not our key — forward one hop closer to the owner.
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(itemKey);
			ListItemBackupMessage fwd = new ListItemBackupMessage(
					AppConfig.myServentInfo.getListenerPort(), next.getListenerPort(), ad);
			MessageUtil.sendMessage(fwd);
		}
	}
}
