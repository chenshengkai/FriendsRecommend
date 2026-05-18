package com.duowan.yy.friends.recommend.arg;

import com.duowan.yy.neo4j.dataimport.arg.ProcessDataArg;

public class FriendsRecommArg extends ProcessDataArg {

	private int limits = 28;

	private int maxSameChannels = 3;

	public FriendsRecommArg(String[] args) {
		super(args);
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (++i >= args.length) {
				break;
			}
			String value = args[i];
			if (arg.equals("-limits")) {
				setLimits(Integer.valueOf(value));
			} else if (arg.equals("-maxchannels")) {
				setMaxSameChannels(Integer.valueOf(value));
			}
		}

	}

	/**
	 * @return the limits
	 */
	public int getLimits() {
		return limits;
	}

	/**
	 * @param limits
	 *            the limits to set
	 */
	public void setLimits(int limits) {
		this.limits = limits;
	}

	/**
	 * @return the maxSameChannels
	 */
	public int getMaxSameChannels() {
		return maxSameChannels;
	}

	/**
	 * @param maxSameChannels
	 *            the maxSameChannels to set
	 */
	public void setMaxSameChannels(int maxSameChannels) {
		this.maxSameChannels = maxSameChannels;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FriendsRecommArg [limits=").append(limits).append(", maxSameChannels=")
				.append(maxSameChannels).append(", ProcessDataArg=").append(super.toString())
				.append("]");
		return builder.toString();
	}

}
