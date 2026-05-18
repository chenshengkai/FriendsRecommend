package com.duowan.yy.friends.recommend;

/**
 * 
 */

import java.io.IOException;

import redis.clients.jedis.Jedis;

import com.duowan.yy.friends.recommend.arg.LoadDataToRedisArg;
import com.duowan.yy.friends.recommend.vo.FriendsRecommResultRedis;

/**
 * Loading friends recommendations to redis server.
 * 
 * @author zhangtao.robin
 * 
 */
public class LoadFriendsRecommRedis extends
		AbstractLoadDataToRedis<FriendsRecommResultRedis, LoadDataToRedisArg> {

	@Override
	public FriendsRecommResultRedis parseLine(String line) {
		return FriendsRecommResultRedis.parseHiveString(line);
	}

	@Override
	public void loadDataToRedis(Jedis redis, FriendsRecommResultRedis data) throws IOException {
		redis.hmset(data.getKey(), data.getHash());
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LoadDataToRedisArg mainArg = new LoadDataToRedisArg(args);
		System.out.println(mainArg.toString());
		try {
			mainArg.checkValid();
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			System.err
					.println("Usage: java "
							+ LoadFriendsRecommRedis.class.getName()
							+ " -redis <host:port:db> <-password password> <-in inputFile> <-noclean>");
			System.exit(-1);
		}

		LoadFriendsRecommRedis loader = new LoadFriendsRecommRedis();
		loader.setMainArg(mainArg);
		try {
			loader.runLoadData();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

}
