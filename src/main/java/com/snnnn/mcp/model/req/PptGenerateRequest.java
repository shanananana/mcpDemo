package com.snnnn.mcp.model.req;

import java.util.List;

public class PptGenerateRequest {

    // 演示文稿标题（用于文件名）
    private String title;

    // 幻灯片内容，简单起见每页只包含一个标题与一个正文
    public static class SlideItem {
        private String heading;
        private String text;

        public String getHeading() { return heading; }
        public void setHeading(String heading) { this.heading = heading; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    private List<SlideItem> slides;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<SlideItem> getSlides() { return slides; }
    public void setSlides(List<SlideItem> slides) { this.slides = slides; }
}


