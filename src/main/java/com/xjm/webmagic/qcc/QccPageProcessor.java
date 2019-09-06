package com.xjm.webmagic.qcc;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.log4j.Log4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The type Qcc page processor.
 *
 * @author xiangjunming
 * @date 2019/07/30 16:58:16
 */
@Log4j
public class QccPageProcessor implements PageProcessor {

    /**
     * 其他页标识
     */
    public static final String SEARCH = ".*?/search_index.*?";

    /**
     * Instantiates a new Qcc page processor.
     *
     * @param key the key
     */
    public QccPageProcessor(String key) {
        this.key = key;
    }

    /**
     * 搜索关键字
     */
    private String key;

    /**
     * 设置爬虫Cookie参数
     */
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000)
            .addCookie("QCCSESSID", "kkcvvphvbisl00r7b2v9h9enk3")
            .addCookie("UM_distinctid", "16c8dd4a3494f4-04f68562229242-7a1437-1fa400-16c8dd4a34abdd")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");

    /**
     * 取a标签法定代表人
     */
    private static Pattern a = Pattern.compile("<a.*?>(.*)</a>");

    /**
     * 取不到a标签取text法定代表人
     */
    private static Pattern text = Pattern.compile("：(.{4})");

    /**
     * 导出数据
     */
    private static List<QccInfo> qccInfos = new ArrayList<>();

    @Override
    public void process(Page page) {
        if (!page.getUrl().regex(SEARCH).match()) {
            //第一页添加其他页
            page.addTargetRequest("https://www.qichacha.com/search_index?key=" + key + "&ajaxflag=1&p=2&");
            page.addTargetRequest("https://www.qichacha.com/search_index?key=" + key + "&ajaxflag=1&p=3&");
            page.addTargetRequest("https://www.qichacha.com/search_index?key=" + key + "&ajaxflag=1&p=4&");
            page.addTargetRequest("https://www.qichacha.com/search_index?key=" + key + "&ajaxflag=1&p=5&");
        }

        try {
            List<String> titles = page.getHtml().xpath("//td/a[@class=ma_h1]/html()").all();
            List<String> realNames = page.getHtml().xpath("//td/p[1]").regex("[\\s\\S]+[法定代表人：|经营者：]").all();
            final List<String> mobiles = page.getHtml().xpath("//td/p[2]/span[@class=m-l]/text()").all();
            AtomicInteger i = new AtomicInteger();
            List<QccInfo> ret = titles.stream().map(title -> {
                title = title.replace("<em>", "").replace("</em>", "").trim();
                String realName = realNames.get(i.get());
                Matcher m = a.matcher(realName);
                if (m.find()) {
                    realName = m.group(1);
                } else {
                    m = text.matcher(realName);
                    if (m.find()) {
                        realName = m.group(1);
                    }
                }
                String mobile = mobiles.get(i.get()).replace("电话：", "").trim();
                i.getAndIncrement();
                return new QccInfo(title, realName, mobile);
            }).collect(Collectors.toList());

            //打印
            qccInfos.addAll(ret);
            System.out.println("ok!");
        } catch (Exception e) {
            log.error(e);
        } finally {
//            cdl.countDown();
        }

    }

    @Override
    public Site getSite() {
        return site;
    }

    /**
     * 开始任务
     */
    public void start() {
        String key = null;
        try {
            key = URLEncoder.encode(this.key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //创建爬虫执行
        Spider.create(this).addUrl("https://www.qichacha.com/search?key=" + key).thread(5).run();

        OutputStream out = null;
        try {
            out = new FileOutputStream("/qcc-" + this.key + ".xlsx");
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
            Sheet sheet1 = new Sheet(1, 0, QccInfo.class);
            sheet1.setSheetName("sheet1");
            writer.write(qccInfos, sheet1);
            writer.finish();
            log.info("导出excel完成!");
        } catch (Exception e) {
            log.error(e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
        }

    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public static void main(String[] args) {
        //泸州，南充，乐山，内江，自贡，广安
        new QccPageProcessor("广安建材公司").start();
    }
}
