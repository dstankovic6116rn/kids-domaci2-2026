package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import servent.message.AskGetMessage;
import servent.message.BackupQtyUpdateMessage;
import servent.message.BuyExecMessage;
import servent.message.BuyExecReplyMessage;
import servent.message.BuyOwnerLookupMessage;
import servent.message.MarketNotificationMessage;
import servent.message.MutexRequestMessage;
import servent.message.MutexTokenMessage;
import servent.message.PutMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord
 * ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the
 * maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 * <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of
 * <code>successorTable</code></li>
 * <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 * <li><code>predecessorInfo</code> - who is our predecessor.</li>
 * <li><code>valueMap</code> - DHT values stored on this node.</li>
 * <li><code>init()</code> - should be invoked when we get the WELCOME
 * message.</li>
 * <li><code>isCollision(int chordId)</code> - checks if a servent with that
 * Chord ID is already active.</li>
 * <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 * <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then
 * return it, otherwise returns the nearest predecessor for this key from my
 * successor table.</li>
 * <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor
 * table.</li>
 * <li><code>putValue(int key, int value)</code> - stores the value locally or
 * sends it on further in the system.</li>
 * <li><code>getValue(int key)</code> - gets the value locally, or sends a
 * message to get it from somewhere else.</li>
 * </ul>
 * 
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;

	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}

	/**
	 * Maps any integer key (e.g. itemId, or name.hashCode()) into the Chord
	 * keyspace [0, CHORD_SIZE). Unlike chordHash above it is used for
	 * ad/index keys, not for node's Chord ID from its port number.
	 */
	public static int keyHash(int rawKey) {
		return Math.floorMod(rawKey, CHORD_SIZE);
	}

	private int chordLevel; // log_2(CHORD_SIZE)

	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;

	// we DO NOT use this to send messages, but only to construct the successor
	// table
	private List<ServentInfo> allNodeInfo;

	private Map<Integer, Integer> valueMap;

	// Master copies of ads created by THIS node
	private final Map<Integer, Ad> myAds = new HashMap<>();

	// Backup copies of ads whose Chord key (hash(itemId)) belongs to this node
	private final Map<Integer, Ad> backupAds = new HashMap<>();

	// Secondary name index: maps hash(productName) - list of NameIndexEntries.
	// This node stores entries whose nameKey falls in its Chord key range
	private final Map<Integer, List<NameIndexEntry>> nameIndex = new HashMap<>();

	// One-time [SYS-NEIGHBORS] print so it fires exactly once per node lifetime
	private volatile boolean joinAnnounced = false;

	private final Set<ServentInfo> subscribers = Collections.synchronizedSet(new LinkedHashSet<>());

	private final Map<Integer, ItemMutexState> mutexStates = new HashMap<>();

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { // not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}

		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}

		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();
	}

	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send
	 * <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		// set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		this.valueMap = welcomeMsg.getValues();

		// tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");

			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getChordLevel() {
		return chordLevel;
	}

	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}

	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}

	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}

	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}

	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}

		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();

		if (predecessorChordId < myChordId) { // no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { // overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Main chord operation - find the nearest node to hop to to find a specific
	 * key.
	 * We have to take a value that is smaller than required to make sure we don't
	 * overshoot.
	 * We can only be certain we have found the required node when it is our first
	 * next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}

		// normally we start the search from our first successor
		int startInd = 0;

		// if the key is smaller than us, and we are not the owner,
		// then all nodes up to CHORD_SIZE will never be the owner,
		// so we start the search from the first item in our table after CHORD_SIZE
		// we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}

		int previousId = successorTable[startInd].getChordId();

		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}

			int successorId = successorTable[i].getChordId();

			if (successorId >= key) {
				return successorTable[i - 1];
			}
			if (key > previousId && successorId < previousId) { // overflow
				return successorTable[i - 1];
			}
			previousId = successorId;
		}
		// if we have only one node in all slots in the table, we might get here
		// then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		// first node after me has to be successorTable[0]

		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;

		int currentIncrement = 2;

		ServentInfo previousNode = AppConfig.myServentInfo;

		// i is successorTable index
		for (int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			// we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;

			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();

			// this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					// before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						// try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { // node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					// check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						// try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}

	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by
	 * chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do
	 * the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);

		allNodeInfo.sort(new Comparator<ServentInfo>() {

			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}

		});

		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();

		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}

		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size() - 1);
		} else {
			predecessorInfo = newList.get(newList.size() - 1);
		}

		updateSuccessorTable();

		maybeAnnounceNeighbors();
	}

	/**
	 * Called at the end of every addNodes(). Prints the
	 * [SYS-NEIGHBORS] line exactly once.
	 */
	public synchronized void maybeAnnounceNeighbors() {
		if (joinAnnounced) {
			return;
		}
		if (predecessorInfo == null || successorTable[0] == null) {
			return;
		}
		joinAnnounced = true;
		int myId = AppConfig.myServentInfo.getChordId();
		int predId = predecessorInfo.getChordId();
		int succId = successorTable[0].getChordId();
		AppConfig.timestampedStandardPrint("[SYS-NEIGHBORS] my_id:" + myId
				+ " neighbors:" + predId + "," + succId + ";");
	}

	/** Stores the master copy of an ad created by this node. */
	public void registerMyAd(Ad ad) {
		synchronized (myAds) {
			myAds.put(ad.getItemId(), ad);
		}
	}

	/**
	 * Atomic check-and-decrement.
	 * Returns the updated Ad on success, or null if there isn't enough
	 * stock or the ad isn't present.
	 */
	public Ad tryDecrementMyAd(int itemId, int qty) {
		synchronized (myAds) {
			Ad existing = myAds.get(itemId);
			if (existing == null || existing.getQty() < qty) {
				return null;
			}
			Ad updated = new Ad(existing.getItemId(), existing.getName(),
					existing.getQty() - qty, existing.getOwnerId(), existing.getOwnerPort());
			myAds.put(itemId, updated);
			return updated;
		}
	}

	/** Retrieves a master ad by itemId, or null. */
	public Ad getMyAd(int itemId) {
		synchronized (myAds) {
			return myAds.get(itemId);
		}
	}

	/**
	 * Stores a backup copy of someones ad.
	 */
	public void storeBackupAd(Ad ad) {
		synchronized (backupAds) {
			backupAds.put(ad.getItemId(), ad);
		}
	}

	/** Returns a backup ad by itemId, or null. */
	public Ad getBackupAd(int itemId) {
		synchronized (backupAds) {
			return backupAds.get(itemId);
		}
	}

	/**
	 * Appends a NameIndexEntry to the list stored under nameKey.
	 */
	public void appendNameIndex(int nameKey, NameIndexEntry entry) {
		synchronized (nameIndex) {
			nameIndex.computeIfAbsent(nameKey, k -> new ArrayList<>()).add(entry);
		}
	}

	/**
	 * Returns a snapshot of all NameIndexEntries stored under nameKey.
	 */
	public List<NameIndexEntry> getNameIndex(int nameKey) {
		synchronized (nameIndex) {
			List<NameIndexEntry> list = nameIndex.get(nameKey);
			if (list == null) {
				return new ArrayList<>();
			}
			return new ArrayList<>(list);
		}
	}

	/**
	 * Local duplicate check for itemId.
	 */
	public boolean knowsItemId(int itemId) {
		synchronized (myAds) {
			if (myAds.containsKey(itemId))
				return true;
		}
		synchronized (backupAds) {
			if (backupAds.containsKey(itemId))
				return true;
		}
		synchronized (nameIndex) {
			for (List<NameIndexEntry> list : nameIndex.values()) {
				for (NameIndexEntry e : list) {
					if (e.getItemId() == itemId)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Registers a new subscriber for this node's listings.
	 */
	public void addSubscriber(ServentInfo subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Returns a snapshot of current subscribers
	 */
	public Set<ServentInfo> getSubscribers() {
		synchronized (subscribers) {
			return new LinkedHashSet<>(subscribers);
		}
	}

	// Suzuki-Kasami distributed mutex

	/**
	 * Returns the per-item mutex state.
	 */
	public ItemMutexState getOrCreateMutexState(int itemId) {
		synchronized (mutexStates) {
			return mutexStates.computeIfAbsent(itemId, k -> new ItemMutexState());
		}
	}

	/**
	 * Called by ListItemCommand when this node lists a new item.
	 */
	public void initOwnerToken(int itemId) {
		ItemMutexState state = getOrCreateMutexState(itemId);
		synchronized (state) {
			if (state.heldToken == null) {
				state.heldToken = new MutexToken(itemId);
			}
		}
	}

	/**
	 * Returns a snapshot of all other nodes, used by the buyer to broadcast
	 * MUTEX_REQUEST to the group.
	 */
	public List<ServentInfo> getAllOtherNodes() {
		synchronized (allNodeInfo) {
			return new ArrayList<>(allNodeInfo);
		}
	}

	/**
	 * Entry point from BuyCommand.
	 *
	 * Prints [MUTEX-REQUEST] item_id:X and either:
	 * if we hold the token enters critical section
	 * else broadcasts MUTEX_REQUEST to other nodes
	 */
	public void requestBuyMutex(int itemId, int qty, int ownerPort) {
		ItemMutexState state = getOrCreateMutexState(itemId);
		int myPort = AppConfig.myServentInfo.getListenerPort();
		boolean enterNow;
		int seqToBroadcast = 0;
		synchronized (state) {
			state.myPendingQty = qty;
			state.knownOwnerPort = ownerPort;
			state.myRequestPending = true;
			state.mySeq++;
			state.RN.put(myPort, state.mySeq);
			seqToBroadcast = state.mySeq;
			enterNow = (state.heldToken != null);
		}
		AppConfig.timestampedStandardPrint("[MUTEX-REQUEST] item_id:" + itemId);
		// Always broadcast request to every other node even if we hold the
		// token. Without this, our request would be invisible to the network.
		for (ServentInfo n : getAllOtherNodes()) {
			MessageUtil.sendMessage(new MutexRequestMessage(
					myPort, n.getListenerPort(), itemId, myPort, seqToBroadcast));
		}
		// If we already hold the token, no need to wait enter CS.
		if (enterNow) {
			enterCS(itemId);
		}
	}

	/**
	 * Suzuki-Kasami request handler
	 */
	public void handleMutexRequest(int itemId, int fromPort, int seq) {
		ItemMutexState state = getOrCreateMutexState(itemId);
		MutexToken tokenToSend = null;
		synchronized (state) {
			int known = state.RN.getOrDefault(fromPort, 0);
			if (seq > known) {
				state.RN.put(fromPort, seq);
				// Fairness - record order so exit CS can clean the queue FIFO
				state.pendingRequesters.remove(Integer.valueOf(fromPort));
				state.pendingRequesters.addLast(fromPort);
			}
			if (state.heldToken != null && !state.inCS && !state.myRequestPending) {
				int lnFrom = state.heldToken.getLN().getOrDefault(fromPort, 0);
				int rnFrom = state.RN.get(fromPort);
				if (rnFrom == lnFrom + 1) {
					tokenToSend = state.heldToken;
					state.heldToken = null;
					state.pendingRequesters.remove(Integer.valueOf(fromPort));
				}
			}
		}
		if (tokenToSend != null) {
			MessageUtil.sendMessage(new MutexTokenMessage(
					AppConfig.myServentInfo.getListenerPort(), fromPort, tokenToSend));
		}
	}

	/**
	 * Token received hanlder, store it, and if we still have a pending request,
	 * enter the CS
	 */
	public void handleTokenReceived(int itemId, MutexToken token) {
		ItemMutexState state = getOrCreateMutexState(itemId);
		boolean shouldEnter;
		synchronized (state) {
			state.heldToken = token;
			shouldEnter = state.myRequestPending && !state.inCS;
		}
		if (shouldEnter) {
			enterCS(itemId);
		}
	}

	/**
	 * Enters the critical section for `itemId`. Prints [MUTEX-ACQUIRED]
	 * then either executes the buy locally (if owner == self) or sends
	 * BUY_EXEC to the owner.
	 */
	private void enterCS(int itemId) {
		ItemMutexState state = getOrCreateMutexState(itemId);
		int myPort = AppConfig.myServentInfo.getListenerPort();
		int ownerPort;
		int qty;
		synchronized (state) {
			state.inCS = true;
			ownerPort = state.knownOwnerPort;
			qty = state.myPendingQty;
		}
		AppConfig.timestampedStandardPrint("[MUTEX-ACQUIRED]");
		// Always send BUY_EXEC over the network, even when the owner is self
		MessageUtil.sendMessage(new BuyExecMessage(myPort, ownerPort, itemId, qty, myPort));
	}

	/**
	 * Called by BuyExecReplyHandler when the owner has responded.
	 */
	public void handleBuyExecReply(int itemId, boolean success, int qtyBought, int remainingQty) {
		if (success) {
			AppConfig.timestampedStandardPrint("[MARKET-BUY-SUCCESS] item_id:" + itemId
					+ " qty_bought:" + qtyBought + " remaining_qty:" + remainingQty);
		} else {
			AppConfig.timestampedStandardPrint("[MARKET-BUY-FAIL] item_id:" + itemId
					+ " reason:OUT_OF_STOCK");
		}
		exitCS(itemId);
	}

	/**
	 * Suzuki-Kasami exit
	 */
	private void exitCS(int itemId) {
		ItemMutexState state = getOrCreateMutexState(itemId);
		int myPort = AppConfig.myServentInfo.getListenerPort();
		MutexToken tokenToForward = null;
		int forwardTo = -1;
		synchronized (state) {
			if (state.heldToken == null) {
				// Shouldn't happen — we were in CS, so we held the token.
				AppConfig.timestampedErrorPrint("exitCS called without holding token for item " + itemId);
				state.inCS = false;
				state.myRequestPending = false;
				state.myPendingQty = 0;
				AppConfig.timestampedStandardPrint("[MUTEX-RELEASED]");
				return;
			}
			state.heldToken.getLN().put(myPort, state.mySeq);
			// Fairness, iterate pendingRequesters in arrival order (FIFO).
			Iterator<Integer> it = state.pendingRequesters.iterator();
			while (it.hasNext()) {
				int port = it.next();
				if (port == myPort) {
					it.remove();
					continue;
				}
				int rn = state.RN.getOrDefault(port, 0);
				int ln = state.heldToken.getLN().getOrDefault(port, 0);
				if (rn == ln + 1) {
					if (!state.heldToken.getQ().contains(port)) {
						state.heldToken.getQ().add(port);
					}
					it.remove();
				}
			}
			state.inCS = false;
			state.myRequestPending = false;
			state.myPendingQty = 0;
			if (!state.heldToken.getQ().isEmpty()) {
				forwardTo = state.heldToken.getQ().poll();
				tokenToForward = state.heldToken;
				state.heldToken = null;
			}
		}
		if (tokenToForward != null) {
			MessageUtil.sendMessage(new MutexTokenMessage(myPort, forwardTo, tokenToForward));
		}
		AppConfig.timestampedStandardPrint("[MUTEX-RELEASED]");
	}

	/**
	 * Owner-side execution of a buy request.
	 */
	public void executeBuyAtOwner(int itemId, int qty, int buyerPort) {
		int myPort = AppConfig.myServentInfo.getListenerPort();

		// Atomic check-and-decrement guarantees stock is never < 0,
		Ad updated = tryDecrementMyAd(itemId, qty);
		if (updated == null) {
			Ad existing = getMyAd(itemId);
			int remaining = (existing != null) ? existing.getQty() : 0;
			MessageUtil.sendMessage(new BuyExecReplyMessage(
					myPort, buyerPort, itemId, qty, remaining, false));
			return;
		}

		// Propagate qty change to backup
		int itemKey = keyHash(itemId);
		ServentInfo next = getNextNodeForKey(itemKey);
		MessageUtil.sendMessage(new BackupQtyUpdateMessage(
				myPort, next.getListenerPort(), itemId, updated.getQty()));

		MessageUtil.sendMessage(new BuyExecReplyMessage(
				myPort, buyerPort, itemId, qty, updated.getQty(), true));
	}

	/**
	 * Overwrites the backup ad's qty.
	 */
	public void handleBackupQtyUpdate(int itemId, int newQty) {
		int itemKey = keyHash(itemId);
		int myPort = AppConfig.myServentInfo.getListenerPort();
		if (isKeyMine(itemKey)) {
			Ad bkp = getBackupAd(itemId);
			if (bkp != null) {
				Ad updated = new Ad(bkp.getItemId(), bkp.getName(), newQty,
						bkp.getOwnerId(), bkp.getOwnerPort());
				storeBackupAd(updated);
			}
		} else {
			ServentInfo next = getNextNodeForKey(itemKey);
			MessageUtil.sendMessage(new BackupQtyUpdateMessage(
					myPort, next.getListenerPort(), itemId, newQty));
		}
	}

	/**
	 * BuyOwnerLookupHandler entry point.
	 */
	public void handleBuyOwnerLookup(int itemId, int originPort) {
		int itemKey = keyHash(itemId);
		int myPort = AppConfig.myServentInfo.getListenerPort();
		if (isKeyMine(itemKey)) {
			Ad bkp = getBackupAd(itemId);
			int ownerPort = (bkp != null) ? bkp.getOwnerPort() : -1;
			MessageUtil.sendMessage(new servent.message.BuyOwnerReplyMessage(
					myPort, originPort, itemId, ownerPort));
		} else {
			ServentInfo next = getNextNodeForKey(itemKey);
			MessageUtil.sendMessage(new BuyOwnerLookupMessage(
					myPort, next.getListenerPort(), itemId, originPort));
		}
	}

	/**
	 * Sends a MARKET_NOTIFICATION to every subscriber except self.
	 */
	public void notifySubscribers(Ad ad) {
		int myPort = AppConfig.myServentInfo.getListenerPort();
		int myChordId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo sub : getSubscribers()) {
			if (sub.getListenerPort() == myPort) {
				continue; // skip self-subscription — [MARKET-LIST] already confirms locally
			}
			MarketNotificationMessage msg = new MarketNotificationMessage(myPort, sub.getListenerPort(), myChordId,
					ad.getItemId());
			MessageUtil.sendMessage(msg);
		}
	}

	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it
	 * on.
	 */
	public void putValue(int key, int value) {
		if (isKeyMine(key)) {
			valueMap.put(key, value);
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, value);
			MessageUtil.sendMessage(pm);
		}
	}

	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise
	 * asks someone else to give us the value.
	 * 
	 * @return
	 *         <ul>
	 *         <li>The value, if we have it</li>
	 *         <li>-1 if we own the key, but there is nothing there</li>
	 *         <li>-2 if we asked someone else</li>
	 *         </ul>
	 */
	public int getValue(int key) {
		if (isKeyMine(key)) {
			if (valueMap.containsKey(key)) {
				return valueMap.get(key);
			} else {
				return -1;
			}
		}

		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(),
				String.valueOf(key));
		MessageUtil.sendMessage(agm);

		return -2;
	}

}
