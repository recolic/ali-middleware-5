package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {
    static long memory_large = 3;
    static long memory_medium = 2;
    static long memory_small = 1;
    static short mask = 0;

    @Override
    public void receiveServerMsg(String msg) {
        //System.out.println("receive msg from server :" + msg);
        String[] rcvmsglist = msg.split(",");
        if (rcvmsglist[0].equals("small")) {
            memory_small = Long.parseLong(rcvmsglist[1]);
            mask |= 0x001;
        }
        else if (rcvmsglist[0].equals("medium")) {
            memory_medium = Long.parseLong(rcvmsglist[1]);
            mask |= 0x010;
        }
        else {
            memory_large = Long.parseLong(rcvmsglist[1]);
            mask |= 0x100;
        }

        if (mask == 0x111) {
            long memory_sum = memory_large + memory_medium + memory_small;
            //System.out.println(msg + " : " + memory_large + ',' + memory_medium + ',' + memory_small + "  |  " + memory_sum);
            UserLoadBalance.weight_large = 6.0 * memory_large / memory_sum;
            UserLoadBalance.weight_medium = 6.0 * memory_medium / memory_sum;
            UserLoadBalance.weight_small = 6.0 * memory_small / memory_sum;
            //System.out.println( UserLoadBalance.weight_large +"  "+ UserLoadBalance.weight_medium+"  "+ UserLoadBalance.weight_small);
            mask = 0x0;
        }
    }

    private String cachedServerCpu;

}
