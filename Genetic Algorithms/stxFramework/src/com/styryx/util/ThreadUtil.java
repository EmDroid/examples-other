package com.styryx.util;

public class ThreadUtil {

	private static ThreadGroup sm_rootThreadGroup = null;
	
	public static ThreadGroup getRootThreadGroup() {
		if (null == sm_rootThreadGroup) {
			ThreadGroup rootGroup = null;
			ThreadGroup group = Thread.currentThread().getThreadGroup();
			while (null != group) {
				rootGroup = group;
				group = group.getParent();
			}
			sm_rootThreadGroup = rootGroup;
		}
		return sm_rootThreadGroup;
	}
	
	public static Thread listAllThreads(ThreadGroup group) {
		int i, size, count;
//		Class<?> findClass = AppletClassLoader.class;
		// List all threads in current group.
		size = group.activeCount() + 1;
		Thread[] threads;
		for (; /* break from inside */;) {
			threads = new Thread[size];
			count = group.enumerate(threads);
			if (count < size) {
				break;
			}
		};
		for (i = 0; i < count; ++i) {
			Thread thread = threads[i];
			String name = thread.getName();
			if (null == name) {
				name = thread.getClass().getName();
			}
//			JOptionPane.showMessageDialog(null, name);
//			if (findClass.isAssignableFrom(thread.getContextClassLoader().getClass())) {
//				return thread;
//			}
		}
		// List all subgroups.
		size = group.activeGroupCount() + 1;
		ThreadGroup[] groups;
		for (; /* break from inside */;) {
			groups = new ThreadGroup[size];
			count = group.enumerate(groups);
			if (count < size) {
				break;
			}
		};
		for (i = 0; i < count; ++i) {
			Thread thread = listAllThreads(groups[i]);
			if (null != thread) {
				return thread;
			}
		}
		// Not found.
		return null;
	}
	
}
