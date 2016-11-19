package cc.xiaokr.herecare;

import android.util.Log;

/**
 * 
 * @author bobby
 *
 */
public class LogUtils {
	private static final boolean DBG = true;
	private static final String SEPARATOR = " ";

	public static void d(String str) {
		if (DBG) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			Log.d(getDefaultTag(stackTraceElement), getLogInfo(stackTraceElement) + str);
		}
	}
	
	public static void e(String str) {
		if (DBG) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			Log.e(getDefaultTag(stackTraceElement), getLogInfo(stackTraceElement) + str);
		}
	}

	private static String getDefaultTag(StackTraceElement stackTraceElement) {
		String fileName = stackTraceElement.getFileName();
		String stringArray[] = fileName.split("\\.");
		String tag = stringArray[0];
		return "krobot_"+tag;
	}

	private static String getLogInfo(StackTraceElement stackTraceElement) {
		StringBuilder logInfoStringBuilder = new StringBuilder();
		String threadName = Thread.currentThread().getName();
		long threadID = Thread.currentThread().getId();
		String fileName = stackTraceElement.getFileName();
		String className = stackTraceElement.getClassName();
		String methodName = stackTraceElement.getMethodName();
		int lineNumber = stackTraceElement.getLineNumber();

		logInfoStringBuilder.append("[");
		//      logInfoStringBuilder.append("threadID=" + threadID).append(SEPARATOR);
		//		logInfoStringBuilder.append("threadName=" + threadName).append(SEPARATOR);
		//		logInfoStringBuilder.append("fileName=" + fileName).append(SEPARATOR);
		//logInfoStringBuilder.append("" + className).append(SEPARATOR);
		//logInfoStringBuilder.append("methodName=" + methodName).append(SEPARATOR);
		logInfoStringBuilder.append("" + lineNumber);
		logInfoStringBuilder.append("]");
		return logInfoStringBuilder.toString();
	}
}
