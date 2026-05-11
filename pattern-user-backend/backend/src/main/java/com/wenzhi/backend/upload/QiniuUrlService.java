package com.wenzhi.backend.upload;

import com.qiniu.util.Auth;
import com.wenzhi.backend.config.QiniuProperties;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class QiniuUrlService {
  private final QiniuProperties props;

  public QiniuUrlService(QiniuProperties props) {
    this.props = props;
  }

  /**
   * If the bucket/domain has access control (e.g. private or original-protection), raw URLs may 403.
   * This method returns a signed download URL when AK/SK are available; otherwise returns the input.
   */
  public String signIfPossible(String rawUrl) {
    if (!StringUtils.hasText(rawUrl)) return rawUrl;
    if (!StringUtils.hasText(props.getAccessKey()) || !StringUtils.hasText(props.getSecretKey())) return rawUrl;
    try {
      String u = toCanonicalPublicUrl(rawUrl);
      var auth = Auth.create(props.getAccessKey(), props.getSecretKey());
      // 1 hour
      return auth.privateDownloadUrl(u, 3600);
    } catch (Exception e) {
      return rawUrl;
    }
  }

  private String toCanonicalPublicUrl(String rawUrl) throws URISyntaxException {
    String trimmed = rawUrl.trim();
    String base = StringUtils.hasText(props.getDomain()) ? normalizeDomain(props.getDomain()) : "";
    base = forceHttpIfLikelyCertIssue(base);

    // If we have a configured base domain, rebuild URL using the key path so old/wrong domains won't 404.
    if (StringUtils.hasText(base)) {
      URI in = new URI(trimmed);
      String path = in.getRawPath(); // e.g. /square/xxx.png
      if (StringUtils.hasText(path) && path.startsWith("/")) {
        return base + path; // drop any existing query, will be re-signed
      }
    }

    // Fallback: keep original, but downgrade https->http for hd-bkt domains if needed
    return forceHttpIfLikelyCertIssue(trimmed);
  }

  private String normalizeDomain(String domain) {
    String d = domain.trim();
    if (d.endsWith("/")) d = d.substring(0, d.length() - 1);
    return d;
  }

  private String forceHttpIfLikelyCertIssue(String url) {
    // Some hd-bkt.clouddn.com endpoints may present certificate CN mismatch on HTTPS in dev.
    // Downgrade to http so the browser can load it without TLS validation.
    if (!StringUtils.hasText(url)) return url;
    String u = url.trim();
    if (u.startsWith("https://") && u.contains(".hd-bkt.clouddn.com/")) {
      return "http://" + u.substring("https://".length());
    }
    return u;
  }
}

