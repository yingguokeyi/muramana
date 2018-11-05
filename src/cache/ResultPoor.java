package cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultPoor {

	public static AtomicInteger sid = new AtomicInteger(1);

	public static Map<Integer, String> resultMap = new ConcurrentHashMap<Integer, String>();

	public static void recovery() {
		while (true) {
			System.out.println("结果池占用数量：" + resultMap.size());
			if (resultMap.size() > 100) {
				System.out.println("结果池回收");
				int flag = sid.get() - 100;
				for (Integer key : resultMap.keySet()) {
					if (key < flag)
						resultMap.remove(key);
				}
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getResult(Integer sid){
		String res = "";
		int times = 0;
		while(times < 100){
			times++;
			if(resultMap.containsKey(sid)){
				res = resultMap.get(sid);
				resultMap.remove(sid);
				break;
			}else{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.out.println("取结果集出现异常");
				}
			}
		}
		return res;
	}
	
}