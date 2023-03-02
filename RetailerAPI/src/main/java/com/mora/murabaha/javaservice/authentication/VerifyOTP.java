package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.kony.dbputilities.util.HelperMethods;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.mora.murabaha.utils.ErrorCodeEnum;

public class VerifyOTP implements JavaService2{

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		Map<String, String> inputParams = HelperMethods.getInputParamMap((Object[])inputArray);
		if(preProcess(inputParams, request)) {
			HashMap<String, Object> requestparam = new HashMap<String, Object>();
			requestparam.put("$filter", inputParams.get("filter"));
			result = DBPServiceExecutorBuilder.builder()
						.withServiceId("RetailerDBService")
						.withOperationId("dbxdb_OTP_get")
						.withRequestParameters(requestparam)
						.build().getResult();
			result = this.postProcess(inputParams, request, result);
		}
		return result;
	}
		
	private boolean preProcess(Map<String, String> inputParams, DataControllerRequest dcRequest) {
        String securityKey = inputParams.get("securityKey");
        if (StringUtils.isBlank((CharSequence)securityKey)) {
            return false;
        }
        if (StringUtils.isBlank((CharSequence)securityKey)) {
            securityKey = dcRequest.getParameter("securityKey");
        }
        String filter = "";
        if (StringUtils.isNotBlank((CharSequence)securityKey)) {
            filter = filter + "securityKey eq " + securityKey;
        }
        inputParams.put("filter", filter);
        return true;
    }
	
	private Result postProcess(Map<String, String> inputParams, DataControllerRequest request, Result result) {
		String Otp = HelperMethods.getFieldValue((Result)result, (String)"Otp");
		if((Otp.equals(request.getParameter("OTP")) || Otp.equals(request.getParameter("Otp")))) {
			HelperMethods.setSuccessMsg((String)"Secure Access Code is verified", (Result)result);
			result.addParam(new Param("isOtpVerified", "true", "String"));
		} else {
			ErrorCodeEnum.ERR_90007.setErrorCode(result);
		}
		result.removeDatasetById("OTP");
		return result;
	}
}
