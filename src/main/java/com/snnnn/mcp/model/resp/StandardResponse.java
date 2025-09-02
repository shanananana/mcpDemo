package com.snnnn.mcp.model.resp;

import java.io.Serializable;

public class StandardResponse<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private String status;
	private String message;
	private T data;

	public StandardResponse() {}

	public StandardResponse(String status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public static <T> StandardResponse<T> success(T data) {
		return new StandardResponse<>("success", "", data);
	}

	public static <T> StandardResponse<T> error(String message) {
		return new StandardResponse<>("error", message, null);
	}

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public T getData() { return data; }
	public void setData(T data) { this.data = data; }
}


