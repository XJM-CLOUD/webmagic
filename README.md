# webmagic
> 根据关键字爬取企查查网站相关信息(公司名称、法定代表人、电话号码)，并导出excel文件
+ 环境
    + java8
    + maven
---
+ 运行main
    + `QccPageProcessor.main()`
    + excel导出位置: 项目运行所在盘符/qcc.xlsx
---
+ cookie配置(浏览器登录后替换以下参数)
    + **QCCSESSID**
    + **UM_distinctid**
    + ```
        private Site site = Site.me().setRetryTimes(3).setSleepTime(1000)
                    .addCookie("QCCSESSID", "a4h71l1urahnetfa5r16ha5553")
                    .addCookie("UM_distinctid", "16c3b73d985cc0-0cc19b032c17de-7a1437-1fa400-16c3b73d986af8")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
      ```
---
+ 相关技术: 爬虫框架webmagic、阿里easyexcel、lombok