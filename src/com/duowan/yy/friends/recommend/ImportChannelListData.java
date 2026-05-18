/**
 * 
 */
package com.duowan.yy.friends.recommend;

import com.duowan.yy.friends.recommend.vo.ChannelInfo;
import com.duowan.yy.neo4j.dataimport.arg.ImportDataArg;

/**
 * @author zhangtao.robin
 * 
 */
public class ImportChannelListData extends CommonImportData<ChannelInfo, ImportDataArg> {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ImportDataArg importArg = new ImportDataArg(args);

		try {
			importArg.checkValid();
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			System.err
					.println("Usage: java " + ImportChannelListData.class.getName()
							+ " -db <dbName> -in <inputFile> <-clean>");
			System.exit(-1);
		}

		ImportChannelListData importer = new ImportChannelListData();
		importer.setImportArg(importArg);
		try {
			importer.runBatchInsert();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

	@Override
	public ChannelInfo parseLine(String line) {
		ChannelInfo chInfo = new ChannelInfo();
		chInfo.setTid(line);
		return chInfo;
	}

	@Override
	public void processData(ChannelInfo data) {
		createChannelNode(data);
	}

}
