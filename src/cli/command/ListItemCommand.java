package cli.command;

import app.Ad;
import app.AppConfig;
import app.ChordState;
import app.NameIndexEntry;
import app.ServentInfo;
import servent.message.ListItemBackupMessage;
import servent.message.ListItemIndexMessage;
import servent.message.util.MessageUtil;

/**
 * CLI command: list_item [item_id] [name] [quantity]
 *
 * Creates a new product advertisement in the distributed market.
 * The node that runs this command becomes the OWNER of the ad.
 *
 * What happens in order:
 *
 *  1. Validate arguments and reject obvious duplicates (local check only).
 *
 *  2. Create an Ad and store it in myAds on this node (master copy).
 *
 *  3. Send a BACKUP copy to the node responsible for hash(itemId) in Chord.
 *     If that node happens to be us, store it locally immediately.
 *     Purpose: fault tolerance — if this node leaves, the backup survives.
 *
 *  4. Send a NAME INDEX entry to the node responsible for hash(productName).
 *     If that node is us, append it locally.
 *     Purpose: makes the product findable by "search name".
 *
 *  5. Print [MARKET-LIST] to confirm the listing.
 *     Note: the print happens before the backup/index messages arrive at
 *     their destinations (no ACKs in Phase 1), so a search issued very
 *     quickly after list_item from a different node may not see it yet.
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

		// Best-effort local duplicate check — cannot detect duplicates on other nodes.
		if (AppConfig.chordState.knowsItemId(itemId)) {
			AppConfig.timestampedErrorPrint("item_id " + itemId + " already known locally");
			return;
		}

		int myId = AppConfig.myServentInfo.getChordId();
		int myPort = AppConfig.myServentInfo.getListenerPort();
		Ad ad = new Ad(itemId, name, qty, myId, myPort);

		// Step 2: store master copy locally.
		AppConfig.chordState.registerMyAd(ad);

		// Step 2a: initialise the Suzuki-Kasami token for this item right here
		// at the owner.  The owner is the natural initial token holder; other
		// nodes will discover the token's existence implicitly when they
		// broadcast MUTEX_REQUEST and the owner forwards the token to them.
		AppConfig.chordState.initOwnerToken(itemId);

		// Step 2b: notify all subscribers that this node has a new listing.
		// Runs before [MARKET-LIST] print; sends are non-blocking (each spawns a thread).
		AppConfig.chordState.notifySubscribers(ad);

		// Step 3: route backup to the node responsible for hash(itemId).
		int itemKey = ChordState.keyHash(itemId);
		if (AppConfig.chordState.isKeyMine(itemKey)) {
			AppConfig.chordState.storeBackupAd(ad);
		} else {
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(itemKey);
			MessageUtil.sendMessage(new ListItemBackupMessage(myPort, next.getListenerPort(), ad));
		}

		// Step 4: route name-index entry to the node responsible for hash(name).
		int nameKey = ChordState.keyHash(name.hashCode());
		NameIndexEntry entry = new NameIndexEntry(itemId, myId, myPort, name);
		if (AppConfig.chordState.isKeyMine(nameKey)) {
			AppConfig.chordState.appendNameIndex(nameKey, entry);
		} else {
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(nameKey);
			MessageUtil.sendMessage(new ListItemIndexMessage(myPort, next.getListenerPort(), entry, nameKey));
		}

		// Step 5: confirm the listing to the user.
		AppConfig.timestampedStandardPrint("[MARKET-LIST] item_id:" + itemId
				+ " name:\"" + name + "\" qty:" + qty);
	}
}
