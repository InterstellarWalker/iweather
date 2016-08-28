package utils;

import java.util.Map;

public interface HttpCallBackListener {
	void onFinish(String response);
	void onError(Exception e);
}
