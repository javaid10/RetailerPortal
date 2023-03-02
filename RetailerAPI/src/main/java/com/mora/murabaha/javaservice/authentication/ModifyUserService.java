package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Result;
import com.mora.murabaha.utils.ErrorCodeEnum;

public class ModifyUserService implements JavaService2 {

	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		if (preprocess(request, response)) {
			HashMap<String, Object> params = (HashMap<String, Object>) inputArray[1];
			HashMap<String, Object> input = new HashMap<String, Object>();
			if (params.containsKey("username") && !StringUtils.isBlank(params.get("username").toString())) {
				input.put("UserName", params.get("username").toString());
			}
			if (params.containsKey("role") && !StringUtils.isBlank(params.get("role").toString())) {
				input.put("Role", params.get("role").toString());
			}
			if (params.containsKey("phonenumber") && !StringUtils.isBlank(params.get("phonenumber").toString())) {
				input.put("PhoneNo", params.get("phonenumber").toString());
			}
			if (params.containsKey("email") && !StringUtils.isBlank(params.get("email").toString())) {
				input.put("EmailId", params.get("email").toString());
			}
			if (params.containsKey("status") && !StringUtils.isBlank(params.get("status").toString())) {
				input.put("Status", params.get("status").toString());
			}
			input.put("UserId", params.get("userid").toString());
			String dbresponse = DBPServiceExecutorBuilder.builder().withServiceId("RetailerDBService")
					.withOperationId("dbxdb_retailer_update").withRequestParameters(input).build().getResponse();
			JsonObject jsonResponse = new JsonParser().parse(dbresponse).getAsJsonObject();
			if (jsonResponse.getAsJsonArray("retailer").size() != 0) {
				result = JSONToResult.convert(jsonResponse.toString());
			} else {
				ErrorCodeEnum.ERR_90004.setErrorCode(result);
			}
		} else {
			ErrorCodeEnum.ERR_90000.setErrorCode(result);
		}
		return result;
	}

	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		boolean status = true;
		String userid = "";
		userid = request.getParameter("userid");
		if (StringUtils.isBlank(userid)) {
			status = false;
		}
		return status;
	}

}
