package com.duowan.yy.friends.recommend;

/**
 * 
 */

import java.io.IOException;

import redis.clients.jedis.Jedis;

import com.duowan.yy.friends.recommend.arg.LoadDataToRedisArg;
import com.duowan.yy.friends.recommend.vo.SameCityUsers;

/**
 * Loading same city users to redis server.
 * 
 * @author zhangtao.robin
 * 
 */
public class LoadSameCityUsersRedis extends
		AbstractLoadDataToRedis<SameCityUsers, LoadDataToRedisArg> {

	@Override
	public SameCityUsers parseLine(String line) {
		return SameCityUsers.parseHiveString(line);
	}

	@Override
	public void loadDataToRedis(Jedis redis, SameCityUsers data) throws IOException {
		redis.sadd(data.getKey(), data.getMembers());
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
							+ LoadSameCityUsersRedis.class.getName()
							+ " -redis <host:port:db> <-password password> <-in inputFile> <-noclean>");
			System.exit(-1);
		}

		LoadSameCityUsersRedis loader = new LoadSameCityUsersRedis();
		loader.setMainArg(mainArg);
		try {
			loader.runLoadData();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

}
