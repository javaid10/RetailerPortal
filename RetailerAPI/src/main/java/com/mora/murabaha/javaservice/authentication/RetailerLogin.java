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
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.mora.murabaha.utils.ErrorCodeEnum;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class RetailerLogin implements JavaService2 {
	
	private static final Logger logger = LogManager.getLogger(RetailerLogin.class);
			
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		logger.error("Retailer Login");
		Record userAttrRecord = new Record();
		if(preprocess(request, response)) { 
			HashMap<String, Object> input = new HashMap<String, Object>();
			input.put("$filter", "UserId eq " + request.getParameter("UserName"));
			String res = DBPServiceExecutorBuilder.builder()
							.withServiceId("RetailerDBService")
							.withOperationId("dbxdb_retailer_get")
							.withRequestParameters(input)
							.build().getResponse();
			logger.error("Response :: "+res);
			JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
			logger.error("Size :: "+retailerResponse.getAsJsonArray("retailer").size());
			if(retailerResponse.getAsJsonArray("retailer").size() != 0) {
				JsonObject retaileruser = retailerResponse.getAsJsonArray("retailer").get(0).getAsJsonObject();
				if(retaileruser.has("Status") && retaileruser.get("Status").getAsString().equalsIgnoreCase("SID_INACTIVE")) {
					ErrorCodeEnum.ERR_90001.setErrorCode(result);
					return result;
				}
				String password = null;
				if(retaileruser.has("Password")) {
					password = retaileruser.get("Password").getAsString();
				}
				if(StringUtils.isBlank(password)) {
					password = retaileruser.get("TempPassword").getAsString();
				}
				if(validatePassword(password, request.getParameter("Password"))) {
					Record securityAttrRecord = new Record();
					securityAttrRecord.setId("security_attributes");
					//generate session token
					String sessionToken = BCrypt.hashpw(retaileruser.get("UserId").getAsString(), BCrypt.gensalt());
					securityAttrRecord.addParam(new Param("session_token", sessionToken));
					
					String userId = retailerResponse.getAsJsonArray("retailer").get(0).getAsJsonObject().get("UserId").getAsString();
					userAttrRecord.setId("user_attributes");
					userAttrRecord.addParam(new Param("user_id", userId));
					userAttrRecord.addParam(new Param("username", retaileruser.get("UserName").getAsString()));
					userAttrRecord.addParam(new Param("role", retaileruser.get("Role").getAsString()));
					userAttrRecord.addParam(new Param("phoneno", retaileruser.get("PhoneNo").getAsString()));
					userAttrRecord.addParam(new Param("email", retaileruser.get("EmailId").getAsString()));
					userAttrRecord.addParam(new Param("status", retaileruser.get("Status").getAsString()));
					userAttrRecord.addParam(new Param("retailerid", retaileruser.get("RetailerId").getAsString()));
					userAttrRecord.addParam(new Param("retailername", retaileruser.get("RetailerName").getAsString()));
					result.addRecord(securityAttrRecord);
					result.addRecord(userAttrRecord);
				} else {
					ErrorCodeEnum.ERR_90002.setErrorCode(result);
				}
			} else {
				ErrorCodeEnum.ERR_90001.setErrorCode(result);
			}
		} else {
			ErrorCodeEnum.ERR_90000.setErrorCode(result);
		}
//		userAttrRecord.setId("user_attributes");
//		userAttrRecord.addParam(new Param("user_id", request.getParameter("UserName")));
//		result.addRecord(userAttrRecord);
		return result;
	}
	
	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		boolean status = true;
		String username = request.getParameter("UserName");
		String password = request.getParameter("Password");
		if(username.isEmpty() || password.isEmpty()) {
			return false;
		}
		return status;
	}
	
	private Boolean validatePassword(String dbPassword, String currentPassword) throws Exception {
		boolean isPasswordValid = false;
		try {
			isPasswordValid = BCrypt.checkpw(currentPassword, dbPassword);
		} catch (Exception exception) {
			logger.error("Error in validating password", exception);
			throw exception;
		}
		logger.debug(
				(new StringBuilder()).append("Response from isPasswordValid  : ").append(isPasswordValid).toString());
		return Boolean.valueOf(isPasswordValid);
	}
}
