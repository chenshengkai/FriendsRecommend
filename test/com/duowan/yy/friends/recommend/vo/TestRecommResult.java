package com.duowan.yy.friends.recommend.vo;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class TestRecommResult extends TestCase {

	public void testFriendsRecommResult() throws Exception {
		FriendsRecommResult result = new FriendsRecommResult();
		result.setUid(3L);
		result.setMaxRecommNum(10);

		result.addRecommUser(1L, RecommReason.USER_FOLLOW_REASON);
		result.addRecommUser(1L, RecommReason.GAME_BINDING_REASON);
		result.addRecommUser(2L, RecommReason.USER_FOLLOW_REASON);
		result.addRecommUser(2L, RecommReason.GAME_BINDING_REASON);
		result.addRecommUser(2L, RecommReason.newChannelLevelReason(2, null));

		System.out.println(result);

		String hiveStr = result.toHiveString();
		System.out.println(hiveStr);

		FriendsRecommResultRedis recommRedis = FriendsRecommResultRedis.parseHiveString(hiveStr);
		System.out.println(recommRedis);
	}

	public void testFriendsRecommResult2() throws Exception {
		FriendsRecommResult result = new FriendsRecommResult();
		result.setUid(3L);
		result.setMaxRecommNum(10);

		result.addRecommUser(1L, RecommReason.USER_FOLLOW_REASON);
		result.addRecommUser(1L, RecommReason.GAME_BINDING_REASON);
		result.addRecommUser(2L, RecommReason.USER_FOLLOW_REASON);
		result.addRecommUser(2L, RecommReason.GAME_BINDING_REASON);
		Set<String> set = new HashSet<String>();
		set.add("101");
		set.add("102");
		result.addRecommUser(2L, RecommReason.newChannelLevelReason(2, set));

		System.out.println(result);

		String hiveStr = result.toHiveString();
		System.out.println(hiveStr);

		FriendsRecommResultRedis recommRedis = FriendsRecommResultRedis.parseHiveString(hiveStr);
		System.out.println(recommRedis);
	}
}
