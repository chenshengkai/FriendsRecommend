/**
 * 
 */
package com.duowan.yy.friends.recommend.vo;

/**
 * @author zhangtao.robin
 * 
 */
public class UserGameBinding {

	private UserInfo userInfo;
	private GameRegionInfo gameRegionInfo;

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
	 * @return the gameRegionInfo
	 */
	public GameRegionInfo getGameRegionInfo() {
		return gameRegionInfo;
	}

	/**
	 * @param gameRegionInfo
	 *            the gameRegionInfo to set
	 */
	public void setGameRegionInfo(GameRegionInfo gameRegionInfo) {
		this.gameRegionInfo = gameRegionInfo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserGameBinding [userInfo=").append(userInfo).append(", gameRegionInfo=")
				.append(gameRegionInfo).append("]");
		return builder.toString();
	}

}
