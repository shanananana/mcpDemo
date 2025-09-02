package com.snnnn.mcp.model.resp;

import java.io.Serializable;

public class FileIdResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fileId;

	public FileIdResponse() {}

	public FileIdResponse(String fileId) {
		this.fileId = fileId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
}


