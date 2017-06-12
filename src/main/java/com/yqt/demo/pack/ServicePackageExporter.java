/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yqt.demo.pack;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.spring.ServiceBean;

/**
 * <p>
 * Title: 服务发布基类
 * </p>
 * <p>
 * Description: 按包路径发布服务
 * </p>
 * <p>
 * Copyright: Copyright (c)2011
 * </p>
 * <p>
 * Company: 易宝支付(YeePay)
 * </p>
 *
 * @author baitao.ji wang.bao
 * @version 0.1, 2014-5-27 10:50
 */
public class ServicePackageExporter implements BeanFactoryAware,
		ApplicationContextAware {

	private static final Logger LOGGER = Logger.getLogger(ServicePackageExporter.class);

	private DefaultListableBeanFactory beanFactory;

	private ApplicationContext applicationContext;

	private String packageName;

	private String interfaceName;

	@Override
	public void setBeanFactory(BeanFactory factory) throws BeansException {
		beanFactory = (DefaultListableBeanFactory) factory;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		exportServices();
	}

	/**
	 * 查找所有需要发布的服务
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void exportServices() {
		List<Class> classes = null;
		if (!StringUtils.isEmpty(packageName)) {
			try {
				// 读取 packageName 包内的所有 class
				classes = ClassReaderUtils.getClasses(packageName);
			} catch (Exception e) {
				LOGGER.error("load package fail.", e);
				throw new RuntimeException("load package fail: " + packageName);
			}
		} else if (!StringUtils.isEmpty(interfaceName)) {
			try {
				// 读取 interfaceName 指定的 class
				Class clz = Thread.currentThread().getContextClassLoader()
						.loadClass(interfaceName);
				classes = new ArrayList<Class>();
				classes.add(clz);
			} catch (Exception e) {
				LOGGER.error("load interface fail.", e);
				throw new RuntimeException("load interface fail: "
						+ interfaceName);
			}
		} else {
			throw new RuntimeException(
					"packageName or interfaceName must be specified.");
		}

		LOGGER.info("load classes: " + classes);
		for (Class clz : classes) {
			// 忽略非接口的class
			if (!clz.isInterface()) {
				continue;
			}

			String simpleName = clz.getSimpleName();
			// 首字母小写
			String serviceBeanName = StringUtils.uncapitalize(simpleName);

			// 1.根据 Spring 注册名称查找 service
			Object service = null;
			try {
				LOGGER.debug("find service by beanName: " + serviceBeanName);
				service = beanFactory.getBean(serviceBeanName);
			} catch (NoSuchBeanDefinitionException e) {
				// Do nothing
			}

			// 2.根据方法名查找 service
			if (service == null) {
				try {
					LOGGER.debug("find service by simpleName: " + simpleName);
					service = beanFactory.getBean(simpleName);
				} catch (NoSuchBeanDefinitionException e) {
					// Do nothing
				}
			}

			// 3.根据类型查找 service
			if (service == null) {
				try {
					LOGGER.debug("find service by type: " + simpleName);
					service = BeanFactoryUtils.beanOfType(beanFactory, clz);
				} catch (NoSuchBeanDefinitionException e) {
					LOGGER.warn("get bean of type[" + simpleName
							+ "] fail! exception:" + e.getMessage());
				}
			}

			// 如果找到了 service 则发布服务
			if (service != null) {
				String serviceName = "/" + simpleName;
				if (beanFactory.containsBean(serviceName)) {
					throw new RuntimeException(serviceName
							+ " already exported.");
				}
				doExportServices(serviceName, clz, service);
			} else {
				LOGGER.warn("service implements not found! interface:"
						+ simpleName);
			}
		}
	}

	/**
	 * 发布具体服务
	 *
	 * @param serviceName
	 *            服务名称
	 * @param clz
	 *            服务类
	 * @param serviceInstance
	 *            具体服务实例
	 */
	@SuppressWarnings("rawtypes")
	private void doExportServices(String serviceName, Class clz,
			Object serviceInstance) {
		ServiceBean<Object> serviceConfig = new ServiceBean<Object>();
		serviceConfig.setInterface(clz);
		if (applicationContext != null) {
			serviceConfig.setApplicationContext(applicationContext);
			try {
				serviceConfig.afterPropertiesSet();
			} catch (RuntimeException e) {
				throw (RuntimeException) e;
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		serviceConfig.setRef(serviceInstance);
		serviceConfig.export();
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
}
