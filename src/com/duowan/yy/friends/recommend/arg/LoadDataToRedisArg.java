/**
 * 
 */
package com.duowan.yy.friends.recommend.arg;

import java.util.StringTokenizer;

import com.duowan.yy.neo4j.dataimport.arg.CheckValid;

/**
 * Arguments: -redis <host:port:db> <-password password> <-in inputFile>.
 * 
 * @author zhangtao.robin
 * 
 */
public class LoadDataToRedisArg implements CheckValid {

	private String redisHost;
	private int redisPort = 6379;
	private int redisDb = 0;
	private String password;
	private String inputFile;
	private boolean cleanDB = true;

	public LoadDataToRedisArg(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-noclean")) {
				setCleanDB(false);
				continue;
			}
			if (++i >= args.length) {
				break;
			}
			String value = args[i];
			if (arg.equals("-redis")) {
				StringTokenizer st = new StringTokenizer(value, ":");
				setRedisHost(st.nextToken());
				if (st.hasMoreTokens()) {
					setRedisPort(Integer.parseInt(st.nextToken()));
				}
				if (st.hasMoreTokens()) {
					setRedisDb(Integer.parseInt(st.nextToken()));
				}
			} else if (arg.equals("-in")) {
				setInputFile(value);
			} else if (arg.equals("-password")) {
				setPassword(value);
			}
		}
	}

	/**
	 * If valid, return null; else return description
	 * 
	 * @return
	 */
	@Override
	public void checkValid() throws IllegalArgumentException {
		if (redisHost == null) {
			throw new IllegalArgumentException("Must set redis host and port");
		}
	}

	/**
	 * @return the inputFile
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile
	 *            the inputFile to set
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * @return the redisHost
	 */
	public String getRedisHost() {
		return redisHost;
	}

	/**
	 * @param redisHost
	 *            the redisHost to set
	 */
	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	/**
	 * @return the redisPort
	 */
	public int getRedisPort() {
		return redisPort;
	}

	/**
	 * @param redisPort
	 *            the redisPort to set
	 */
	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the redisDb
	 */
	public int getRedisDb() {
		return redisDb;
	}

	/**
	 * @param redisDb
	 *            the redisDb to set
	 */
	public void setRedisDb(int redisDb) {
		this.redisDb = redisDb;
	}

	/**
	 * @return the cleanDB
	 */
	public boolean isCleanDB() {
		return cleanDB;
	}

	/**
	 * @param cleanDB
	 *            the cleanDB to set
	 */
	public void setCleanDB(boolean cleanDB) {
		this.cleanDB = cleanDB;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LoadDataToRedisArg [redisHost=").append(redisHost).append(", redisPort=")
				.append(redisPort).append(", redisDb=").append(redisDb).append(", password=")
				.append(password).append(", inputFile=").append(inputFile).append(", cleanDB=")
				.append(cleanDB).append("]");
		return builder.toString();
	}

}
