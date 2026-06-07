package app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Suzuki-Kasami state held by every node.
 * Every node keeps one ItemMutexState per item.
 */
public class ItemMutexState {

	// Highest request sequence keyed by their port.
	public final Map<Integer, Integer> RN = new HashMap<>();

	// Fairness: arrival-ordered list
	public final LinkedList<Integer> pendingRequesters = new LinkedList<>();

	// My own request seq counter
	public int mySeq = 0;

	// null when we don't have the token, non-null when we do.
	public MutexToken heldToken = null;

	public boolean myRequestPending = false;
	public int myPendingQty = 0;
	public int knownOwnerPort = -1; // -1 until owner lookup completes
	public boolean inCS = false;
}
