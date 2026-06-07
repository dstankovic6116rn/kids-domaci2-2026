package servent.message;

import java.util.List;

import app.NameIndexEntry;

/**
 * Reply to a SearchLookupMessage. Sent directly to the node that searched.
 * The receiver then contacts each entrys owner
 * directly with AdFetchMessage to retrieve the live ad details and qty.
 */
public class SearchLookupReplyMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// Search term
	private final String name;

	// All NameIndexEntries whose name matches
	private final List<NameIndexEntry> entries;

	public SearchLookupReplyMessage(int senderPort, int receiverPort, String name, List<NameIndexEntry> entries) {
		super(MessageType.SEARCH_LOOKUP_REPLY, senderPort, receiverPort);
		this.name = name;
		this.entries = entries;
	}

	public String getName() {
		return name;
	}

	public List<NameIndexEntry> getEntries() {
		return entries;
	}
}
