/**
 * 
 */
package com.duowan.yy.friends.recommend.vo;

/**
 * @author zhangtao.robin
 * 
 */
public class GameRegionInfo {
	private String gameRegionService;

	/**
	 * @return the gameRegionService
	 */
	public String getGameRegionService() {
		return gameRegionService;
	}

	/**
	 * @param gameRegionService
	 *            the gameRegionService to set
	 */
	public void setGameRegionService(String gameRegionService) {
		this.gameRegionService = gameRegionService;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GameRegionInfo [gameRegionService=").append(gameRegionService).append("]");
		return builder.toString();
	}

}
