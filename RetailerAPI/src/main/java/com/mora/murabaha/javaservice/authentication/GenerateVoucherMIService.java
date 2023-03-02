package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Result;

public class GenerateVoucherMIService implements JavaService2 {

	private static final Logger logger = LogManager.getLogger(GenerateVoucherMIService.class);
			
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		if(preprocess(request, response)) {
			HashMap<String, Object> params = (HashMap<String, Object>) inputArray[1];
			HashMap<String, Object> getinput = new HashMap<String, Object>();
			getinput.put("mobileno", params.get("mobile").toString());
			getinput.put("startdate", params.get("startdate").toString());
			getinput.put("enddate", params.get("enddate").toString());
			String res = DBPServiceExecutorBuilder.builder()
							.withServiceId("RetailerDBService")
							.withOperationId("dbxdb_sp_get_vouchermi")
							.withRequestParameters(getinput)
							.build().getResponse();
			logger.error("Response :: "+res);
			JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
			result = JSONToResult.convert(retailerResponse.toString());
		}
		return result;
	}

	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		String startDate = request.getParameter("startDate");
		String expiryDate = request.getParameter("expiryDate");
		String status = request.getParameter("status");
		String mobile = request.getParameter("mobile");
		if(StringUtils.isNotBlank(mobile) && StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(expiryDate) && StringUtils.isNotBlank(status)) {
			return true;
		}
		return false;
	}

}
