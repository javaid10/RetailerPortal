package com.mora.murabaha.javaservice.authentication;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.kony.dbputilities.exceptions.HttpCallException;
import com.kony.dbputilities.util.HelperMethods;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.ResultToJSON;
import com.mora.murabaha.utils.ErrorCodeEnum;

public class RequestOTP implements JavaService2{
	private static final Logger logger = LogManager.getLogger(RequestOTP.class);
	
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		String userId = null;
		if (!this.validate(request)) {
            result = new Result();
            ErrorCodeEnum.ERR_90006.setErrorCode(result);
            return result;
        }
		String phone = request.getParameter("Phone");
		int invalidAttempt = 0,retryCount = 0;
		Result Otpresult = this.getOTPResult(request);
		String securityKey = null;
        if (HelperMethods.hasRecords((Result)Otpresult)) {
            if (StringUtils.isNotBlank((CharSequence)HelperMethods.getFieldValue((Result)Otpresult, (String)"InvalidAttempt"))) {
            	invalidAttempt = Integer.parseInt(HelperMethods.getFieldValue((Result)Otpresult, (String)"InvalidAttempt"));
            }
            if (StringUtils.isNotBlank((CharSequence)HelperMethods.getFieldValue((Result)Otpresult, (String)"securityKey"))) {
            	securityKey = HelperMethods.getFieldValue((Result)Otpresult, (String)"securityKey");
            }
        }
        if(invalidAttempt >= 3) {
        	ErrorCodeEnum.ERR_90006.setErrorCode(result);
            return result;
        }
        Result otpresult = this.createOTP(request, retryCount, securityKey, userId);
        logger.error("otpresult :: "+ResultToJSON.convert(otpresult));
        String otp = HelperMethods.getParamValue((Param)otpresult.getParamByName("Otp"));
        securityKey = HelperMethods.getParamValue((Param)otpresult.getParamByName("securityKey"));
        result.addParam(new Param("securityKey", securityKey, "string"));
        result.addParam(new Param("invalidAttempt", invalidAttempt+"", "String"));
        if(StringUtils.isNotBlank(otp) && StringUtils.isNotBlank(phone)) {
        	String countryCode = phone.substring(0, 3);
        	phone = (countryCode.equalsIgnoreCase("966")) ? phone : "966"+phone;
        	HashMap<String,Object> sendSMSRequest = new HashMap<String, Object>();
        	sendSMSRequest.put("AppSid", "o6oNEqvPNkLrc6gJtYalHwKXtgVgq7");
        	sendSMSRequest.put("Body", otp);
        	sendSMSRequest.put("Recipient", phone);
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
		return result;
	}

	private boolean validate(DataControllerRequest dcRequest) {
        return StringUtils.isNotBlank((CharSequence)dcRequest.getParameter("Phone")) || StringUtils.isNotBlank((CharSequence)dcRequest.getParameter("Email"));
    }
	
	private Result getOTPResult(DataControllerRequest dcRequest) throws HttpCallException {
        String securityKey = dcRequest.getParameter("securityKey");
        String serviceKey = dcRequest.getParameter("serviceKey");
        String filter = "";
        if (StringUtils.isNotBlank((CharSequence)securityKey)) {
            filter = filter + "securityKey eq " + securityKey;
        }
        if (StringUtils.isNotBlank((CharSequence)serviceKey)) {
            if (!filter.isEmpty()) {
                filter = filter + " and ";
            }
            filter = filter + "serviceKey eq " + serviceKey;
        }
        Result result = null;
        HashMap<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("$filter", filter);
		//result = HelperMethods.callGetApi((DataControllerRequest)dcRequest, (String)filter, (Map)HelperMethods.getHeaders((DataControllerRequest)dcRequest), (String)"OTP.readRecord");
		if(StringUtils.isNotBlank(filter)) {
			try {
				result = DBPServiceExecutorBuilder.builder()
						.withServiceId("RetailerDBService")
						.withOperationId("dbxdb_OTP_get")
						.withRequestParameters(inputParams)
						.build().getResult();
			} catch (DBPApplicationException e) {
				e.printStackTrace();
			}
		}
        return result;
    }
	
	 private int generateOtp() {
        int count = 6;
        int floor = (int)Math.pow(10.0, count - 1);
        int ceil = floor * 9;
        SecureRandom rand = new SecureRandom();
        return floor + rand.nextInt(ceil);
    }
	 
	private Result createOTP(DataControllerRequest dcRequest, int retryCount, String securityKey, String userId) {
        Result result = new Result();
        int otp = this.generateOtp();
        String serviceKey = dcRequest.getParameter("serviceKey");
        HashMap<String, Object> inputParams = new HashMap<String, Object>();
        inputParams.put("securityKey", securityKey);
        if (StringUtils.isNotBlank((CharSequence)userId)) {
            inputParams.put("User_id", userId);
        }
        if (StringUtils.isNotBlank((CharSequence)dcRequest.getParameter("Phone"))) {
            inputParams.put("Phone", dcRequest.getParameter("Phone"));
        }
        if (StringUtils.isNotBlank((CharSequence)dcRequest.getParameter("Email"))) {
            inputParams.put("Email", dcRequest.getParameter("Email"));
        }
        inputParams.put("Otp", String.valueOf(otp));
        inputParams.put("OtpType", dcRequest.getParameter("OtpType"));
        inputParams.put("NumberOfRetries", "" + (retryCount + 1));
        inputParams.put("createdts", HelperMethods.getCurrentTimeStamp());
        
        String url = "dbxdb_OTP_create";
        if (StringUtils.isBlank((CharSequence)securityKey) || retryCount == -1) {
            securityKey = HelperMethods.getNewId();
            inputParams.put("serviceKey", serviceKey);
            inputParams.put("InvalidAttempt", 0);
            inputParams.put("NumberOfRetries", 0);
        } else {
            url = "dbxdb_OTP_update";
        }
        inputParams.put("securityKey", securityKey);
        try {
            //result = HelperMethods.callApi((DataControllerRequest)dcRequest, inputParams, (Map)HelperMethods.getHeaders((DataControllerRequest)dcRequest), (String)url);
        	result = DBPServiceExecutorBuilder.builder()
							.withServiceId("RetailerDBService")
							.withOperationId(url)
							.withRequestParameters(inputParams)
							.build().getResult();
        }
        catch (DBPApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (!HelperMethods.hasError((Result)result)) {
            result = this.postProcess(securityKey, otp, dcRequest, userId);
        }
        return result;
    }
	
	private Result postProcess(String securityKey, int otp, DataControllerRequest dcRequest, String userId) {
        Result result = new Result();
        Param sKeyParam = new Param("securityKey", securityKey, "string");
        Param otpParam = new Param("Otp", String.valueOf(otp), "string");
        result.addParam(sKeyParam);
        result.addParam(otpParam);
        return result;
    }
}
