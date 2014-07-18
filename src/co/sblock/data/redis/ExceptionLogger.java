package co.sblock.data.redis;

import com.tmathmeyer.jadis.async.CallBackLogger;
import co.sblock.utilities.Log;

/**
 * 
 * @author ted
 *
 */
public class ExceptionLogger implements CallBackLogger<Exception> {

	static final ExceptionLogger log = new ExceptionLogger();
	
	/**
	 * @return the logger instance
	 */
	public static ExceptionLogger getEL() {
		return log;
	}
	
	@Override
	public void Log(Exception e, Class<?> clazz) {
		Log.getLog("ExceptionLogger").criticalErr(e);
		Log.getLog("ExceptionLogger").severe(clazz.getSimpleName());
	}

}
