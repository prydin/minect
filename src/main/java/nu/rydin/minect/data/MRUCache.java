/*
 * This file contains software that has been made available under
 * The Frameworx Open License 1.0. Use and distribution hereof are
 * subject to the restrictions set forth therein.
 *
 * Copyright (c) 2003 The Frameworx Company
 * All Rights Reserved
 */

package nu.rydin.minect.data;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * MRU (Most Recently Used) cache with a timed expiration mechanism. <br>
 * Works like <code>Map</code>, but limits the number of items to a preset
 * number, but keeping only the most recently used ones (hence the name). This
 * implementation also has a timed expiration feature that allows a user to
 * specify the time a value is valid. After that time, the value is discarded,
 * regardless of when it was last accessed. An "eviction policy" can be
 * specified to take some action when a value is discarded from the cache,
 * eihter because it expired, or it had to be discarded to make room for a new
 * value.
 */
public class MRUCache<K, V> implements Map<K, V>
{
	/**
	 * Interface to objects that want to be informed when a value was discarded
	 * from the cache.
	 */
	public interface EvictionPolicy<K, V>
	{
		/**
		 * Called when a value is about to be discarded from the cache.
		 * 
		 * @param key
		 *            The key
		 * @param value
		 *            The value.
		 */
		void evict(K key, V value);
	}

	/**
	 * Internal class wrapping cached values. Implements a doubly-linked list
	 * atom, which allows the cache to keep an "eviction queue", i.e. a queue of
	 * values eligible to be discarded from the cache. When the cache is full
	 * and it has to make room for a new item, the item at the tail end of the
	 * eviction queue is discarded. Whenever a cache item is accessed, it is
	 * moved to the head end of the queue.
	 */
	class CacheItem<K, V> extends ListAtom implements Map.Entry<K, V>
	{
		static final long serialVersionUID = 20051233123123L;

		/**
		 * The key
		 */
		protected final K m_key;

		/**
		 * The value
		 */
		protected V m_value;

		/**
		 * Constructs a new <code>CacheItem</code>
		 * 
		 * @param key
		 *            The key
		 * @param value
		 *            The value
		 */
		CacheItem(K key, V value)
		{
			m_key = key;
			m_value = value;
		}

		public K getKey()
		{
			return m_key;
		}

		public V getValue()
		{
			return m_value;
		}

		/**
		 * Removes this <code>CacheItem</code> from the eviction queue
		 */
		synchronized void remove()
		{
			this.yank();
		}

		public V setValue(V value)
		{
			m_value = value;
			return value;
		}
	}

	/**
	 * The cached data
	 */
	private HashMap<K, CacheItem<K, V>> m_map;

	/**
	 * Head of eviction queue
	 */
	private ListAtom m_head;

	/**
	 * Eviction policy, i.e. a class that will be called whenever a value is
	 * about to be discarded from the cache.
	 */
	private EvictionPolicy<K, V> m_evictionPolicy;

	/**
	 * The specified maximum size
	 */
	private int m_maxSize;

	/**
	 * Creates a new <code>MRUCache</code> with a specified maximum size
	 * 
	 * @param maxSize
	 *            The maximum size.
	 */
	public MRUCache(int maxSize)
	{
		this(maxSize, null);
	}

	/**
	 * Creates a new <code>MRUCache</code> with a specified maximum size and an
	 * eviction policy that will be called whenever a value is about to be
	 * discarded from the cache.
	 * 
	 * @param maxSize
	 *            The maximum size.
	 * @param evictionPolicy
	 *            The eviction policy
	 */
	public MRUCache(int maxSize, EvictionPolicy<K, V> evictionPolicy)
	{
		m_map = new HashMap<K, CacheItem<K, V>>(maxSize);
		m_maxSize = maxSize;
		m_head = new ListAtom();
		m_evictionPolicy = evictionPolicy;
	}

	/**
	 * Removes all values from the cache.
	 */
	public synchronized void clear()
	{
		if (m_evictionPolicy != null) {
			Iterator<Map.Entry<K, CacheItem<K, V>>> itor = m_map.entrySet()
					.iterator();
			while (itor.hasNext()) {
				Map.Entry<K, CacheItem<K, V>> me = itor.next();
				m_evictionPolicy.evict(me.getKey(), me.getValue().getValue());
			}
		}
		m_map.clear();
		m_head.yank();
	}

	/**
	 * Returns <code>true</code> if the specified key is present in the cache.
	 * 
	 * @param key
	 *            The key to look for
	 */

	public synchronized boolean containsKey(Object key)
	{
		// TODO: This also updates MRU status. Is that really what we want?
		//
		return this.getCacheItem((K) key) != null;
	}

	/**
	 * Returns <code>true</code> if the specified value id present in the cache.
	 * 
	 * @param value
	 *            The value to look for.
	 */
	public synchronized boolean containsValue(Object value)
	{
		for (Iterator<V> itor = this.values().iterator(); itor.hasNext();) {
			if (value.equals(itor.next()))
				return true;
		}
		return false;
	}

	/**
	 * Returns the set of <code>Map.Entry</code> objects connecting keys to
	 * their values.
	 */
	public synchronized Set<Map.Entry<K, V>> entrySet()
	{
		Set<Entry<K, V>> answer = new HashSet<Entry<K, V>>(this.size());
		for(CacheItem<K, V> value : m_map.values())
			answer.add(value);
		return answer;
	}

