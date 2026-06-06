package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ItemMutexState;
import app.ServentInfo;
import servent.message.BuyOwnerLookupMessage;
import servent.message.util.MessageUtil;

/**
 * CLI command: buy [item_id] [quantity]
 *
 * Distributed mutex-protected stock decrement.  The flow:
 *
 *   1. Validate args.
 *   2. Resolve the owner port:
 *        - if THIS node owns the item (myAds) -> skip lookup.
 *        - elif THIS node holds the backup    -> read ownerPort from backupAd.
 *        - else                               -> route BUY_OWNER_LOOKUP via
 *                                                Chord; BuyOwnerReplyHandler
 *                                                resumes once the reply lands.
 *   3. With owner port known, call chordState.requestBuyMutex(itemId, qty,
 *      ownerPort) which prints [MUTEX-REQUEST] and broadcasts the Suzuki-
 *      Kasami REQUEST (or enters the CS immediately if we already hold the
 *      token).
 *
 * All later prints — [MUTEX-ACQUIRED], [MARKET-BUY-*], [MUTEX-RELEASED] —
 * are emitted from ChordState as the protocol progresses.
 */
public class BuyCommand implements CLICommand {

	@Override
	public String commandName() {
		return "buy";
	}

	@Override
	public void execute(String args) {
		if (args == null) {
			AppConfig.timestampedErrorPrint("Usage: buy [item_id] [quantity]");
			return;
		}
		String[] parts = args.split(" ");
		if (parts.length != 2) {
			AppConfig.timestampedErrorPrint("Usage: buy [item_id] [quantity]");
			return;
		}
		int itemId;
		int qty;
		try {
			itemId = Integer.parseInt(parts[0]);
			qty = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("item_id and quantity must be integers");
			return;
		}
		if (qty <= 0) {
			AppConfig.timestampedErrorPrint("quantity must be positive");
			return;
		}

		int myPort = AppConfig.myServentInfo.getListenerPort();

		// Always discover the owner via the Chord-routed lookup, even when
		// we ourselves are the owner or hold the backup.  The message
		// loops back via TCP and BuyOwnerLookupHandler answers normally.
		// No local shortcuts — guarantees the operation goes through the
		// network as required.
		ItemMutexState state = AppConfig.chordState.getOrCreateMutexState(itemId);
		synchronized (state) {
			state.myPendingQty = qty;
		}
		int itemKey = ChordState.keyHash(itemId);
		ServentInfo next = AppConfig.chordState.getNextNodeForKey(itemKey);
		MessageUtil.sendMessage(new BuyOwnerLookupMessage(myPort, next.getListenerPort(), itemId, myPort));
	}
}
