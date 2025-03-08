package cn.czh.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * spring工具类用来获取一些实体类
 */
@Service
public class SpringBeanUtil implements ApplicationContextAware {
	
	private static ApplicationContext appContext;
	
	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		appContext = context; 
	}

	public static ApplicationContext getApplicationContext(){
		return appContext;
	}

	public static <T> T getBean(Class<T> cls) {
		return appContext.getBean(cls);
	}

	public static <T> T getBean(String className) {
		return (T) appContext.getBean(className);
	}

	public static <T> T getBean(String className, Class<T> cls) {
		return appContext.getBean(className, cls);
	}

}