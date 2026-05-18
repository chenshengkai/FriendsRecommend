/**
 * 
 */
package com.duowan.yy.friends.recommend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

import com.duowan.yy.friends.recommend.arg.FriendsRecommArg;
import com.duowan.yy.friends.recommend.vo.FriendsRecommResult;
import com.duowan.yy.friends.recommend.vo.RecommReason;
import com.duowan.yy.friends.recommend.vo.UserInfo;
import com.duowan.yy.neo4j.dataimport.AbstractProcessDataFromFileParallel;

/**
 * @author zhangtao.robin
 * 
 */
public class FriendsRecommParallel extends
		AbstractProcessDataFromFileParallel<UserInfo, FriendsRecommArg> {

	private Index<Node> userIndexer;

	private int processLimits;

	public FriendsRecommParallel() {
		super();
		// setThreadPoolSize(16);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FriendsRecommArg mainArg = new FriendsRecommArg(args);

		try {
			mainArg.checkValid();
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			System.err
					.println("Usage: java " + FriendsRecommParallel.class.getName()
							+ " -db <dbName> -in <inputFile> -out <outputFile> -limits <number>");
			System.exit(-1);
		}
		/*
				System.out.println(mainArg);
				try {
					new BufferedReader(new InputStreamReader(System.in)).readLine();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("Staring ...");
		*/
		FriendsRecommParallel recomm = new FriendsRecommParallel();
		recomm.setProcessArg(mainArg);
		try {
			recomm.runProcessData();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

	private Long parseUserId(String uidStr) {
		try {
			return Long.valueOf(uidStr);
		} catch (NumberFormatException ne) {
			log.error("UID not number: " + uidStr);
			return null;
		}
	}

	private Node findUserNode(UserInfo userInfo) {
		IndexHits<Node> hits = userIndexer.get(RecommConstants.UID_TEXT, userInfo.getUid());
		Node node = null;
		try {
			node = hits.getSingle();
		} finally {
			hits.close();
		}
		return node;
	}

	private void userFollows(Node userNode, FriendsRecommResult result) {
		TraversalDescription tdOut = Traversal.description()
				.uniqueness(Uniqueness.NODE_PATH)
				.breadthFirst()
				.relationships(RelTypes.USER_FOLLOW, Direction.OUTGOING)
				.evaluator(Evaluators.atDepth(1));

		Traverser traverserOut = tdOut.traverse(userNode);
		if (traverserOut == null) {
			return;
		}

		TraversalDescription tdIn = Traversal.description()
				.uniqueness(Uniqueness.NODE_PATH)
				.breadthFirst()
				.relationships(RelTypes.USER_FOLLOW, Direction.INCOMING)
				.evaluator(Evaluators.atDepth(1));

		Traverser traverserIn = tdIn.traverse(userNode);
		if (traverserIn == null) {
			return;
		}

		List<Node> nodesOut = new ArrayList<Node>();
		for (Node node : traverserOut.nodes()) {
			nodesOut.add(node);
		}
		List<Node> nodeIn = new ArrayList<Node>();
		for (Node node : traverserIn.nodes()) {
			nodeIn.add(node);
		}
		nodesOut.retainAll(nodeIn);

		if (nodesOut.size() == 0) {
			return;
		}

		Set<Long> follows = new HashSet<Long>();
		for (Node node : nodesOut) {
			Long uid = (Long) node.getProperty(RecommConstants.UID_TEXT);
			if (follows.add(uid)) {
				result.addRecommUser(uid, RecommReason.USER_FOLLOW_REASON);
			}
			if (follows.size() >= getProcessArg().getLimits()) {
				break;
			}
		}
	}

	private void userGameBindings(Node userNode, FriendsRecommResult result) {
		TraversalDescription td = Traversal.description()
				.uniqueness(Uniqueness.NODE_PATH)
				// .breadthFirst()
				.depthFirst()
				.relationships(RelTypes.GAME_BINDING)
				.evaluator(Evaluators.atDepth(2));

		Traverser traverser = td.traverse(userNode);
		if (traverser == null) {
			return;
		}

		Set<Long> bindings = new HashSet<Long>();
		for (Node node : traverser.nodes()) {
			Long uid = (Long) node.getProperty(RecommConstants.UID_TEXT);
			if (bindings.add(uid)) {
				result.addRecommUser(uid, RecommReason.GAME_BINDING_REASON);
			}
			if (bindings.size() >= processLimits) {
				break;
			}
		}
	}

	private void userChannelLevels(Node userNode, FriendsRecommResult result) {
		TraversalDescription td = Traversal.description()
				.uniqueness(Uniqueness.NODE_PATH)
				// .breadthFirst()
				.depthFirst()
				.relationships(RelTypes.CHANNEL_LEVEL)
				.evaluator(Evaluators.atDepth(2));

		Traverser traverser = td.traverse(userNode);
		if (traverser == null) {
			return;
		}

		Map<Long, Integer> levels = new HashMap<Long, Integer>();
		Map<Long, Set<String>> channels = new HashMap<Long, Set<String>>();
		for (Path path : traverser) {
			String tid = null;
			for (Node node : path.nodes()) {
				if (node.getId() == userNode.getId()) {
					continue;
				}
				String tmpTid = (String) node.getProperty(RecommConstants.TID_TEXT, null);
				if (tmpTid != null) {
					tid = tmpTid;
				} else {
					// User node
					Long uid = (Long) node.getProperty(RecommConstants.UID_TEXT);

					Set<String> set = channels.get(uid);
					if (set == null) {
						set = new HashSet<String>(getProcessArg().getMaxSameChannels());
						set.add(tid);
						channels.put(uid, set);
					} else if (set.size() < getProcessArg().getMaxSameChannels()) {
						set.add(tid);
					}

					Integer value = levels.get(uid);
					if (value == null) {
						levels.put(uid, Integer.valueOf(1));
						if (levels.size() >= processLimits) {
							break;
						}
					} else {
						levels.put(uid, Integer.valueOf(value + 1));
					}

				}
			}
		}

		@SuppressWarnings("unchecked")
		Entry<Long, Integer>[] arr = (Entry<Long, Integer>[]) levels.entrySet().toArray(
				new Entry[levels.size()]);
		Arrays.sort(arr, new Comparator<Entry<Long, Integer>>() {

			@Override
			public int compare(Entry<Long, Integer> o1, Entry<Long, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		for (Entry<Long, Integer> entry : arr) {
			result.addRecommUser(
					entry.getKey(),
					RecommReason.newChannelLevelReason(entry.getValue(),
							channels.get(entry.getKey())));
		}
	}

	@Override
	public UserInfo parseLine(String line) {
		UserInfo user = new UserInfo();
		user.setUid(parseUserId(line));
		return user;
	}

	/* (non-Javadoc)
	 * @see com.duowan.yy.neo4j.dataimport.AbstractProcessDataFromFileParallel#processDataThreaded(java.lang.Object)
	 */
	@Override
	public void processDataThreaded(UserInfo data) throws IOException {
		// find the user node
		Node userNode = findUserNode(data);
		if (userNode == null) {
			log.error("Can't find user node with " + data);
			return;
		}

		FriendsRecommResult result = new FriendsRecommResult();
		result.setUid(data.getUid());
		// set limits
		result.setMaxRecommNum(getProcessArg().getLimits());

		// long s1 = System.currentTimeMillis();
		// first, user follows
		userFollows(userNode, result);
		if (log.isDebugEnabled()) {
			log.debug("After userFollows, the result: " + result);
		}
		// long s2 = System.currentTimeMillis();
		// next, game binding
		userGameBindings(userNode, result);
		if (log.isDebugEnabled()) {
			log.debug("After userGameBindings, the result: " + result);
		}
		// long s3 = System.currentTimeMillis();
		// next, channel level
		userChannelLevels(userNode, result);
		if (log.isDebugEnabled()) {
			log.debug("After userChannelLevels, the result: " + result);
		}
		// long s4 = System.currentTimeMillis();
		/*
		if (log.isInfoEnabled()) {
			log.info("Uid: " + data.getUid() + ", userFollows: " + (s2 - s1)
					+ ", userGameBindings: " + (s3 - s2)
					+ ", userChannelLevels: " + (s4 - s3) + ", total: " + (s4 - s1));
		}
		*/
		if (result.getFriends().size() != 0) {
			getWriter().write(result.toHiveString());
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Cannot find any recommendation for user: " + data);
			}
		}
	}

	@Override
	public void initAfterOpenDB() {
		super.initAfterOpenDB();
		userIndexer = getGraphDb().index().forNodes(RecommConstants.USER_NODES);
		processLimits = getProcessArg().getLimits() * 2;
	}

	@Override
	public void cleanOtherVars() {
		userIndexer = null;
	}

	/* (non-Javadoc)
	 * @see com.duowan.yy.neo4j.dataimport.AbstractGraphDB#getDBConfig()
	 */
	@Override
	public Map<String, String> getDBConfig() {
		Map<String, String> config = new HashMap<String, String>();
		config.put("cache_type", "none");
		// config.put("dump_configuration", "true");
		return config;
	}
}
