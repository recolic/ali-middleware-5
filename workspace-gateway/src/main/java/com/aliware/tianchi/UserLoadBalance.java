package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    static double weight_large = 3.0;
    static double weight_medium = 2.0;
    static double weight_small = 1.0;

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        double thread = ThreadLocalRandom.current().nextDouble(6.0);
        int id = 2;
        if (thread < weight_small) id = 0;
        else if (thread < weight_small + weight_medium) id = 1;
        //System.out.println(id + " | " + weight_large + ',' + weight_medium + ',' + weight_small);
        return invokers.get(id);
    }
}
