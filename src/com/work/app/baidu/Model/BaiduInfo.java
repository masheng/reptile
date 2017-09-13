package com.work.app.baidu.Model;

/**
 * Created by ms on 2017/9/10.
 */

/*
  {
                "datatype": 1,
                "itemdata": {
                    "sname": "懒人听书",
                    "aladdin_flag": "1",
                    "docid": "22225652",
                    "groupid": "7214955",
                    "packageid": "2010699497",
                    "type": "soft",
                    "versionname": "6.1.3",
                    "versioncode": "17613",
                    "cateid": "505",
                    "size_ori": 20969375,
                    "size": "20M",
                    "download_url": "http://gdown.baidu.com/data/wisegame/2840eec84251646c/lanrentingshu_17613.apk",
                    "updatetime": "2017-09-06",
                    "icon": "http://cdn00.baidu-img.cn/timg?vsapp&size=b150_150&imgtype=3&quality=80&er&sec=0&di=2a5a4a352833f081f9dfd822fb7db90d&ref=http%3A%2F%2Fd.hiphotos.bdimg.com&src=http%3A%2F%2Fd.hiphotos.bdimg.com%2Fwisegame%2Fwh%253D512%252C512%2Fsign%3Db0278ffbfc36afc30e593760822bc7f1%2Fa9d3fd1f4134970a8326d2149ecad1c8a7865db0.jpg",
                    "download_inner": "http://gdown.baidu.com/data/wisegame/2840eec84251646c/lanrentingshu_17613.apk",
                    "signmd5": "2058188461",
                    "md5": "b95c99772840eec84251646ce753278a",
                    "firstflag": false,
                    "package": "bubei.tingshu",
                    "score": "85",
                    "score_count": "20453",
                    "all_download_ori": "4791万",
                    "platform_version": "4.0",
                    "popularity": "93",
                    "manual_short_brief": "懒人一族一族的福音，地铁公交，家务，散步，旅行甚至工作，畅游在有声的世界。",
                    "today_strDownload_ori": "1万",
                    "yesterday_download_pid": "17009",
                    "app_gift_title": "",
                    "display_download": "47911762",
                    "support_chip": "0",
                    "ishot": false,
                    "commentsnum_ori": "20453",
                    "usenum_ori": "4791万",
                    "adv_item": "source+DIS_NATURAL@boardid+34592@pos+13@flow+BOARD@trans+@oriboardid+34592@oripos+13@exp+exp1@sample+nature@package+bubei.tingshu@searchid+3308334465574089650@terminal_type+client",
                    "detail_background": "",
                    "discount_title": "优惠",
                    "inner_content": "",
                    "brief": "懒人听书是深受广大用户喜爱的移动有声阅读应用，2亿注册用户，3千万月活跃用户。 懒人听书是有声阅读领域的Kindle，去除不必要的噪音及影响，一切产品设计都为保障用户的听读体验。产品特性 1.海量资源：8000多正版书籍、无数主播节目，每日更新。 2.上传节目：所有用户都是懒人主播，可自由上传随时收听。 3.下载收听：所有书籍、节目免费。 4.交流社区：与主播和同好听友即时互动交流。 5.云端同步：支持iOS、Android、Web等多平台数据同步。 6.使用简便：去除多余的功能，老人小孩可以快速上手。 7.睡眠模式：睡眠模式支持定时关闭、定集关闭。 8.文本同步：边听边看，绘声绘色。 联系方式 官方网站：http://lrts.me 新浪微博：@懒人听书微博 或 http://weibo.com/lanrentingshu 微信帐号：懒人听书 或 lazyman-on联系邮箱：service@lazyaudio.com",
                    "manual_brief": "懒人一族一族的福音，地铁公交，家务，散步，旅行甚至工作，畅游在有声的世界。",
                    "recommend_enter": {
                        "f_prefix": "recommendentrance@softcate@1@22225652",
                        "all_display_count_limit": 10
                    },
                    "f_prefix": "soft$$$soft@cate@from_launcher@catesoft@1@1@catestrategy+adv@boardcate@board_101_0311"
                }
            },
* */
public class BaiduInfo {
    public String sname;
    public String packageid;//百度下packageid
    public String versionname;
    public String versioncode;
    public String cateid;//用处未知
    public String size_ori;//总大小
    public String updatetime;//最后更新时间
    public String package_name;//包名
    public int score;//评分
    public int score_count;//评分总数
    public long display_download;//下载总数
    public String platform_version;//最低平台版本
    public int popularity;//热度
    public String manual_short_brief;//短评
    public String brief;//app介绍
    public int yesterday_download_pid;//昨天下载数量
    public String category;//类别
    public String subcategory;//子类别
}
