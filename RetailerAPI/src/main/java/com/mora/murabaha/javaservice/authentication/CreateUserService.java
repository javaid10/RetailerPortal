package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kony.dbputilities.util.HelperMethods;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.ResultToJSON;
import com.mora.murabaha.utils.ErrorCodeEnum;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class CreateUserService implements JavaService2{

	private static final Logger logger = LogManager.getLogger(CreateUserService.class);
			
	@SuppressWarnings("unchecked")
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result(); 
		if(preprocess(request, response)) {
			String userId = generateUserID(request);
//			HashMap<String, Object> getinput = new HashMap<String, Object>();
//			getinput.put("$filter", "UserId eq " + request.getParameter("userid"));
//			String res = DBPServiceExecutorBuilder.builder()
//							.withServiceId("RetailerDBService")
//							.withOperationId("dbxdb_retailer_get")
//							.withRequestParameters(getinput)
//							.build().getResponse();
//			logger.error("Response :: "+res);
//			JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
//			logger.error("Size :: "+retailerResponse.getAsJsonArray("retailer").size());
			
//			if(retailerResponse.getAsJsonArray("retailer").size() == 0) {
				HashMap<String, Object> params = (HashMap<String, Object>)inputArray[1];
				HashMap<String, Object> input = new HashMap<String, Object>();
				input.put("UserId", userId);
				input.put("UserName", params.get("username"));
				input.put("RetailerName", params.get("retailername"));
				input.put("Role", params.get("role"));
				input.put("PhoneNo", params.get("phonenumber"));
				input.put("EmailId", params.get("email"));
				input.put("RetailerId", params.get("retailerid"));
				String password = generateActivationCode();
				String hashedPwd = BCrypt.hashpw(password, BCrypt.gensalt());
				input.put("TempPassword", hashedPwd);
				input.put("Status", "SID_CUS_NEW");
				String dbresponse = DBPServiceExecutorBuilder.builder()
									.withServiceId("RetailerDBService")
									.withOperationId("dbxdb_retailer_create")
									.withRequestParameters(input)
									.build().getResponse();
				JsonObject jsonResponse = new JsonParser().parse(dbresponse).getAsJsonObject();
				if(jsonResponse.getAsJsonArray("retailer").size() != 0) {
					result = JSONToResult.convert(jsonResponse.toString());
					result.addParam("id",userId);
					result.addParam("pws", password);
					sendUserIdPassword(request, password, userId);
				} else {
					ErrorCodeEnum.ERR_90004.setErrorCode(result);
				}
//			} else {
//				ErrorCodeEnum.ERR_90005.setErrorCode(result);
//			}
		} else {
			ErrorCodeEnum.ERR_90000.setErrorCode(result);
		}
		return result;
	}

	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		boolean status = true;
		String username = "",role = "", phonenumber = "", email = "", userid = "";
		username = request.getParameter("username");
		role = request.getParameter("role");
		phonenumber = request.getParameter("phonenumber");
		email = request.getParameter("email");
		//userid = request.getParameter("userid");
		if(StringUtils.isBlank(username) || StringUtils.isBlank(role) || StringUtils.isBlank(phonenumber) || StringUtils.isBlank(email)) {
			status = false;
		}
		return status;
	}

	private String generateActivationCode() {
		StringBuilder sb = new StringBuilder(8);
		String alphaNumbericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
		for (int i = 0; i < 8; i++) {
			int index = (int) (alphaNumbericString.length() * Math.random());
			sb.append(alphaNumbericString.charAt(index));
		}
		String Password = sb.toString();
		return Password;
	}
	
	@SuppressWarnings("unchecked")
	private String generateUserID(DataControllerRequest request) throws DBPApplicationException {
		boolean isUserFound = false;
		HashMap<String, Object> input = new HashMap<String, Object>();
		logger.error(request.getParameter("username").toString());
		String userId = request.getParameter("username").toString();
		userId = userId.replaceAll("\\s", "");
		String query = "startswith('UserId','"+userId+"') eq true";
		input.put("$filter", query);
		input.put("$select", "UserId");
		String res = DBPServiceExecutorBuilder.builder()
				.withServiceId("RetailerDBService")
				.withOperationId("dbxdb_retailer_get")
				.withRequestParameters(input)
				.build().getResponse();
		logger.error("Response :: "+res);
		JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
		JsonArray userIDList = retailerResponse.get("retailer").getAsJsonArray();
		logger.error("userIDList size :: "+userIDList.size());
		/*
		String numeric = "0123456789";
		String specialchars = "@_#";
		String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder sb = null;
		//while(!isUserFound) {
			sb = new StringBuilder();
			sb.append(request.getParameter("username"));
			logger.error("While inside :: ");
			int index = (int) (numeric.length() * Math.random());
			sb.append(numeric.charAt(index));
			int index1 = (int) (specialchars.length() * Math.random());
			sb.append(specialchars.charAt(index1));
			int index2 = (int) (alpha.length() * Math.random());
			sb.append(alpha.charAt(index2));
			String userId = sb.toString();
			logger.error("userId :: "+userId);
			if(!userIDList.containsValue(userId)) {				
				isUserFound = true;
			}
		//}
		 * 
		 */
		String newUserId = (userIDList.size() == 0) ? userId : userId+(userIDList.size()+1)+"";
		logger.error("newUserId :: "+newUserId);
		return newUserId;
	}
	
	private void sendUserIdPassword(DataControllerRequest request,String Password, String userId) throws DBPApplicationException {
		String content = "User ID :: "+userId+" Password :: "+Password;
		logger.error("content :: "+content);
		HashMap<String,Object> sendSMSRequest = new HashMap<String, Object>();
    	sendSMSRequest.put("AppSid", "o6oNEqvPNkLrc6gJtYalHwKXtgVgq7");
    	sendSMSRequest.put("Body", content);
    	sendSMSRequest.put("Phone", "966"+request.getParameter("phonenumber"));
    	sendSMSRequest.put("Recipient", "966"+request.getParameter("phonenumber"));
    	sendSMSRequest.put("SenderID", "MORAFinance");
    	sendSMSRequest.put("responseType", "JSON");
    	sendSMSRequest.put("statusCallback", "sent");
    	sendSMSRequest.put("baseEncode", "true");
    	sendSMSRequest.put("async", "false");
    	sendSMSRequest.put("CorrelationID", "242343424234");
    	
    	logger.error("Input param :: "+sendSMSRequest.entrySet());
    	Result smsresult = DBPServiceExecutorBuilder.builder()
				.withServiceId("UniphonicRestAPIMurabaha")
				.withOperationId("SendMessage")
				.withRequestParameters(sendSMSRequest)
				.build().getResult();
    	logger.error("smsresult :: "+ResultToJSON.convert(smsresult));
	
    	String email = request.getParameter("email");
    	logger.error("Input param :: email "+email);
        if (StringUtils.isNotBlank((CharSequence)content) && StringUtils.isNotBlank((CharSequence)email)) {
        	Map headers = HelperMethods.getHeaders((DataControllerRequest)request);
        	HashMap<String, String> input = new HashMap();
            input.put("Subscribe", "true");
            input.put("FirstName", "firstName");
            input.put("LastName", "lastName");
            String emailContent = content;
            input.put("emailBody", emailContent);
            input.put("emailSubject", "Ijarah");
            if (StringUtils.isNotBlank((CharSequence)emailContent)) {
                input.put("Email", request.getParameter("email"));
                headers = HelperMethods.getHeaders((DataControllerRequest)request);
                headers.put("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
                HelperMethods.callApiAsync((DataControllerRequest)request, input, (Map)headers, (String)"KMS.sendEmailOrch");
            }
        }        
	}	
}
