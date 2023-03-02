package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.ResultToJSON;
import com.mora.murabaha.utils.ErrorCodeEnum;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class ResendUserPasswordService implements JavaService2{

	private static final Logger logger = LogManager.getLogger(ResendUserPasswordService.class);
			
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
				String phoneno = retailerResponse.getAsJsonArray("retailer").get(0).getAsJsonObject().get("PhoneNo").getAsString(); 
				String password = request.getParameter("resetpassword");
				String salt = BCrypt.gensalt((int)11);
		        String hashedPassword = BCrypt.hashpw((String)password, (String)salt);
		        
				HashMap<String, Object> inputParam = new HashMap<String, Object>();
				inputParam.put("Password", hashedPassword);
				inputParam.put("TempPassword", null);
				inputParam.put("UserId", userId);
				inputParam.put("Status", "SID_RESET_PASSWORD");
				String updatePassword = DBPServiceExecutorBuilder.builder()
												.withServiceId("RetailerDBService")
												.withOperationId("dbxdb_retailer_update")
												.withRequestParameters(inputParam)
												.build().getResponse();
				JsonObject retailerPwdUpdateResponse = new JsonParser().parse(updatePassword).getAsJsonObject();
				logger.error("retailerPwdUpdateResponse :: "+retailerPwdUpdateResponse);
				if(retailerPwdUpdateResponse.getAsJsonArray("retailer").size() != 0) {
					result.addParam("status", "sucess");
					result.addParam("password",password);
					sendUserIdPassword(request, password, userId, phoneno);
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
	
	private void sendUserIdPassword(DataControllerRequest request,String Password, String userId,String phoneno) throws DBPApplicationException {
		String content = "User ID :: "+userId+" Password :: "+Password;
		logger.error("content :: "+content);
		HashMap<String,Object> sendSMSRequest = new HashMap<String, Object>();
    	sendSMSRequest.put("AppSid", "o6oNEqvPNkLrc6gJtYalHwKXtgVgq7");
    	sendSMSRequest.put("Body", content);
    	sendSMSRequest.put("Recipient", "966"+phoneno);
    	sendSMSRequest.put("SenderID", "MORAFinance");
    	sendSMSRequest.put("responseType", "JSON");
    	sendSMSRequest.put("statusCallback", "sent");
    	sendSMSRequest.put("baseEncode", "true");
    	sendSMSRequest.put("async", "false");
    	sendSMSRequest.put("CorrelationID", "242343424234");
    	
    	Result smsresult = DBPServiceExecutorBuilder.builder()
				.withServiceId("UniphonicRestAPIMurabaha")
				.withOperationId("SendMessage")
				.withRequestParameters(sendSMSRequest)
				.build().getResult();
    	logger.error(ResultToJSON.convert(smsresult));
	}

}
