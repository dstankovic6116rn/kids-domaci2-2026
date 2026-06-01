package cli.command;

import java.util.ArrayList;
import java.util.List;

import app.Ad;
import app.AppConfig;
import app.ChordState;
import app.NameIndexEntry;
import app.ServentInfo;
import servent.message.AdFetchMessage;
import servent.message.SearchLookupMessage;
import servent.message.util.MessageUtil;

/**
 * CLI command: search [name]
 *
 * Searches the distributed network for all product listings with the given name.
 * Prints one [MARKET-SEARCH-RESULT] line per matching ad found.
 *
 * How the search works:
 *
 *  1. Compute nameKey = hash(name) — same formula used when an ad was indexed.
 *
 *  2. If THIS node owns nameKey:
 *       - Read matching NameIndexEntries from the local name index.
 *       - For each entry:
 *           * If the owner is us  -> look up the ad in myAds, print immediately.
 *           * If the owner is remote -> send AdFetchMessage to that owner;
 *             AdFetchReplyHandler will print the result when the reply arrives.
 *
 *  3. If THIS node does NOT own nameKey:
 *       - Send a SearchLookupMessage toward the responsible node (Chord routing).
 *       - SearchLookupHandler on that node will return a SearchLookupReplyMessage
 *         back to us, then SearchLookupReplyHandler continues as in step 2.
 *
 * Results arrive asynchronously (non-FIFO) so [MARKET-SEARCH-RESULT] lines
 * may appear interleaved with other log output — that is by design.
 */
public class SearchCommand implements CLICommand {

	@Override
	public String commandName() {
		return "search";
	}

	@Override
	public void execute(String args) {
		if (args == null || args.isEmpty()) {
			AppConfig.timestampedErrorPrint("Usage: search [name]");
			return;
		}
		String name = args.trim();

		// Compute the Chord key for the product name — same as during list_item.
		int nameKey = ChordState.keyHash(name.hashCode());
		int myPort = AppConfig.myServentInfo.getListenerPort();

		if (AppConfig.chordState.isKeyMine(nameKey)) {
			// The name index for this key lives on this node.
			List<NameIndexEntry> all = AppConfig.chordState.getNameIndex(nameKey);
			List<NameIndexEntry> matches = new ArrayList<>();
			for (NameIndexEntry e : all) {
				// Filter by exact name to exclude hash-collision entries.
				if (e.getName().equals(name)) {
					matches.add(e);
				}
			}
			for (NameIndexEntry entry : matches) {
				if (entry.getOwnerPort() == myPort) {
					// We own this ad — print directly without a network round-trip.
					Ad ad = AppConfig.chordState.getMyAd(entry.getItemId());
					if (ad != null) {
						AppConfig.timestampedStandardPrint("[MARKET-SEARCH-RESULT] item_id:"
								+ ad.getItemId() + " name:\"" + ad.getName() + "\" qty:"
								+ ad.getQty() + " owner_id:" + ad.getOwnerId());
					}
				} else {
					// Remote owner — ask for the live ad; result printed by AdFetchReplyHandler.
					MessageUtil.sendMessage(new AdFetchMessage(myPort, entry.getOwnerPort(),
							entry.getItemId(), myPort));
				}
			}
		} else {
			// Name index is on another node — route the lookup via Chord.
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(nameKey);
			MessageUtil.sendMessage(new SearchLookupMessage(myPort, next.getListenerPort(),
					name, nameKey, myPort));
		}
	}
}
