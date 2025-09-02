package com.snnnn.mcp.model.req;

import org.springframework.ai.tool.annotation.ToolParam;

import java.io.Serializable;

public class PdfGenerateHtmlRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@ToolParam(description = "PDF 标题，用于生成文件名的一部分")
	private String title;

	@ToolParam(description = "完整 HTML 内容，支持内联样式")
	private String html;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}
}


