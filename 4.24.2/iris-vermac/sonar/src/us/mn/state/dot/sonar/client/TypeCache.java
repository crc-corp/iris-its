/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2013  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.sonar.client;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A type cache represents the first-level nodes in the SONAR namespace. It
 * contains all proxy objects of the given type.
 *
 * @author Douglas Lau
 */
public class TypeCache<T extends SonarObject> implements Iterable<T> {

	/** Initial capacity of type hash */
	static private final int INITIAL_CAPACITY = 256;

	/** Class loader needed to create proxy objects */
	static protected final ClassLoader LOADER =
		TypeCache.class.getClassLoader();

	/** Type name */
	public final String tname;

	/** Interfaces which proxies of this type implement */
	protected final Class[] ifaces;

	/** Sonar object proxy method invoker */
	protected final SonarInvoker invoker;

	/** Client (to send attribute update messages) */
	protected final Client client;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** All SONAR objects of this type are put here.
	 * All access must be synchronized on the "children" lock. */
	private final ConcurrentHashMap<String, T> children =
		new ConcurrentHashMap<String, T>(INITIAL_CAPACITY, 0.75f, 1);

	/** Weak mapping from proxy object to attribute map.
	 * All access must be synchronized on the "children" lock. */
	private final WeakHashMap<T, AttributeMap> attributes =
		new WeakHashMap<T, AttributeMap>();

	/** Flag to indicate enumeration from server is complete */
	private boolean enumerated = false;

	/** A phantom is a new object which has had attributes set, but not
	 * been declared with Message.OBJECT ("o") */
	private T phantom;

	/** Proxy listener list */
	private final LinkedList<ProxyListener<T>> listeners =
		new LinkedList<ProxyListener<T>>();

	/** Create a type cache. NOTE: because of limitations with generics
	 * and reflection, the interface must be passed to the constructor in
	 * addition to the &lt;T&gt; qualifier. These types must be identical!
	 * For example, to create a cache of Users, do this:
	 * <code>new TypeCache&lt;User&gt;(User.class)</code> */
	public TypeCache(Class iface, Client c) throws NoSuchFieldException,
		IllegalAccessException
	{
		assert SonarObject.class.isAssignableFrom(iface);
		tname = Namespace.typeName(iface);
		ifaces = new Class[] { iface };
		invoker = new SonarInvoker(this, iface);
		client = c;
		namespace = client.getNamespace();
	}

	/** Notify proxy listeners that a proxy has been added */
	protected void notifyProxyAdded(T proxy) {
		for(ProxyListener<T> l: listeners)
			l.proxyAdded(proxy);
	}

	/** Notify proxy listeners that enumeration has completed */
	protected void notifyEnumerationComplete() {
		for(ProxyListener<T> l: listeners)
			l.enumerationComplete();
	}

	/** Notify proxy listeners that a proxy has been removed */
	protected void notifyProxyRemoved(T proxy) {
		for(ProxyListener<T> l: listeners)
			l.proxyRemoved(proxy);
	}

	/** Notify proxy listeners that a proxy has been changed */
	protected void notifyProxyChanged(T proxy, String a) {
		for(ProxyListener<T> l: listeners)
			l.proxyChanged(proxy, a);
	}

	/** Create a proxy in the type cache */
	@SuppressWarnings("unchecked")
	T createProxy(String name) {
		T o = (T)Proxy.newProxyInstance(LOADER, ifaces, invoker);
		AttributeMap amap = new AttributeMap(
			invoker.createAttributes(name));
		synchronized(children) {
			children.put(name, o);
			attributes.put(o, amap);
			phantom = o;
		}
		return o;
	}

	/** Get (or create) a proxy from the type cache */
	T getProxy(String name) {
		synchronized(children) {
			if(children.containsKey(name))
				return children.get(name);
			else
				return createProxy(name);
		}
	}

	/** Add a proxy to the type cache */
	T add(String name) {
		T o = getProxy(name);
		synchronized(children) {
			notifyProxyAdded(o);
		}
		phantom = null;
		return o;
	}

	/** Enumeration of proxy type is complete */
	public void enumerationComplete() {
		synchronized(children) {
			notifyEnumerationComplete();
			enumerated = true;
		}
	}

	/** Remove a proxy from the type cache */
	T remove(String name) throws NamespaceError {
		synchronized(children) {
			T proxy = children.remove(name);
			if(proxy == null)
				throw NamespaceError.nameUnknown(name);
			AttributeMap amap = attributes.get(proxy);
			if(amap != null)
				amap.zombie = true;
			notifyProxyRemoved(proxy);
			return proxy;
		}
	}

