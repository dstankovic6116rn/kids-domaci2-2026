package app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Per-item Suzuki-Kasami state held by every node.
 *
 * Every node keeps one ItemMutexState per known item.  The state has two
 * roles depending on whether the node currently holds the token:
 *
 *   1. Always: tracks RN[port] = highest request sequence number this node
 *      has heard from each other node.  Updated whenever a MUTEX_REQUEST
 *      arrives.
 *
 *   2. While holding token: stores the live token (LN map + queue Q).
 *      When entering the CS, the token data is moved out of the field
 *      `heldToken` into the working LN/Q used during release.
 *
 * The "myRequestPending" / "myPendingQty" / "knownOwnerPort" / "inCS" fields
 * are buy-flow bookkeeping for THIS node when it is the requesting buyer.
 *
 * All methods on this class are external — callers are expected to
 * synchronize on the instance for the duration of any logical operation
 * (e.g. handling one MUTEX_REQUEST or one token receipt).
 */
public class ItemMutexState {

	// Highest request seq seen from each node, keyed by their port.
	public final Map<Integer, Integer> RN = new HashMap<>();

	// Fairness: arrival-ordered list of requesters whose latest REQUEST has
	// not yet been forwarded the token.  Used by exitCS to populate the
	// token's Q in FIFO order (vs. RN.entrySet() which is HashMap-ordered).
	// Entries leave this list when we forward the token to that requester.
	public final LinkedList<Integer> pendingRequesters = new LinkedList<>();

	// My own request seq counter (only meaningful on the requesting node).
	public int mySeq = 0;

	// Token presence: null when we don't have it, non-null when we do.
	public MutexToken heldToken = null;

	// Buy-flow bookkeeping (only meaningful when myRequestPending == true).
	public boolean myRequestPending = false;
	public int myPendingQty = 0;
	public int knownOwnerPort = -1; // -1 until owner lookup completes / not needed for self-buy
	public boolean inCS = false;
}