	/**
	 * Returns <code>true</code> if the specified object is an
	 * <code>MRUCache</code> and its values are equal to the called object.
	 * 
	 * @param o
	 *            The object to compare to
	 */
	public synchronized boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof MRUCache))
			return false;
		return ((MRUCache<K, V>) o).m_map.equals(m_map);
	}

	/**
	 * Returns the value associated with the specified key, or null if no value
	 * exists for this key.
	 * 
	 * @param key
	 *            The key
	 */
	@Override
	public synchronized V get(Object key)
	{
		CacheItem<K, V> ci = this.getCacheItem(key);
		if (ci == null)
			return null;
		return ci.m_value;
	}

	/**
	 * Returns the hash code.
	 */
	public synchronized int hashCode()
	{
		return m_map.hashCode();
	}

	/**
	 * Returns <code>true</code> if the cache is empty.
	 */
	public synchronized boolean isEmpty()
	{
		return m_map.isEmpty();
	}

	/**
	 * Returns all the keys as a <code>Set</code>
	 */
	public synchronized Set<K> keySet()
	{
		return m_map.keySet();
	}

	/**
	 * Associates a key with a value and stores it in the cache. If the cache
	 * has reached its maximum size, the least recently used value is discarded.
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	@Override
	public synchronized V put(K key, V value)
	{
		CacheItem<K, V> ci = this.getCacheItem(key);
		if (ci == null)
			this.insertCacheItem(new CacheItem<K, V>(key, value));
		else
			ci.m_value = value;
		return value;
	}

	/**
	 * Puts all the items in the specified map into this cache. Note that, if
	 * the specified map is larger than the cache, only the <i>n</i> last items
	 * will be stored, where <i>n</i> is the specified maximum size of the
	 * cache.
	 * 
	 * @param t
	 *            The map
	 */
	public void putAll(Map<? extends K, ? extends V> t)
	{
		for (Map.Entry<? extends K, ? extends V> entry : t.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Consciously remove the object corresponding to the <code>key</code> from
	 * the cache. The <code>EvictionPolicy</code> will not be enforced on the
	 * removed object.
	 * 
	 * @param key
	 *            The key.
	 */
	public synchronized V remove(Object key)
	{
		CacheItem<K, V> ci = (CacheItem<K, V>) m_map.remove(key);
		if (ci == null)
			return null;
		ci.remove();
		return ci.m_value;
	}

	/**
	 * Returns the current number of values in the cache.
	 */
	public int size()
	{
		return m_map.size();
	}

	/**
	 * Returns the values a <code>Collection</code>
	 */
	public synchronized Collection<V> values()
	{
		Collection<CacheItem<K, V>> values = m_map.values();
		ArrayList<V> answer = new ArrayList<V>(values.size());
		for (CacheItem<K, V> value : values)
			answer.add(value.getValue());
		return answer;
	}

	/**
	 * Returns the eviction policy.
	 */
	public EvictionPolicy<K, V> getEvictionPolicy()
	{
		return m_evictionPolicy;
	}

	/**
	 * Retruns the specified maximum size.
	 */
	public int getMaxSize()
	{
		return m_maxSize;
	}

	/**
	 * Sets the eviction policy to be called when values are discarded from the
	 * cache.
	 * 
	 * @param evictionPolicy
	 *            The eviction policy.
	 */
	public void setEvictionPolicy(EvictionPolicy<K, V> evictionPolicy)
	{
		m_evictionPolicy = evictionPolicy;
	}

	/**
	 * Sets the maximum size. If the maximum size is smaller than the current
	 * size, some values may be discarded from the cache.
	 * 
	 * @param maxSize
	 *            The new maximum size.
	 */
	public synchronized void setMaxSize(int maxSize)
	{
		int oldSize = m_maxSize;
		m_maxSize = maxSize;
		if (oldSize > maxSize)
			//
			// Perhaps we need to kick something out?
			//
			this.assertMaxSize(0);
	}

	/**
	 * Returns the <code>CacheItem</code> associated with a key, or null if no
	 * <code>CacheItem</code> existed for the specified key.
	 * 
	 * @param key
	 *            The key
	 */
	CacheItem<K, V> getCacheItem(Object key)
	{
		CacheItem<K, V> ci = m_map.get(key);
		if (ci == null)
			return null;
		ci.succeed(m_head);
		return ci;
	}

	/**
	 * Add the <code>CacheItem</code> to the linked list and to the internal
	 * map.
	 */
	void insertCacheItem(CacheItem<K, V> ci)
	{
		// Add to map and MRU list
		//
		this.assertMaxSize(1);
		m_map.put(ci.m_key, ci);
		ci.succeed(m_head);
	}

	/**
	 * Makes sure there is room for the specified number of additional items.
	 * 
	 * @param toMakeRoomFor
	 *            The number of additional items.
	 */
	private void assertMaxSize(int toMakeRoomFor)
	{
		int maxMapSize = m_maxSize - toMakeRoomFor;
		while (m_map.size() > maxMapSize) {
			// We have to kick something out. Find the victim and remove it
			//
			CacheItem<K, V> victim = (CacheItem<K, V>) m_head.previous();

			// Now we have determined the victim. Yank it from the MRU list
			//
			victim.remove();
			m_map.remove(victim.m_key);
			m_evictionPolicy.evict(victim.m_key, victim.m_value);
		}
	}
}
