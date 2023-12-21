package cn.edu.sustech.ooadbackend.utils;

import cn.edu.sustech.ooadbackend.common.FileType;
import cn.edu.sustech.ooadbackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * Cos 对象存储操作
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;
    
    @Resource
    private RedisClient redisClient;

    /**
     * 上传对象
     * @param key 唯一键
     * @param localFilePath 本地文件路径
     * @param fileType 上传文件的类型
     * @return PutObjectResult
     */
    public PutObjectResult putObject(String key, String localFilePath, FileType fileType) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), fileType.getFolder() + key, new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     * @param key 唯一键
     * @param file 本地文件
     * @param fileType 上传文件的类型
     * @return PutObjectResult
     */
    public PutObjectResult putObject(String key, File file, FileType fileType) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), fileType.getFolder() + key, file);
        return cosClient.putObject(putObjectRequest);
    }
    
    /**
     * 上传对象
     * @param key 唯一键
     * @param fileStream 文件流
     * @param fileType 上传文件的类型
     * @return PutObjectResult
     */
    public PutObjectResult putObject(String key, InputStream fileStream, long fileSize, FileType fileType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), fileType.getFolder() + key, fileStream, metadata);
        return cosClient.putObject(putObjectRequest);
    }
    
    /**
     * 根据对象key获取预签名访问Url(过期时间1分钟)
     * @param objectKey 对象唯一键
     * @return 预签名访问Url
     */
    public String getObjectPresignedUrl(String objectKey, FileType fileType) {
        return getObjectPresignedUrl(objectKey, 60, fileType); // 有效期1分钟
    }
    
    /**
     * 根据对象key获取预签名访问Url
     * @param objectKey 对象唯一键
     * @param expirationSecond 过期时间（单位：秒）
     * @return 预签名访问Url
     */
    public String getObjectPresignedUrl(String objectKey, long expirationSecond, FileType fileType) {
        String key = fileType.getFolder() + objectKey;
        
        // 若存在redis缓存，返回redis缓存中的Url
        if (redisClient.hasKey(key)) {
            return redisClient.get(key);
        }
        
        // 设置 URL 的过期时间
        Date expiration = new Date(new Date().getTime() + 1000 * expirationSecond);
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(cosClientConfig.getBucket(), key, HttpMethodName.GET);
        req.setExpiration(expiration);
        
        // 获取预签名链接
        String presignedUrl = cosClient.generatePresignedUrl(req).toString();
        
        // 设置Redis缓存
        redisClient.set(key, presignedUrl, expirationSecond);
        
        return presignedUrl;
    }
}
