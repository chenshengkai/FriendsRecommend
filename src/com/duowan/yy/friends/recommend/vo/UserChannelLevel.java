/**
 * 
 */
package com.duowan.yy.friends.recommend.vo;

import java.util.Map;

/**
 * @author zhangtao.robin
 * 
 */
public class UserChannelLevel {

	private UserInfo userInfo;
	private Map<ChannelInfo, Integer> mapLevels;

	/**
	 * @return the userInfo
	 */
	public UserInfo getUserInfo() {
		return userInfo;
	}

	/**
	 * @param userInfo
	 *            the userInfo to set
	 */
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	/**
	 * @return the mapLevels
	 */
	public Map<ChannelInfo, Integer> getMapLevels() {
		return mapLevels;
	}

	/**
	 * @param mapLevels
	 *            the mapLevels to set
	 */
	public void setMapLevels(Map<ChannelInfo, Integer> mapLevels) {
		this.mapLevels = mapLevels;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserChannelLevel [userInfo=").append(userInfo).append(", mapLevels=")
				.append(mapLevels).append("]");
		return builder.toString();
	}

}
