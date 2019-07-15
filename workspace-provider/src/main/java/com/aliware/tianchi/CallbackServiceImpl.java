package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author daofeng.xjf
 * <p>
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 */
public class CallbackServiceImpl implements CallbackService {

    public CallbackServiceImpl() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!listeners.isEmpty()) {
                    String messageToPush = generateStatusMessage();
                    for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                        try {
                            entry.getValue().receiveServerMsg(messageToPush);
                        } catch (Throwable t1) {
                            listeners.remove(entry.getKey());
                        }
                    }
                }
            }
        }, 0, 5000);
    }

    private Timer timer = new Timer();

    private String generateStatusMessage() {
        try {
            List<String> cpuLoadList = getProcessCpuLoad().stream().map(Object::toString).collect(Collectors.toList());
            String cpuLoadString = String.join(",", cpuLoadList);
            System.out.println("Server push " + cpuLoadString);
            return "cpu=" + cpuLoadString;
        }
        catch(Exception ex) {
            return "error";

        }

    }

    /**
     * key: listener type
     * value: callback listener
     */
    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();

    @Override
    public void addListener(String key, CallbackListener listener) {
        System.out.println("Server: add Listener " + key);
        listeners.put(key, listener);
        listener.receiveServerMsg(new Date().toString()); // send notification for change
    }

    // System status impl
    private static List<Double> getProcessCpuLoad() throws Exception {
        List<Double> result = new ArrayList<>();

        MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
        ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

        for(int i = 0; i < list.size(); ++i) {
            Attribute att = (Attribute) list.get(i);
            Double value  = (Double)att.getValue();
            if(value == -1.0)
                // usually takes a couple of seconds before we get real values
                result.add(Double.NaN);
            else
                // returns a percentage value with 1 decimal point precision
                result.add(((int)(value * 1000) / 10.0));
        }

        return result;
    }
}
