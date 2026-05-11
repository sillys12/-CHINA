package com.wenzhi.backend.upload;

import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.wenzhi.backend.config.QiniuProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
  private final QiniuProperties props;
  private final UploadManager uploadManager;
  private final QiniuUrlService urlService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public UploadController(QiniuProperties props, QiniuUrlService urlService) {
    this.props = props;
    this.urlService = urlService;
    var cfg = Configuration.create();
    this.uploadManager = new UploadManager(cfg);
  }

  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadImages(@RequestParam("files") List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return ResponseEntity.badRequest().body(new ErrorResponse("请选择图片"));
    }
    if (files.size() > 9) {
      return ResponseEntity.badRequest().body(new ErrorResponse("最多上传 9 张图片"));
    }
    if (!StringUtils.hasText(props.getAccessKey()) || !StringUtils.hasText(props.getSecretKey())) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("七牛配置缺失（accessKey/secretKey）"));
    }
    if (!StringUtils.hasText(props.getBucket()) || !StringUtils.hasText(props.getDomain())) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("七牛配置缺失（bucket/domain）"));
    }

    Auth auth = Auth.create(props.getAccessKey(), props.getSecretKey());
    String token = auth.uploadToken(props.getBucket());
    List<ImageUploadResponse> out = new ArrayList<>();
    for (MultipartFile f : files) {
      if (f == null || f.isEmpty()) continue;
      var ct = f.getContentType();
      if (ct == null || !ct.startsWith("image/")) {
        return ResponseEntity.badRequest().body(new ErrorResponse("只支持图片文件"));
      }
      try {
        String ext = guessExt(ct);
        String key = "square/" + UUID.randomUUID() + ext;
        Response r = uploadManager.put(f.getBytes(), key, token);
        if (!r.isOK()) {
          return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse("上传失败：" + r.error));
        }
        DefaultPutRet ret = objectMapper.readValue(r.bodyString(), DefaultPutRet.class);
        String url = normalizeDomain(props.getDomain()) + "/" + ret.key;
        out.add(new ImageUploadResponse(ret.key, urlService.signIfPossible(url)));
      } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse("上传失败：" + e.getMessage()));
      }
    }
    return ResponseEntity.ok(out);
  }

  private String normalizeDomain(String domain) {
    String d = domain.trim();
    if (d.endsWith("/")) d = d.substring(0, d.length() - 1);
    return d;
  }

  private String guessExt(String contentType) {
    return switch (contentType) {
      case "image/png" -> ".png";
      case "image/jpeg" -> ".jpg";
      case "image/gif" -> ".gif";
      case "image/webp" -> ".webp";
      default -> "";
    };
  }

  public record ImageUploadResponse(String key, String url) {}

  public record ErrorResponse(String message) {}
}

