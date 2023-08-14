package com.webank.wedatasphere.dss.framework.release.restful;

import com.webank.wedatasphere.dss.framework.proxy.restful.DssProxyUserController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "/dss/framework/proxy", produces = {"application/json"})
@RestController
public class ProxyUserController extends DssProxyUserController {
}
