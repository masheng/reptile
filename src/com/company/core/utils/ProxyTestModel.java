package com.company.core.utils;

import com.work.proxyIp.kuai_proxy.KuaiModel;

/**
 * Created by ms on 2017/9/11.
 */
public class ProxyTestModel {
    public ProxyTestModel() {
        kuaiModel = new KuaiModel();
    }

    public int useCount;//使用次数
    public int success;//成功次数
    public int ms;//平均响应时间
    public long curr;

    public KuaiModel kuaiModel;

    @Override
    public String toString() {
        return "ProxyTestModel{" +
                "useCount=" + useCount +
                ", success=" + success +
                ", ms=" + ms +
                ", curr=" + curr +
                ", kuaiModel=" + kuaiModel +
                '}';
    }
}
