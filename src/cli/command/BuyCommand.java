package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ItemMutexState;
import app.ServentInfo;
import servent.message.BuyOwnerLookupMessage;
import servent.message.util.MessageUtil;

/**
 * buy [item_id] [quantity]
 *
 * Distributed mutex-protected stock decrement. The flow:
 * if this node owns the item -> skip lookup.
 * elif this node holds the backup -> read ownerPort from backupAd.
 * else -> route BUY_OWNER_LOOKUP via Chord BuyOwnerReplyHandler
 * 
 * call requestBuyMutex -> prints [MUTEX-REQUEST] and broadcasts the Suzuki-
 * Kasami REQUEST
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

		// Always discover the owner via the Chord-routed lookup.
		ItemMutexState state = AppConfig.chordState.getOrCreateMutexState(itemId);
		synchronized (state) {
			state.myPendingQty = qty;
		}
		int itemKey = ChordState.keyHash(itemId);
		ServentInfo next = AppConfig.chordState.getNextNodeForKey(itemKey);
		MessageUtil.sendMessage(new BuyOwnerLookupMessage(myPort, next.getListenerPort(), itemId, myPort));
	}
}
