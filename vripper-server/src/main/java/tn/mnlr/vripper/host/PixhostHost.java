package tn.mnlr.vripper.host;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.protocol.HttpClientContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import tn.mnlr.vripper.exception.HostException;
import tn.mnlr.vripper.exception.XpathException;
import tn.mnlr.vripper.services.HostService;
import tn.mnlr.vripper.services.XpathService;

@Service
@Slf4j
public class PixhostHost extends Host {

  private static final String host = "pixhost.to";
  private static final String lookup = "pixhost.to/show";
  private static final String IMG_XPATH = "//img[@id='image']";

  private final HostService hostService;
  private final XpathService xpathService;

  @Autowired
  public PixhostHost(HostService hostService, XpathService xpathService) {
    this.hostService = hostService;
    this.xpathService = xpathService;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public String getLookup() {
    return lookup;
  }

  @Override
  public HostService.NameUrl getNameAndUrl(final String url, final HttpClientContext context)
      throws HostException {

    Document doc = hostService.getResponse(url, context).getDocument();

    Node imgNode;
    try {
      log.debug(String.format("Looking for xpath expression %s in %s", IMG_XPATH, url));
      imgNode = xpathService.getAsNode(doc, IMG_XPATH);
    } catch (XpathException e) {
      throw new HostException(e);
    }

    try {
      log.debug(String.format("Resolving name and image url for %s", url));
      String imgTitle = imgNode.getAttributes().getNamedItem("alt").getTextContent().trim();
      String imgUrl = imgNode.getAttributes().getNamedItem("src").getTextContent().trim();

      return new HostService.NameUrl(imgTitle.substring(imgTitle.indexOf('_') + 1), imgUrl);
    } catch (Exception e) {
      throw new HostException("Unexpected error occurred", e);
    }
  }
}
