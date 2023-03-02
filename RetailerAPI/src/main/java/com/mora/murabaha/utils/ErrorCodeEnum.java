package com.mora.murabaha.utils;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.Record;

public enum ErrorCodeEnum {
	ERR_90000(90000, "Invalid parameters please give valid details."),
	ERR_90001(90001, "No Records Found"),
	ERR_90002(90002, "Invalid Password"),
	ERR_90003(90003, "Password update failed"),
	ERR_90004(90004, "Record not inserted"),
	ERR_90005(90005, "UserId already exist"),
	ERR_90006(90006, "Phone number not found"),
	ERR_90007(90007, "Invalid OTP");

    public static final String ERROR_CODE_KEY = "dbpErrCode";
    public static final String ERROR_MESSAGE_KEY = "dbpErrMsg";
    public static final String OPSTATUS_CODE = "opstatus";
    public static final String HTTPSTATUS_CODE = "httpStatusCode";
    public static final String ERROR_DETAILS = "errorDetails";
    private int errorCode;
    private String message;

    private ErrorCodeEnum(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return this.message;
    }

    public String getErrorCodeAsString() {
        return String.valueOf(this.errorCode);
    }

    public Integer getErrorCodeAsInt() {
        return this.errorCode;
    }

    public String getMessage(String ... params) {
        return String.format(this.message, params);
    }

    public Result setErrorCode(Result result) {
        if (result == null) {
            result = new Result();
        }
        result.addParam(new Param(ERROR_CODE_KEY, this.getErrorCodeAsString(), "int"));
        result.addParam(new Param(ERROR_MESSAGE_KEY, this.getMessage(), "string"));
        result.addParam(new Param(OPSTATUS_CODE, "0", "int"));
        result.addParam(new Param(HTTPSTATUS_CODE, "0", "int"));
        return result;
    }

    public Record setErrorCode(Record record) {
        if (record == null) {
            record = new Record();
        }
        record.addParam(new Param(ERROR_CODE_KEY, this.getErrorCodeAsString(), "int"));
        record.addParam(new Param(ERROR_MESSAGE_KEY, this.getMessage(), "string"));
        record.addParam(new Param(OPSTATUS_CODE, "0", "int"));
        record.addParam(new Param(HTTPSTATUS_CODE, "0", "int"));
        return record;
    }

    public Result setErrorCode(Result result, String errorMessage) {
        if (result == null) {
            result = new Result();
        }
        if (StringUtils.isBlank((CharSequence)errorMessage)) {
            errorMessage = this.message;
        }
        result.addParam(new Param(ERROR_CODE_KEY, this.getErrorCodeAsString(), "int"));
        result.addParam(new Param(ERROR_MESSAGE_KEY, errorMessage, "string"));
        result.addParam(new Param(OPSTATUS_CODE, "0", "int"));
        result.addParam(new Param(HTTPSTATUS_CODE, "0", "int"));
        return result;
    }

    public Record setErrorCode(Record record, String errorMessage) {
        if (record == null) {
            record = new Record();
        }
        record.addParam(new Param(ERROR_CODE_KEY, this.getErrorCodeAsString(), "int"));
        record.addParam(new Param(ERROR_MESSAGE_KEY, errorMessage, "string"));
        record.addParam(new Param(OPSTATUS_CODE, "0", "int"));
        record.addParam(new Param(HTTPSTATUS_CODE, "0", "int"));
        return record;
    }

    public Result setErrorCode(Result result, String errorCode, String errorMessage) {
        if (result == null) {
            result = new Result();
        }
        result.addParam(new Param(ERROR_CODE_KEY, errorCode, "int"));
        result.addParam(new Param(ERROR_MESSAGE_KEY, errorMessage, "string"));
        result.addParam(new Param(OPSTATUS_CODE, "0", "int"));
        result.addParam(new Param(HTTPSTATUS_CODE, "0", "int"));
        return result;
    }

    public Result setErrorCodewithErrorDetails(Result result, String errorMessage, String errorDetails) {
        if (result == null) {
            result = new Result();
        }
        result.addParam(new Param(ERROR_CODE_KEY, this.getErrorCodeAsString(), "int"));
        result.addParam(new Param(ERROR_MESSAGE_KEY, errorMessage, "string"));
        result.addParam(new Param(ERROR_DETAILS, errorDetails, "string"));
        result.addParam(new Param(OPSTATUS_CODE, "0", "int"));
        result.addParam(new Param(HTTPSTATUS_CODE, "0", "int"));
        return result;
    }

    public JsonObject setErrorCode(JsonObject result) {
        if (result == null) {
            result = new JsonObject();
        }
        result.addProperty(ERROR_CODE_KEY, (Number)this.getErrorCodeAsInt());
        result.addProperty(ERROR_MESSAGE_KEY, this.getMessage());
        result.addProperty(OPSTATUS_CODE, (Number)Integer.parseInt("0"));
        result.addProperty(HTTPSTATUS_CODE, (Number)Integer.parseInt("0"));
        return result;
    }

    public JsonObject setErrorCode(JsonObject result, String message) {
        if (result == null) {
            result = new JsonObject();
        }
        result.addProperty(ERROR_CODE_KEY, (Number)this.getErrorCodeAsInt());
        result.addProperty(ERROR_MESSAGE_KEY, message);
        result.addProperty(OPSTATUS_CODE, (Number)Integer.parseInt("0"));
        result.addProperty(HTTPSTATUS_CODE, (Number)Integer.parseInt("0"));
        return result;
    }

    public Result updateResultObject(Result result) {
        if (result == null) {
            return this.constructResultObject();
        }
        return this.addAttributesToResultObject(result);
    }

    public Result constructResultObject() {
        Result result = new Result();
        return this.addAttributesToResultObject(result);
    }

    private Result addAttributesToResultObject(Result result) {
        result.addParam(new Param(ERROR_CODE_KEY, this.getErrorCodeAsString()));
        result.addParam(new Param(ERROR_MESSAGE_KEY, this.message));
        return result;
    }
}
