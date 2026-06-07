package cli.command;

import app.Ad;
import app.AppConfig;
import app.ChordState;
import app.NameIndexEntry;
import app.ServentInfo;
import servent.message.ListItemBackupMessage;
import servent.message.NameIndexStoreMessage;
import servent.message.util.MessageUtil;

/**
 * list_item [item_id] [name] [quantity]
 * 
 * Create an Ad and store it in myAds on this node.
 * Send a backup copy to the node responsible for hash(itemId) in Chord.
 *
 * Send a name index entry to the node responsible for hash(productName). Makes
 * the product findable by "search name".
 *
 * Print [MARKET-LIST] to confirm the listing.
 */
public class ListItemCommand implements CLICommand {

	@Override
	public String commandName() {
		return "list_item";
	}

	@Override
	public void execute(String args) {
		if (args == null) {
			AppConfig.timestampedErrorPrint("Usage: list_item [item_id] [name] [quantity]");
			return;
		}
		String[] parts = args.split(" ");
		if (parts.length != 3) {
			AppConfig.timestampedErrorPrint("Usage: list_item [item_id] [name] [quantity]");
			return;
		}

		int itemId;
		int qty;
		try {
			itemId = Integer.parseInt(parts[0]);
			qty = Integer.parseInt(parts[2]);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("item_id and quantity must be integers");
			return;
		}
		String name = parts[1];

		if (qty <= 0) {
			AppConfig.timestampedErrorPrint("quantity must be positive");
			return;
		}

		// local duplicate check
		if (AppConfig.chordState.knowsItemId(itemId)) {
			AppConfig.timestampedErrorPrint("item_id " + itemId + " already known locally");
			return;
		}

		int myId = AppConfig.myServentInfo.getChordId();
		int myPort = AppConfig.myServentInfo.getListenerPort();
		Ad ad = new Ad(itemId, name, qty, myId, myPort);

		// store master copy locally.
		AppConfig.chordState.registerMyAd(ad);

		// initialise the Suzuki-Kasami token for this item
		AppConfig.chordState.initOwnerToken(itemId);

		// notify all subscribers
		AppConfig.chordState.notifySubscribers(ad);

		// route backup through the network
		int itemKey = ChordState.keyHash(itemId);
		ServentInfo backupNext = AppConfig.chordState.getNextNodeForKey(itemKey);
		MessageUtil.sendMessage(new ListItemBackupMessage(myPort, backupNext.getListenerPort(), ad));

		// route name-index entry through the network.
		int nameKey = ChordState.keyHash(name.hashCode());
		NameIndexEntry entry = new NameIndexEntry(itemId, myId, myPort, name);
		ServentInfo indexNext = AppConfig.chordState.getNextNodeForKey(nameKey);
		MessageUtil.sendMessage(new NameIndexStoreMessage(myPort, indexNext.getListenerPort(), entry, nameKey));

		// confirm the listing to the user.
		AppConfig.timestampedStandardPrint("[MARKET-LIST] item_id:" + itemId
				+ " name:\"" + name + "\" qty:" + qty);
	}
}
