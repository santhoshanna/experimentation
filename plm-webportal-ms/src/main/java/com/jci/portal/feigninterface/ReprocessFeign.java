package com.jci.portal.feigninterface;

import java.util.HashMap;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="plm-reprocesiing-ms",url="http://10.109.218.6:9090")
public interface ReprocessFeign {
	
	@RequestMapping(method = RequestMethod.POST, value="/Reprocess")
	public String sendData2(@RequestBody HashMap<String, Object> hashMap );

}
