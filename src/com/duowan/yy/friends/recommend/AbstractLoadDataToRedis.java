package com.duowan.yy.friends.recommend;

/**
 * 
 */

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.duowan.yy.friends.recommend.arg.LoadDataToRedisArg;

/**
 * Loading datas to redis server.
 * 
 * @author zhangtao.robin
 * 
 */
public abstract class AbstractLoadDataToRedis<T extends Object, A extends LoadDataToRedisArg> {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	private static final int TEXT_BUFFER_SIZE = 128 * 1024;
	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	private int queueCapacity = 10000;
	private int threadPoolSize = 0;

	private BlockingQueue<T> queueDataIn;
	private ExecutorService threadPool;
	private List<DataProcess> dataProcesses;

	private A mainArg;

	private BufferedReader reader;
	private JedisPool redisPool;
	private int spacingNumber = 100000;

	public void runLoadData() throws IOException, InterruptedException {
		long startRunTime = 0L;
		int sum = 0;
		try {
			initReader();
			startRedisPool();
			cleanRedis();
			startDataProcesses();
			startRunTime = System.currentTimeMillis();
			String line = null;
			while ((line = reader.readLine()) != null) {
				T data = parseLine(line);
				if (data != null) {
					queueDataIn.put(data);
					if (++sum % spacingNumber == 0) {
						log.info("Process datas: " + sum + ", spent time: " +
								(System.currentTimeMillis() - startRunTime) / 1000);
					}
				}
			}
		} finally {
			waitQueueEmpty();
			stopDataProcesses();
			shutdownThreadPool();
			stopRedisPool();
			closeReader();
		}
		log.info("Total process datas: " + sum + ", spent time: " +
				(System.currentTimeMillis() - startRunTime) / 1000);
	}

	public abstract T parseLine(String line);

	public abstract void loadDataToRedis(Jedis redis, T data) throws IOException;

	/**
	 * @return the mainArg
	 */
	public A getMainArg() {
		return mainArg;
	}

	/**
	 * @param mainArg
	 *            the mainArg to set
	 */
	public void setMainArg(A mainArg) {
		this.mainArg = mainArg;
	}

	/**
	 * @return the queueCapacity
	 */
	public int getQueueCapacity() {
		return queueCapacity;
	}

	/**
	 * @param queueCapacity
	 *            the queueCapacity to set
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	/**
	 * @return the threadPoolSize
	 */
	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	/**
	 * @param threadPoolSize
	 *            the threadPoolSize to set
	 */
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	private void initReader() throws IOException {
		if (mainArg.getInputFile() == null) {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(FileDescriptor.in), UTF8_CHARSET), TEXT_BUFFER_SIZE);
		} else {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					mainArg.getInputFile()), UTF8_CHARSET), TEXT_BUFFER_SIZE);
		}
	}

	private void closeReader() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private void startRedisPool() {
		redisPool = new JedisPool(new JedisPoolConfig(), mainArg.getRedisHost(),
				mainArg.getRedisPort(), 3000, mainArg.getPassword(), mainArg.getRedisDb());
	}

	private void stopRedisPool() {
		if (redisPool != null) {
			redisPool.destroy();
		}
	}

	private void startDataProcesses() {
		if (threadPoolSize == 0) {
			int cores = Runtime.getRuntime().availableProcessors() / 2;
			threadPoolSize = cores < 4 ? 4 : cores;
		}
		queueDataIn = new ArrayBlockingQueue<T>(queueCapacity);
		threadPool = Executors.newFixedThreadPool(threadPoolSize);

		dataProcesses = new ArrayList<DataProcess>(threadPoolSize);
		for (int i = 0; i < threadPoolSize; i++) {
			DataProcess process = new DataProcess();
			dataProcesses.add(process);
			threadPool.execute(process);
		}
		log.info("Start data process threads: " + threadPoolSize);
	}

	private void stopDataProcesses() {
		if (dataProcesses != null) {
			for (int i = 0; i < threadPoolSize; i++) {
				DataProcess process = dataProcesses.get(i);
				if (process != null) {
					process.stopMe();
				}
			}
		}
	}

	private void waitQueueEmpty() {
		if (queueDataIn != null) {
			while (queueDataIn.size() != 0) {
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	private void shutdownThreadPool() {
		if (threadPool != null) {
			threadPool.shutdown();
			try {
				// Wait a while for existing tasks to terminate
				if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
					threadPool.shutdownNow(); // Cancel currently executing
												// tasks
					// Wait a while for tasks to respond to being cancelled
					if (!threadPool.awaitTermination(6, TimeUnit.SECONDS))
						log.error("Pool did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				threadPool.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}
	}

	private void cleanRedis() {
		if (mainArg.isCleanDB()) {
			log.info("Cleaning DB " + mainArg.getRedisDb());
			Jedis redis = redisPool.getResource();

			// temporary setting timeout infinite
			redis.getClient().setTimeoutInfinite();
			try {
				redis.flushDB();
			} finally {
				// restore timeout
				redis.getClient().rollbackTimeout();
				redisPool.returnResource(redis);
			}
		}
	}

	class DataProcess implements Runnable {

		private boolean running = true;

		public DataProcess() {
			super();
		}

		public void stopMe() {
			running = false;
		}

		@Override
		public void run() {
			while (running) {
				T data = null;
				try {
					data = queueDataIn.poll(500, TimeUnit.MICROSECONDS);
					if (data == null) {
						continue;
					}

					Jedis redis = redisPool.getResource();
					try {
						loadDataToRedis(redis, data);
					} finally {
						redisPool.returnResource(redis);
					}

				} catch (InterruptedException e) {
					if (running) {
						e.printStackTrace(System.err);
					}
				} catch (Exception e) {
					log.error("Process data " + data + " error: " + e, e);
				}
			}
		}
	}

}
