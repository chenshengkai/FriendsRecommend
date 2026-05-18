package com.duowan.yy.friends.recommend;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;

import com.duowan.yy.friends.recommend.vo.ChannelInfo;
import com.duowan.yy.friends.recommend.vo.GameRegionInfo;
import com.duowan.yy.friends.recommend.vo.UserInfo;
import com.duowan.yy.neo4j.dataimport.AbstractBatchInsertDBFromFile;
import com.duowan.yy.neo4j.dataimport.arg.ImportDataArg;

public abstract class CommonImportData<T extends Object, A extends ImportDataArg> extends
		AbstractBatchInsertDBFromFile<T, A> {

	protected final Pattern SPLIT_PATTERN_COMMA = Pattern.compile(",");
	protected final Pattern SPLIT_PATTERN_TAB = Pattern.compile("\t");
	protected final Pattern SPLIT_PATTERN_COLON = Pattern.compile(":");

	private BatchInserterIndex userBatchIndexer;
	private BatchInserterIndex gameRegionBatchIndexer;
	private BatchInserterIndex channelBatchIndexer;

	@Override
	public void initAfterOpenDB() {
		userBatchIndexer = getIndexProvider().nodeIndex(RecommConstants.USER_NODES,
				MapUtil.stringMap("type", "exact"));
		userBatchIndexer.setCacheCapacity(RecommConstants.UID_TEXT, RecommConstants.CACHE_CAPACITY);

		gameRegionBatchIndexer = getIndexProvider().nodeIndex(RecommConstants.GAME_REGION_NODES,
				MapUtil.stringMap("type", "exact"));
		gameRegionBatchIndexer.setCacheCapacity(RecommConstants.GAME_REGION_TEXT,
				RecommConstants.CACHE_CAPACITY);

		channelBatchIndexer = getIndexProvider().nodeIndex(RecommConstants.CHANNEL_NODES,
				MapUtil.stringMap("type", "exact"));
		channelBatchIndexer.setCacheCapacity(RecommConstants.TID_TEXT,
				RecommConstants.CACHE_CAPACITY);
	}

	@Override
	public void finalizeBeforeCloseDB() {
		if (userBatchIndexer != null) {
			userBatchIndexer.flush();
		}
		if (gameRegionBatchIndexer != null) {
			gameRegionBatchIndexer.flush();
		}
		if (channelBatchIndexer != null) {
			channelBatchIndexer.flush();
		}
	}

	@Override
	public void cleanOtherVars() {
		userBatchIndexer = null;
		gameRegionBatchIndexer = null;
		channelBatchIndexer = null;
	}

	protected Long parseUserId(String uidStr) {
		try {
			return Long.valueOf(uidStr);
		} catch (NumberFormatException ne) {
			log.error("UID not number: " + uidStr);
			return null;
		}
	}

	protected Long findUserNode(UserInfo userInfo) {
		IndexHits<Long> hits = userBatchIndexer.get(RecommConstants.UID_TEXT, userInfo.getUid());
		Long node = null;
		try {
			node = hits.getSingle();
		} finally {
			hits.close();
		}
		return node;
	}

	protected Long createUserNode(UserInfo userInfo) {
		Map<String, Object> nodeProperties = Collections.singletonMap(RecommConstants.UID_TEXT,
				(Object) userInfo.getUid());
		// create user node, with uid property
		long node = getInserter().createNode(nodeProperties);
		// add uid to user index
		userBatchIndexer.add(node, nodeProperties);
		return node;
	}

	protected Long findGameRegionNode(GameRegionInfo gameRegionInfo) {
		IndexHits<Long> hits = gameRegionBatchIndexer.get(RecommConstants.GAME_REGION_TEXT,
				gameRegionInfo.getGameRegionService());
		Long node = null;
		try {
			node = hits.getSingle();
		} finally {
			hits.close();
		}
		return node;
	}

	protected Long createGameRegionNode(GameRegionInfo gameRegionInfo) {
		Map<String, Object> nodeProperties = Collections.singletonMap(
				RecommConstants.GAME_REGION_TEXT,
				(Object) (gameRegionInfo.getGameRegionService()));
		long node = getInserter().createNode(nodeProperties);
		gameRegionBatchIndexer.add(node, nodeProperties);
		return node;
	}

	protected Long findChannelNode(ChannelInfo chInfo) {
		IndexHits<Long> hits = channelBatchIndexer.get(RecommConstants.TID_TEXT, chInfo.getTid());
		Long node = null;
		try {
			node = hits.getSingle();
		} finally {
			hits.close();
		}
		return node;
	}

	protected Long createChannelNode(ChannelInfo chInfo) {
		Map<String, Object> nodeProperties = Collections.singletonMap(RecommConstants.TID_TEXT,
				(Object) chInfo.getTid());
		long node = getInserter().createNode(nodeProperties);
		channelBatchIndexer.add(node, nodeProperties);
		return node;
	}

}