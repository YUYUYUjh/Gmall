package com.yy.gmall.product.utils;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.ClassUtils;

import java.io.IOException;

/**
 * @author Yu
 * @create 2021-09-29 18:24
 */
public class FastDFSUtil {

    private static TrackerClient trackerClient = null;
    static {
        try {
            String path = ClassUtils.getDefaultClassLoader().getResource("fdfs_client.conf").getPath();
            //0:设置配置文件的位置进行读取  init IO流 读取配置文件 必须是绝对路径
            ClientGlobal.init(path);
            //1:连接Tracker跟踪器
            trackerClient = new TrackerClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //上传
    public static String uploadFile(byte[] file,String ext){
        try {
            //2.返回值存储节点地址
            TrackerServer trackerServer = trackerClient.getConnection();
            //3.连接存储节点
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,null);
            //4.上传文件   param1:文件 , param2: 后缀 , param3:文件附加信息
            String fileId = storageClient1.upload_appender_file1(file,ext,null);
            System.out.println(fileId);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
