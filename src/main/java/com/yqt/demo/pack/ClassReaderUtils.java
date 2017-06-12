/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yqt.demo.pack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * 用来读取类路径下class文件的工具
 *
 * @author：junning.li
 * @since：2011-5-13 下午11:09:27
 * @version:
 */
public class ClassReaderUtils {
	/**
	 * 根据包名获取包下所有class列表
	 *
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static List<Class> getClasses(String packageName)
			throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		List<JarInputStream> jarFiles = new ArrayList<JarInputStream>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			if ("jar".equals(resource.getProtocol())) {
				String rp = resource.getPath();
				int index = rp.indexOf("jar!");
				jarFiles.add(new JarInputStream(new FileInputStream(rp
						.substring(5, index + 3))));
			} else {
				try {
					dirs.add(new File(resource.toURI()));
				} catch (Exception e) {
					dirs.add(new File(resource.getFile()));
				}
			}
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (JarInputStream jarFile : jarFiles) {
			classes.addAll(findClassesInJar(jarFile, packageName));
		}
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	/**
	 * 从JAR文件中列出某个package下所有的class
	 *
	 * @param jarFile
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static List<Class> findClassesInJar(JarInputStream jarFile,
			String packageName) throws ClassNotFoundException, IOException {
		List<Class> classes = new ArrayList<Class>();
		JarEntry jarEntry = null;
		packageName = packageName.replaceAll("\\.", "/");
		while (true) {
			jarEntry = jarFile.getNextJarEntry();
			if (jarEntry == null) {
				break;
			}
			if ((jarEntry.getName().startsWith(packageName))
					&& (jarEntry.getName().endsWith(".class"))) {
				String className = jarEntry.getName().replaceAll("/", "\\.");
				className = className.substring(0, className.length() - 6);
				classes.add(Thread.currentThread().getContextClassLoader()
						.loadClass(className));
			}
		}
		return classes;
	}

	/**
	 * 从固定目录下面列出某个package的所有class
	 *
	 * @param directory
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<Class> findClasses(File directory, String packageName)
			throws ClassNotFoundException, FileNotFoundException, IOException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file,
						packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				String className = packageName
						+ '.'
						+ file.getName().substring(0,
								file.getName().length() - 6);
				classes.add(Thread.currentThread().getContextClassLoader()
						.loadClass(className));
			}
		}
		return classes;
	}

	public static void main(String[] args) {
		String className = "com.yeepay.g3.utils.common.FormatUtils";
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		String path = className.replace('.', '/') + ".class";
		System.out.println(classLoader.getResource(path));
	}
}