	/** Lookup a proxy from the given name */
	public T lookupObject(String n) {
		return children.get(n);
	}

	/** Get the size of the cache */
	public int size() {
		return children.size();
	}

	/** Check if a proxy object is a zombie */
	private boolean isZombie(T o) {
		synchronized(children) {
			AttributeMap amap = attributes.get(o);
			return amap != null && amap.zombie;
		}
	}

	/** Lookup the attribute map for the given object */
	private Map<String, Attribute> lookupAttributeMap(T o) {
		synchronized(children) {
			AttributeMap amap = attributes.get(o);
			return amap != null ? amap.attrs : null;
		}
	}

	/** Lookup an attribute of the given proxy */
	protected Attribute lookupAttribute(T o, String a)
		throws NamespaceError
	{
		Map<String, Attribute> amap = lookupAttributeMap(o);
		if(amap != null) {
			Attribute attr = amap.get(a);
			if(attr != null)
				return attr;
			else
				throw NamespaceError.nameUnknown(a);
		} else
			throw NamespaceError.nameUnknown("o:" + a);
	}

	/** Get the value of an attribute from the named proxy */
	Object getAttribute(String n, String a) throws NamespaceError {
		T obj = lookupObject(n);
		if(obj == null)
			throw NamespaceError.nameUnknown(n);
		else
			return getAttribute(obj, a);
	}

	/** Get the value of an attribute from the given proxy */
	Object getAttribute(T o, String a) throws NamespaceError {
		Attribute attr = lookupAttribute(o, a);
		return attr.getValue();
	}

	/** Set the value of an attribute on the given proxy.
	 * @param o Proxy object
	 * @param a Attribute name
	 * @param args New attribute value
	 * @param check Flag to check cache before sending message to server */
	void setAttribute(T o, String a, Object[] args, boolean check)
		throws SonarException
	{
		Attribute attr = lookupAttribute(o, a);
		if(check && attr.valueEquals(args))
			return;
		String[] values = namespace.marshall(attr.type, args);
		if(!isZombie(o))
			client.setAttribute(new Name(o, a), values);
	}

	/** Update an attribute value into the given proxy */
	void updateAttribute(T o, String a, String[] v)
		throws SonarException
	{
		Attribute attr = lookupAttribute(o, a);
		attr.setValue(namespace.unmarshall(attr.type, v));
		synchronized(children) {
			if(o != phantom)
				notifyProxyChanged(o, a);
		}
	}

	/** Remove the specified object */
	void removeObject(T o) {
		if(!isZombie(o))
			client.removeObject(new Name(o));
	}

	/** Create the specified object name */
	public void createObject(String oname) {
		client.createObject(new Name(tname, oname));
	}

	/** Create an object with the specified attributes */
	public void createObject(String oname, Map<String, Object> amap) {
		for(Map.Entry<String, Object> entry: amap.entrySet()) {
			Object v = entry.getValue();
			String[] values = namespace.marshall(
				v.getClass(), new Object[] { v });
			Name name = new Name(tname, oname, entry.getKey());
			client.setAttribute(name, values);
		}
		// FIXME: there is a race between the setAttribute calls and
		// the createObject call. Another thread could get in between
		// and mess up the "phantom" object creation.
		client.createObject(new Name(tname, oname));
	}

	/** Add a ProxyListener */
	public void addProxyListener(ProxyListener<T> l) {
		synchronized(children) {
			listeners.add(l);
			for(T proxy: children.values())
				l.proxyAdded(proxy);
			if(enumerated)
				l.enumerationComplete();
		}
	}

	/** Remove a ProxyListener */
	public void removeProxyListener(ProxyListener<T> l) {
		synchronized(children) {
			listeners.remove(l);
		}
	}

	/** Ignore updates to the specified attribute for all objects */
	public void ignoreAttribute(String a) {
		// To ignore an attribute for all objects in the cache,
		// the name must equal getAttributeName() for any object.
		client.ignoreName(new Name(tname, "", a));
	}

	/** Watch for all attributes of the specified object */
	public void watchObject(T proxy) {
		if(!isZombie(proxy))
			client.enumerateName(new Name(tname, proxy.getName()));
	}

	/** Ignore attributes of the specified object.  This just removes an
	 * object watch -- it does not prevent the type watch from causing
	 * the object to be watched.  */
	public void ignoreObject(T proxy) {
		if(!isZombie(proxy))
			client.ignoreName(new Name(tname, proxy.getName()));
	}

	/** Get an iterator of all objects of the type */
	public Iterator<T> iterator() {
		return Collections.unmodifiableCollection(
			children.values()).iterator();
	}
}
