package servent.message;

import java.util.List;

import app.NameIndexEntry;

/**
 * Reply to a SearchLookupMessage.  Sent directly (in one hop) from the
 * name-index node back to the original searching node (originPort).
 *
 * Contains all NameIndexEntries whose name matches the search term exactly.
 * The receiver (SearchLookupReplyHandler) then contacts each entry's owner
 * directly with an AdFetchMessage to retrieve the live ad details and qty.
 *
 * An empty entries list means no matching ads exist in the network.
 */
public class SearchLookupReplyMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// The search term that was originally queried — kept for context/logging.
	private final String name;

	// All NameIndexEntries whose name matches exactly (pre-filtered by the sender).
	private final List<NameIndexEntry> entries;

	public SearchLookupReplyMessage(int senderPort, int receiverPort, String name, List<NameIndexEntry> entries) {
		super(MessageType.SEARCH_LOOKUP_REPLY, senderPort, receiverPort);
		this.name = name;
		this.entries = entries;
	}

	public String getName() { return name; }
	public List<NameIndexEntry> getEntries() { return entries; }
}
