package LMS;

import java.util.ArrayList;

public class HoldRequestOperations {

   static ArrayList <HoldRequest> holdRequests;

    //   初始化
    public HoldRequestOperations()
    {
        if (holdRequests == null) {
            holdRequests = new ArrayList<>();
        }
    }
    
    // 预约入队
    public static void addHoldRequest(HoldRequest hr)
    {
        holdRequests.add(hr);
    }
    
    // 预约出队（删除第一个）
    public static void removeHoldRequest()
    {
        if(!holdRequests.isEmpty())
        {
            holdRequests.remove(0);
        }
    }
    
    // 删除指定位置的预约请求
    public static void removeHoldRequestAt(int index)
    {
        if(index >= 0 && index < holdRequests.size())
        {
            holdRequests.remove(index);
        }
    }

    public static boolean isEmpty() {
        return holdRequests.isEmpty();
    }

    public static int size() {
        return holdRequests.size();
    }

    public static HoldRequest get(int i) {
        return holdRequests.get(i);
    }
}
