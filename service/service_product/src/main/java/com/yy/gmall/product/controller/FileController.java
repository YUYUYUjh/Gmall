package com.yy.gmall.product.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.product.utils.FastDFSUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Yu
 * @create 2021-09-29 18:21
 */
@RestController
@RequestMapping("/admin/product")
@RefreshScope //扩展配置文件内容变更 不用重启服务器
public class FileController {

    @Value("${img.url}")
    private String imgUrl;

    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file){
        System.out.println(file.getOriginalFilename());//2c50e5ebb1efbaa8.jpg
        //获取文件后缀名
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        //文件的上传
        String fileId = null;
        try {
            fileId = FastDFSUtil.uploadFile(file.getBytes(), ext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.ok(imgUrl + fileId);
    }

}
