package com.uhope.rl.application.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.log4j.Logger;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传工具类
 * @author ruanrj
 *
 */
public class FileUploadUtil {
	
	private final static Logger LOG = Logger.getLogger(FileUploadUtil.class);
	
	/*private final static String DFS_SERVICE = "dfsService";
	
	static IDfsService dfsService;
	static{
		dfsService = (IDfsService) SpringContextUtil.getBean(DFS_SERVICE);
	}*/

	static TrackerClient trackerClient;
	static TrackerServer trackerServer;
	static StorageServer storageServer;
	static StorageClient storageClient;

	static {
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath:fdfs.properties");
			//获得文件流，因为在jar文件中，不能直接通过文件资源路径拿到文件，但是可以在jar包中拿到文件流
			InputStream stream = resources[0].getInputStream();
			String targetFilePath = System.getProperty("java.io.tmpdir") + resources[0].getFilename();
			File ttfFile = new File(targetFilePath);
			inputstreamToFile(stream, ttfFile);

			/*String filePath = new ClassPathResource("fdfs.properties").getFile().getAbsolutePath();
			LOG.info("fdfs文件地址：" + filePath);
			String filePath1 = ResourceUtils.getFile("classpath:fdfs.properties").getAbsolutePath();
			LOG.info("fdfs1文件地址：" + filePath1);*/
			String filePath2 = ttfFile.getAbsolutePath();
			LOG.info("fdfs2文件地址：" + filePath2);

			ClientGlobal.init(filePath2);
			trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
			trackerServer = trackerClient.getConnection();
			storageServer = trackerClient.getStoreStorage(trackerServer);

			storageClient = new StorageClient(trackerServer, storageServer);
		} catch (Exception e) {
			LOG.error("FastDFS Client Init Fail!",e);
		}
	}



	/**
	 * 上传文件工具类
	 * @param files {@link  org.springframework.web.multipart.MultipartFile}
	 * @return
	 */
	/*public static String[] upload(MultipartFile[] files){
		return upload(files, new ArrayList<byte[]>(), new ArrayList<String>(), new ArrayList<String>());
	}

	*//**
	 * 上传文件工具类
	 * @param files {@link  org.springframework.web.multipart.MultipartFile}
	 * @param names 文件名
	 * @return
	 *//*
	public static String[] upload(MultipartFile[] files, List<String> names){
		return upload(files, new ArrayList<byte[]>(), names, new ArrayList<String>());
	}

	*//**
	 * 上传文件工具类
	 * @param files
	 * @param names 文件名
	 * @param types 文件类型
	 * @return
	 *//*
	public static String[] upload(MultipartFile[] files, List<String> names, List<String> types){
		return upload(files, new ArrayList<byte[]>(),names, types);
	}*/

	/**
	 * 上传文件工具类
	 * @return 	上传完生成文件名
	 */
	/*public static String[] upload(MultipartFile[] files, List<byte[]> filebytes, List<String> names, List<String> types){
		if(files != null && files.length > 0){
			String[] datas = null;
			for(MultipartFile file : files){
				try {
					filebytes.add(file.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					LOG.error(String.format("Upload file[%s] error: %s", file.getOriginalFilename(), e.getMessage()));
				}
				datas = getFileType(file.getOriginalFilename());
				if(datas != null && datas.length == 2){
					names.add(datas[0]);
					types.add(datas[1]);
				}
			}
			try {
				return	dfsService.upload(filebytes, types);
			} catch (IOException e) {
				e.printStackTrace();
				LOG.error(String.format("Upload file within dfs-service error: %s", e.getMessage()));
				return null;
			}
		}
		return null;
	}*/

	/*public static String[] upload(File[] files, List<String> names){
		if(files != null && files.length > 0){
			String[] datas = null;
			for(File f : files){
				datas = getFileType(f.getName());
				if(datas != null && datas.length == 2){
					names.add(datas[0]);
				}
			}
			try {
				return	dfsService.upload(files);
			} catch (IOException e) {
				e.printStackTrace();
				LOG.error(String.format("Upload file within dfs-service error: %s", e.getMessage()));
				return null;
			}
		}
		return null;
	}*/

	public static String[] upload(File... arrfile){
		if(arrfile != null){
			try {
				String[] filepath = new String[arrfile.length];
				for(int i = 0; i < arrfile.length; ++i) {
					filepath[i] = arrfile[i].getAbsolutePath();
				}

				ArrayList list = Lists.newArrayList();
				String[] res = null;
				String[] fileId = null;
				String[] e = filepath;
				int len$ = filepath.length;

				for(int i$ = 0; i$ < len$; ++i$) {
					String pathname = e[i$];
					fileId = storageClient.upload_file(pathname, Files.getFileExtension(pathname), (NameValuePair[])null);
					list.add(fileId != null?fileId[0] + "/" + fileId[1]:null);
				}

				if(null != list && list.size() > 0) {
					res = (String[])list.toArray(new String[list.size()]);
					list.clear();
				}

				return res;
				//return	dfsService.upload(files)[0];
			} catch (IOException e) {
				e.printStackTrace();
				LOG.error(String.format("Upload file within dfs-service error: %s", e.getMessage()));
				return null;
			} catch (MyException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	

	public static void inputstreamToFile(InputStream ins,File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		os.close();
		ins.close();
	}
}
