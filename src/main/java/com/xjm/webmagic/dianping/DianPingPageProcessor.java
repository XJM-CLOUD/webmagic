package com.xjm.webmagic.dianping;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大众点评爬虫
 *
 * @author xiangjunming
 * @date 2019/07/30 16:58:16
 */
@Log4j
public class DianPingPageProcessor implements PageProcessor {

    private static final Map<String, Integer> keywordMap = new HashMap<>();

    static {
        keywordMap.put("成都", 8);
        keywordMap.put("广安", 250);
        keywordMap.put("重庆", 9);
        keywordMap.put("桂林", 226);
        keywordMap.put("玉林", 232);
        keywordMap.put("南宁", 224);
        keywordMap.put("贵港", 231);
    }

    /**
     * 其他页标识
     */
    public static final String ChildPage = ".*?/p[d]{1}?\\?.*?";

    public DianPingPageProcessor(String keyword, String key) {
        this.keyword = keyword;
        this.key = key;

        String cookieStr = "s_ViewType=10; _lxsdk_cuid=16c837b3a9bc8-0878432115c33f-7a1437-1fa400-16c837b3a9bc8; _lxsdk=16c837b3a9bc8-0878432115c33f-7a1437-1fa400-16c837b3a9bc8; _hc.v=5e049a4b-4a3e-0893-d170-85d57850e568.1565573987; Hm_lvt_dbeeb675516927da776beeb1d9802bd4=1565573999; Hm_lpvt_dbeeb675516927da776beeb1d9802bd4=1565573999; _lxsdk_s=16c837b38e1-7c5-5cf-5c9%7C%7C44";

        String[] cookies = cookieStr.split("; ");
        for(String cookie : cookies){
            String[] keyValue = cookie.split("=");
            site.addCookie(keyValue[0], keyValue[1]);
        }
    }

    /**
     * 城市
     */
    private String keyword;

    /**
     * 搜索关键字
     */
    private String key;

    /**
     * url
     */
    private String url;

    private Integer complatePage = 1;

    /**
     * 设置爬虫Cookie参数
     */
    private Site site = Site.me().setRetryTimes(3).setSleepTime(3000)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");

    /**
     * 导出数据
     */
    private static List<DianPingInfo> infos = new ArrayList<>();

    @Override
    public void process(Page page) {

        if (!page.getUrl().regex(ChildPage).match()) {
            //最大页码
//            Integer maxPage = Integer.valueOf(page.getHtml().xpath("//div[@class=page]/a[10]/text()").get());
            Integer maxPage = 10;
            for (int i = 2; i <= maxPage; i++) {
                //第一页添加其他页
                page.addTargetRequest(url + "/p" + i + "?aid=132750127%2C19437032&cpt=132750127%2C19437032");
            }
        }

        try {

            //详情页地址
            List<String> detailLinks = page.getHtml().xpath("//div[@class=tit]/a[1]/@href").all();

            //创建详情页面爬虫
            Spider.create(new PageProcessor() {
                @Override
                public void process(Page page) {
                    String title = page.getHtml().xpath("//div[@class=shop-name]/h1/text()").get();
                    String address = null;
                    String mobile = null;
                    String mobile2 = null;
                    if(key.contains("婚纱")){
                        address = page.getHtml().xpath("//div[@class=shop-addr]/div[1]/span[1]/@title").get();
                        mobile = page.getHtml().xpath("//div[@class=shop-contact]/span[1]/text()").get();
                    }else{
                        //驾校 教育
                        address = page.getHtml().xpath("//div[@class=address]/text()").get();
                        mobile = page.getHtml().xpath("//div[@class=phone]/span[1]/@data-phone").get();
                        mobile2 = page.getHtml().xpath("//div[@class=phone]/span[3]/@data-phone").get();
                    }
                    DianPingInfo info = new DianPingInfo(title, address, mobile, mobile2);
                    infos.add(info);
                }

                @Override
                public Site getSite() {
                    return site;
                }
            }).addUrl(detailLinks.toArray(new String[detailLinks.size()])).thread(5).run();
            log.info("已完成处理：" + complatePage++ + "页");
        } catch (Exception e) {
            log.error(e);
        } finally {

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
        String encodeKey = null;
        try {
            encodeKey = URLEncoder.encode(this.key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url = "https://www.dianping.com/search/keyword/" + keywordMap.get(keyword) + "/0_" + encodeKey;
        //创建爬虫执行
        Spider.create(this).addUrl(url).thread(5).run();

        OutputStream out = null;
        try {
            out = new FileOutputStream("/dianping_" + keyword + "_" + key + ".xlsx");
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
            Sheet sheet1 = new Sheet(1, 0, DianPingInfo.class);
            sheet1.setSheetName("sheet1");
            writer.write(infos, sheet1);
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

    public static void main(String[] args) {
        new DianPingPageProcessor("桂林", "装修公司").start();
    }
}
