package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.murabaha.utils.ErrorCodeEnum;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class ResetPasswordService implements JavaService2{

	private static final Logger logger = LogManager.getLogger(ResetPasswordService.class);
			
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		logger.error("Reset Password Service");
		if(preprocess(request, response)) { 
			HashMap<String, Object> input = new HashMap<String, Object>();
			input.put("$filter", "UserId eq " + request.getParameter("userid"));
			String res = DBPServiceExecutorBuilder.builder()
							.withServiceId("RetailerDBService")
							.withOperationId("dbxdb_retailer_get")
							.withRequestParameters(input)
							.build().getResponse();
			logger.error("Response :: "+res);
			JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
			logger.error("Size :: "+retailerResponse.getAsJsonArray("retailer").size());
			if(retailerResponse.getAsJsonArray("retailer").size() != 0) {
				String userId = retailerResponse.getAsJsonArray("retailer").get(0).getAsJsonObject().get("UserId").getAsString();
				String password = request.getParameter("resetpassword");
				String salt = BCrypt.gensalt((int)11);
		        String hashedPassword = BCrypt.hashpw((String)password, (String)salt);
		        
				HashMap<String, Object> inputParam = new HashMap<String, Object>();
				inputParam.put("Password", hashedPassword);
				inputParam.put("TempPassword", null);
				inputParam.put("UserId", userId);
				inputParam.put("Status", "SID_ACTIVE");
				String updatePassword = DBPServiceExecutorBuilder.builder()
												.withServiceId("RetailerDBService")
												.withOperationId("dbxdb_retailer_update")
												.withRequestParameters(inputParam)
												.build().getResponse();
				JsonObject retailerPwdUpdateResponse = new JsonParser().parse(updatePassword).getAsJsonObject();
				logger.error("retailerPwdUpdateResponse :: "+retailerPwdUpdateResponse);
				if(retailerPwdUpdateResponse.getAsJsonArray("retailer").size() != 0) {
					result.addParam("status", "sucess");
				} else {
					ErrorCodeEnum.ERR_90003.setErrorCode(result);
				}
			} else {
				ErrorCodeEnum.ERR_90001.setErrorCode(result);
			}
		} else {
			ErrorCodeEnum.ERR_90000.setErrorCode(result);
		}
		return result;
	}

	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		boolean status = true;
		String username = request.getParameter("userid");
		String password = request.getParameter("resetpassword");
		if(username.isEmpty() || password.isEmpty()) {
			return false;
		}
		return status;
	}

}
