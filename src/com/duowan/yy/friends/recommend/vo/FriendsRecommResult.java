/**
 * 
 */
package com.duowan.yy.friends.recommend.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author zhangtao.robin
 * 
 */
public class FriendsRecommResult {
	private Long uid;
	private Map<Long, List<RecommReason>> friends;
	private int maxRecommNum;

	/**
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}

	/**
	 * @return the friends
	 */
	public Map<Long, List<RecommReason>> getFriends() {
		if (friends == null) {
			friends = new HashMap<Long, List<RecommReason>>();
		}
		return friends;
	}

	/**
	 * @return the maxRecommNum
	 */
	public int getMaxRecommNum() {
		return maxRecommNum;
	}

	/**
	 * @param maxRecommNum
	 *            the maxRecommNum to set
	 */
	public void setMaxRecommNum(int maxRecommNum) {
		this.maxRecommNum = maxRecommNum;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FriendsRecommResult [uid=").append(uid).append(", friends=")
				.append(friends).append("]");
		return builder.toString();
	}

	public void addRecommUser(Long uid, RecommReason reason) {
		List<RecommReason> reasons = null;

		if (friends == null) {
			friends = new HashMap<Long, List<RecommReason>>();
		} else {
			reasons = friends.get(uid);
		}

		if (reasons == null && friends.size() < maxRecommNum) {
			reasons = new ArrayList<RecommReason>();
			reasons.add(reason);
			friends.put(uid, reasons);
		} else if (reasons != null) {
			reasons.add(reason);
		}
	}

	public String toHiveString() {
		StringBuilder builder = new StringBuilder();
		builder.append(uid).append('\t');
		if (friends != null) {
			boolean hasNextUser = false;
			for (Entry<Long, List<RecommReason>> entry : friends.entrySet()) {
				if (hasNextUser) {
					builder.append(',');
				}
				builder.append(entry.getKey()).append(':');
				List<RecommReason> reasons = entry.getValue();
				boolean hasNextReason = false;
				for (RecommReason reason : reasons) {
					if (hasNextReason) {
						builder.append('|');
					}
					builder.append(reason.getReason());
					if (reason.getValue() != null) {
						builder.append('-').append(reason.getValue());
					}
					if (reason.getChannels() != null) {
						for (String tid : reason.getChannels()) {
							builder.append(' ').append(tid);
						}
					}
					hasNextReason = true;
				}
				hasNextUser = true;
			}
		}
		return builder.append('\n').toString();
	}

}
