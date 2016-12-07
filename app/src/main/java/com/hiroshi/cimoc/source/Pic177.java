package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/10/5.
 */

public class Pic177 extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://www.177pic66.com/page/%d?s=%s", page, keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#content > div.post_box")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("div.tit > h2 > a", "href").substring(29);
                String title = node.text("div.tit > h2 > a");
                String author = StringUtils.match("(\\[中文\\])?\\[(.*?)\\]", title, 2);
                if (author != null) {
                    title = title.replaceFirst("(\\[中文\\])?\\[(.*?)\\]\\s*", "");
                }
                String cover = node.attr("div.c-con > a > img", "src");
                String update = node.text("div.c-top > div.datetime").replace(" ", "-");
                return new Comic(SourceManager.SOURCE_177PIC, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.177pic66.com/html/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#content > div.post > div.c-top2 > div.tit > h1");
        String author = StringUtils.match("(\\[中文\\])?\\[(.*?)\\]", title, 2);
        if (author != null) {
            title = title.replaceFirst("(\\[中文\\])?\\[(.*?)\\]\\s*", "");
        }
        String cover = body.attr("#content > div.post > div.entry-content > p > img", "src");
        String update = body.text("#content > div.post > div.c-top2 > div.datetime");
        comic.setInfo(title, cover, update, null, author, true);

        return null;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int count = body.list("#single-navi span.single-navi").size();
        for (int i = count; i > 0; --i) {
            list.add(new Chapter("Ch" + i, String.valueOf(i)));
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = "http://www.177pic66.com/page/" + page;
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.conter > div.main > div.post_box")) {
            String cid = node.attr("div.tit > h2 > a", "href").substring(29);
            String title = node.text("div.tit > h2 > a");
            String author = StringUtils.match("(\\[中文\\])?\\[(.*?)\\]", title, 2);
            if (author != null) {
                title = title.replaceFirst("(\\[中文\\])?\\[(.*?)\\]\\s*", "");
            }
            String cover = node.attr("div.c-con > a > img", "src");
            String update = node.text("div.c-top > div.datetime").replace(" ", "-");
            list.add(new Comic(SourceManager.SOURCE_177PIC, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://www.177pic66.com/html/%s/%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int count = 0;
        for (Node node : body.list("#content > div.post > div.entry-content > p > img")) {
            list.add(new ImageUrl(++count, node.attr("src"), false));
        }
        return list;
    }

}
