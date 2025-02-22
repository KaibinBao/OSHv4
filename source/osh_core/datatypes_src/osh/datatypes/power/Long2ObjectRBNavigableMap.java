package osh.datatypes.power;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

public class Long2ObjectRBNavigableMap<V>  extends AbstractLong2ObjectSortedMap<V>
			implements
			java.io.Serializable,
			Cloneable, NavigableMap<Long, V> {

	/** A reference to the root entry. */
	protected transient Entry<V> tree;

	/** Number of entries in this map. */
	protected int count;

	/** The first key in this map. */
	protected transient Entry<V> firstEntry;

	/** The last key in this map. */
	protected transient Entry<V> lastEntry;

	/** Cached set of entries. */
	protected transient ObjectSortedSet<Long2ObjectMap.Entry<V>> entries;

	/** Cached set of keys. */
	protected transient LongSortedSet keys;

	/** Cached collection of values. */
	protected transient ObjectCollection<V> values;

	/**
	 * The value of this variable remembers, after a <code>put()</code> or a
	 * <code>remove()</code>, whether the <em>domain</em> of the map has been
	 * modified.
	 */
	protected transient boolean modified;

	/** This map's comparator, as provided in the constructor. */
	protected Comparator<? super Long> storedComparator;

	/**
	 * This map's actual comparator; it may differ from
	 * {@link #storedComparator} because it is always a type-specific
	 * comparator, so it could be derived from the former by wrapping.
	 */
	protected transient LongComparator actualComparator;

	private static final long serialVersionUID = -7046029254386353129L;

	private static final boolean ASSERTS = false;

	{
		allocatePaths();
	}

	/**
	 * Creates a new empty tree map.
	 */

	public Long2ObjectRBNavigableMap() {
		tree = null;
		count = 0;
	}

	/**
	 * Generates the comparator that will be actually used.
	 *
	 * <P>
	 * When a specific {@link Comparator} is specified and stored in
	 * {@link #storedComparator}, we must check whether it is type-specific. If
	 * it is so, we can used directly, and we store it in
	 * {@link #actualComparator}. Otherwise, we generate on-the-fly an anonymous
	 * class that wraps the non-specific {@link Comparator} and makes it into a
	 * type-specific one.
	 */
	private void setActualComparator() {

		/*
		 * If the provided comparator is already type-specific, we use it.
		 * Otherwise, we use a wrapper anonymous class to fake that it is
		 * type-specific.
		 */
		if (storedComparator == null
				|| storedComparator instanceof LongComparator)
			actualComparator = (LongComparator) storedComparator;
		else
			actualComparator = new LongComparator() {
				public int compare(long k1, long k2) {
					return storedComparator.compare((Long.valueOf(k1)),
							(Long.valueOf(k2)));
				}
				public int compare(Long ok1, Long ok2) {
					return storedComparator.compare(ok1, ok2);
				}
			};

	}

	/**
	 * Creates a new empty tree map with the given comparator.
	 *
	 * @param c
	 *            a (possibly type-specific) comparator.
	 */

	public Long2ObjectRBNavigableMap(final Comparator<? super Long> c) {
		this();
		storedComparator = c;
		setActualComparator();
	}

	/**
	 * Creates a new tree map copying a given map.
	 *
	 * @param m
	 *            a {@link Map} to be copied into the new tree map.
	 */

	public Long2ObjectRBNavigableMap(final Map<? extends Long, ? extends V> m) {
		this();
		putAll(m);
	}

	/**
	 * Creates a new tree map copying a given sorted map (and its
	 * {@link Comparator}).
	 *
	 * @param m
	 *            a {@link SortedMap} to be copied into the new tree map.
	 */

	public Long2ObjectRBNavigableMap(final SortedMap<Long, V> m) {
		this(m.comparator());
		putAll(m);
	}

	/**
	 * Creates a new tree map copying a given map.
	 *
	 * @param m
	 *            a type-specific map to be copied into the new tree map.
	 */

	public Long2ObjectRBNavigableMap(final Long2ObjectMap<? extends V> m) {
		this();
		putAll(m);
	}

	/**
	 * Creates a new tree map copying a given sorted map (and its
	 * {@link Comparator}).
	 *
	 * @param m
	 *            a type-specific sorted map to be copied into the new tree map.
	 */

	public Long2ObjectRBNavigableMap(final Long2ObjectSortedMap<V> m) {
		this(m.comparator());
		putAll(m);
	}

	/**
	 * Creates a new tree map using the elements of two parallel arrays and the
	 * given comparator.
	 *
	 * @param k
	 *            the array of keys of the new tree map.
	 * @param v
	 *            the array of corresponding values in the new tree map.
	 * @param c
	 *            a (possibly type-specific) comparator.
	 * @throws IllegalArgumentException
	 *             if <code>k</code> and <code>v</code> have different lengths.
	 */

	public Long2ObjectRBNavigableMap(final long[] k, final V v[],
			final Comparator<? super Long> c) {
		this(c);
		if (k.length != v.length)
			throw new IllegalArgumentException(
					"The key array and the value array have different lengths ("
							+ k.length + " and " + v.length + ")");
		for (int i = 0; i < k.length; i++)
			this.put(k[i], v[i]);
	}

	/**
	 * Creates a new tree map using the elements of two parallel arrays.
	 *
	 * @param k
	 *            the array of keys of the new tree map.
	 * @param v
	 *            the array of corresponding values in the new tree map.
	 * @throws IllegalArgumentException
	 *             if <code>k</code> and <code>v</code> have different lengths.
	 */

	public Long2ObjectRBNavigableMap(final long[] k, final V v[]) {
		this(k, v, null);
	}

	/*
	 * The following methods implements some basic building blocks used by all
	 * accessors. They are (and should be maintained) identical to those used in
	 * RBTreeSet.drv.
	 * 
	 * The put()/remove() code is derived from Ben Pfaff's GNU libavl
	 * (http://www.msu.edu/~pfaffben/avl/). If you want to understand what's
	 * going on, you should have a look at the literate code contained therein
	 * first.
	 */

	/**
	 * Compares two keys in the right way.
	 *
	 * <P>
	 * This method uses the {@link #actualComparator} if it is non-
	 * <code>null</code>. Otherwise, it resorts to primitive type comparisons or
	 * to {@link Comparable#compareTo(Object) compareTo()}.
	 *
	 * @param k1
	 *            the first key.
	 * @param k2
	 *            the second key.
	 * @return a number smaller than, equal to or greater than 0, as usual
	 *         (i.e., when k1 &lt; k2, k1 = k2 or k1 &gt; k2, respectively).
	 */

	final int compare(final long k1, final long k2) {
		return actualComparator == null
				? (Long.compare((k1), (k2)))
				: actualComparator.compare(k1, k2);
	}

	/**
	 * Returns the entry corresponding to the given key, if it is in the tree;
	 * <code>null</code>, otherwise.
	 *
	 * @param k
	 *            the key to search for.
	 * @return the corresponding entry, or <code>null</code> if no entry with
	 *         the given key exists.
	 */

	final Entry<V> findKey(final long k) {
		Entry<V> e = tree;
		int cmp;

		while (e != null && (cmp = compare(k, e.key)) != 0)
			e = cmp < 0 ? e.left() : e.right();

		return e;
	}

	/**
	 * Locates a key.
	 *
	 * @param k
	 *            a key.
	 * @return the last entry on a search for the given key; this will be the
	 *         given key, if it present; otherwise, it will be either the
	 *         smallest greater key or the greatest smaller key.
	 */

	final Entry<V> locateKey(final long k) {
		Entry<V> e = tree, last = tree;
		int cmp = 0;

		while (e != null && (cmp = compare(k, e.key)) != 0) {
			last = e;
			e = cmp < 0 ? e.left() : e.right();
		}

		return cmp == 0 ? e : last;
	}

	/**
	 * This vector remembers the path and the direction followed during the
	 * current insertion. It suffices for about 2<sup>32</sup> entries.
	 */
	private transient boolean dirPath[];
	private transient Entry<V> nodePath[];

	@SuppressWarnings("unchecked")
	private void allocatePaths() {
		dirPath = new boolean[64];
		nodePath = new Entry[64];
	}
	public V put(final long k, final V v) {
		Entry<V> e = add(k);
		final V oldValue = e.value;
		e.value = v;
		return oldValue;
	}

	/**
	 * Returns a node with key k in the balanced tree, creating one with
	 * defRetValue if necessary.
	 *
	 * @param k
	 *            the key
	 * @return a node with key k. If a node with key k already exists, then that
	 *         node is returned, otherwise a new node with defRetValue is
	 *         created ensuring that the tree is balanced after creation of the
	 *         node.
	 */
	private Entry<V> add(final long k) {
		/*
		 * After execution of this method, modified is true iff a new entry has
		 * been inserted.
		 */
		modified = false;
		int maxDepth = 0;

		Entry<V> e;

		if (tree == null) { // The case of the empty tree is treated separately.
			count++;
			e = tree = lastEntry = firstEntry = new Entry<V>(k, defRetValue);
		} else {
			Entry<V> p = tree;
			int cmp, i = 0;

			while (true) {
				if ((cmp = compare(k, p.key)) == 0) {
					// We clean up the node path, or we could have stale
					// references later.
					while (i-- != 0)
						nodePath[i] = null;
					return p;
				}

				nodePath[i] = p;

				if (dirPath[i++] = cmp > 0) {
					if (p.succ()) {
						count++;
						e = new Entry<V>(k, defRetValue);

						if (p.right == null)
							lastEntry = e;

						e.left = p;
						e.right = p.right;

						p.right(e);

						break;
					}

					p = p.right;
				} else {
					if (p.pred()) {
						count++;
						e = new Entry<V>(k, defRetValue);

						if (p.left == null)
							firstEntry = e;

						e.right = p;
						e.left = p.left;

						p.left(e);

						break;
					}

					p = p.left;
				}
			}

			modified = true;
			maxDepth = i--;

			while (i > 0 && !nodePath[i].black()) {
				if (!dirPath[i - 1]) {
					Entry<V> y = nodePath[i - 1].right;

					if (!nodePath[i - 1].succ() && !y.black()) {
						nodePath[i].black(true);
						y.black(true);
						nodePath[i - 1].black(false);
						i -= 2;
					} else {
						Entry<V> x;

						if (!dirPath[i])
							y = nodePath[i];
						else {
							x = nodePath[i];
							y = x.right;
							x.right = y.left;
							y.left = x;
							nodePath[i - 1].left = y;

							if (y.pred()) {
								y.pred(false);
								x.succ(y);
							}
						}

						x = nodePath[i - 1];
						x.black(false);
						y.black(true);

						x.left = y.right;
						y.right = x;
						if (i < 2)
							tree = y;
						else {
							if (dirPath[i - 2])
								nodePath[i - 2].right = y;
							else
								nodePath[i - 2].left = y;
						}

						if (y.succ()) {
							y.succ(false);
							x.pred(y);
						}
						break;
					}
				} else {
					Entry<V> y = nodePath[i - 1].left;

					if (!nodePath[i - 1].pred() && !y.black()) {
						nodePath[i].black(true);
						y.black(true);
						nodePath[i - 1].black(false);
						i -= 2;
					} else {
						Entry<V> x;

						if (dirPath[i])
							y = nodePath[i];
						else {
							x = nodePath[i];
							y = x.left;
							x.left = y.right;
							y.right = x;
							nodePath[i - 1].right = y;

							if (y.succ()) {
								y.succ(false);
								x.pred(y);
							}

						}

						x = nodePath[i - 1];
						x.black(false);
						y.black(true);

						x.right = y.left;
						y.left = x;
						if (i < 2)
							tree = y;
						else {
							if (dirPath[i - 2])
								nodePath[i - 2].right = y;
							else
								nodePath[i - 2].left = y;
						}

						if (y.pred()) {
							y.pred(false);
							x.succ(y);
						}

						break;
					}
				}
			}
		}
		tree.black(true);
		// We clean up the node path, or we could have stale references later.
		while (maxDepth-- != 0)
			nodePath[maxDepth] = null;
		if (ASSERTS) {
			checkNodePath();
			checkTree(tree, 0, -1);
		}
		return e;
	}

	/*
	 * After execution of this method, {@link #modified} is true iff an entry
	 * has been deleted.
	 */

	public V remove(final long k) {
		modified = false;

		if (tree == null)
			return defRetValue;

		Entry<V> p = tree;
		int cmp;
		int i = 0;
		final long kk = k;

		while (true) {
			if ((cmp = compare(kk, p.key)) == 0)
				break;

			dirPath[i] = cmp > 0;
			nodePath[i] = p;

			if (dirPath[i++]) {
				if ((p = p.right()) == null) {
					// We clean up the node path, or we could have stale
					// references later.
					while (i-- != 0)
						nodePath[i] = null;
					return defRetValue;
				}
			} else {
				if ((p = p.left()) == null) {
					// We clean up the node path, or we could have stale
					// references later.
					while (i-- != 0)
						nodePath[i] = null;
					return defRetValue;
				}
			}

		}

		if (p.left == null)
			firstEntry = p.next();
		if (p.right == null)
			lastEntry = p.prev();

		if (p.succ()) {
			if (p.pred()) {
				if (i == 0)
					tree = p.left;
				else {
					if (dirPath[i - 1])
						nodePath[i - 1].succ(p.right);
					else
						nodePath[i - 1].pred(p.left);
				}
			} else {
				p.prev().right = p.right;

				if (i == 0)
					tree = p.left;
				else {
					if (dirPath[i - 1])
						nodePath[i - 1].right = p.left;
					else
						nodePath[i - 1].left = p.left;
				}
			}
		} else {
			boolean color;
			Entry<V> r = p.right;

			if (r.pred()) {
				r.left = p.left;
				r.pred(p.pred());
				if (!r.pred())
					r.prev().right = r;
				if (i == 0)
					tree = r;
				else {
					if (dirPath[i - 1])
						nodePath[i - 1].right = r;
					else
						nodePath[i - 1].left = r;
				}

				color = r.black();
				r.black(p.black());
				p.black(color);
				dirPath[i] = true;
				nodePath[i++] = r;
			} else {
				Entry<V> s;
				int j = i++;

				while (true) {
					dirPath[i] = false;
					nodePath[i++] = r;
					s = r.left;
					if (s.pred())
						break;
					r = s;
				}

				dirPath[j] = true;
				nodePath[j] = s;

				if (s.succ())
					r.pred(s);
				else
					r.left = s.right;

				s.left = p.left;

				if (!p.pred()) {
					p.prev().right = s;
					s.pred(false);
				}

				s.right(p.right);

				color = s.black();
				s.black(p.black());
				p.black(color);

				if (j == 0)
					tree = s;
				else {
					if (dirPath[j - 1])
						nodePath[j - 1].right = s;
					else
						nodePath[j - 1].left = s;
				}
			}
		}

		int maxDepth = i;

		if (p.black()) {
			for (; i > 0; i--) {
				if (dirPath[i - 1] && !nodePath[i - 1].succ()
						|| !dirPath[i - 1] && !nodePath[i - 1].pred()) {
					Entry<V> x = dirPath[i - 1]
							? nodePath[i - 1].right
							: nodePath[i - 1].left;

					if (!x.black()) {
						x.black(true);
						break;
					}
				}

				if (!dirPath[i - 1]) {
					Entry<V> w = nodePath[i - 1].right;

					if (!w.black()) {
						w.black(true);
						nodePath[i - 1].black(false);

						nodePath[i - 1].right = w.left;
						w.left = nodePath[i - 1];

						if (i < 2)
							tree = w;
						else {
							if (dirPath[i - 2])
								nodePath[i - 2].right = w;
							else
								nodePath[i - 2].left = w;
						}

						nodePath[i] = nodePath[i - 1];
						dirPath[i] = false;
						nodePath[i - 1] = w;
						if (maxDepth == i++)
							maxDepth++;

						w = nodePath[i - 1].right;
					}

					if ((w.pred() || w.left.black())
							&& (w.succ() || w.right.black())) {
						w.black(false);
					} else {
						if (w.succ() || w.right.black()) {
							Entry<V> y = w.left;

							y.black(true);
							w.black(false);
							w.left = y.right;
							y.right = w;
							w = nodePath[i - 1].right = y;

							if (w.succ()) {
								w.succ(false);
								w.right.pred(w);
							}
						}

						w.black(nodePath[i - 1].black());
						nodePath[i - 1].black(true);
						w.right.black(true);

						nodePath[i - 1].right = w.left;
						w.left = nodePath[i - 1];

						if (i < 2)
							tree = w;
						else {
							if (dirPath[i - 2])
								nodePath[i - 2].right = w;
							else
								nodePath[i - 2].left = w;
						}

						if (w.pred()) {
							w.pred(false);
							nodePath[i - 1].succ(w);
						}
						break;
					}
				} else {
					Entry<V> w = nodePath[i - 1].left;

					if (!w.black()) {
						w.black(true);
						nodePath[i - 1].black(false);

						nodePath[i - 1].left = w.right;
						w.right = nodePath[i - 1];

						if (i < 2)
							tree = w;
						else {
							if (dirPath[i - 2])
								nodePath[i - 2].right = w;
							else
								nodePath[i - 2].left = w;
						}

						nodePath[i] = nodePath[i - 1];
						dirPath[i] = true;
						nodePath[i - 1] = w;
						if (maxDepth == i++)
							maxDepth++;

						w = nodePath[i - 1].left;
					}

					if ((w.pred() || w.left.black())
							&& (w.succ() || w.right.black())) {
						w.black(false);
					} else {
						if (w.pred() || w.left.black()) {
							Entry<V> y = w.right;

							y.black(true);
							w.black(false);
							w.right = y.left;
							y.left = w;
							w = nodePath[i - 1].left = y;

							if (w.pred()) {
								w.pred(false);
								w.left.succ(w);
							}
						}

						w.black(nodePath[i - 1].black());
						nodePath[i - 1].black(true);
						w.left.black(true);

						nodePath[i - 1].left = w.right;
						w.right = nodePath[i - 1];

						if (i < 2)
							tree = w;
						else {
							if (dirPath[i - 2])
								nodePath[i - 2].right = w;
							else
								nodePath[i - 2].left = w;
						}

						if (w.succ()) {
							w.succ(false);
							nodePath[i - 1].pred(w);
						}
						break;
					}
				}
			}

			if (tree != null)
				tree.black(true);
		}

		modified = true;
		count--;
		// We clean up the node path, or we could have stale references later.
		while (maxDepth-- != 0)
			nodePath[maxDepth] = null;
		if (ASSERTS) {
			checkNodePath();
			checkTree(tree, 0, -1);
		}
		return p.value;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public V put(final Long ok, final V ov) {
		final V oldValue = put(((ok).longValue()), (ov));
		return modified ? (this.defRetValue) : (oldValue);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public V remove(final Object ok) {
		final V oldValue = remove(((((Long) (ok)).longValue())));
		return modified ? (oldValue) : (this.defRetValue);
	}

	public boolean containsValue(final Object v) {
		final ValueIterator i = new ValueIterator();
		Object ev;

		int j = count;
		while (j-- != 0) {
			ev = i.next();
			if (((ev) == null ? (v) == null : (ev).equals(v)))
				return true;
		}

		return false;
	}

	public void clear() {
		count = 0;
		tree = null;
		entries = null;
		values = null;
		keys = null;
		firstEntry = lastEntry = null;
	}

	/**
	 * This class represent an entry in a tree map.
	 *
	 * <P>
	 * We use the only "metadata", i.e., {@link Entry#info}, to store
	 * information about color, predecessor status and successor status.
	 *
	 * <P>
	 * Note that since the class is recursive, it can be considered equivalently
	 * a tree.
	 */

	private static final class Entry<V>
			implements
				Cloneable,
				Long2ObjectMap.Entry<V> {
		/** The the bit in this mask is true, the node is black. */
		private final static int BLACK_MASK = 1;
		/**
		 * If the bit in this mask is true, {@link #right} points to a
		 * successor.
		 */
		private final static int SUCC_MASK = 1 << 31;
		/**
		 * If the bit in this mask is true, {@link #left} points to a
		 * predecessor.
		 */
		private final static int PRED_MASK = 1 << 30;
		/** The key of this entry. */
		long key;
		/** The value of this entry. */
		V value;
		/** The pointers to the left and right subtrees. */
		Entry<V> left, right;
		/**
		 * This integers holds different information in different bits (see
		 * {@link #SUCC_MASK} and {@link #PRED_MASK}.
		 */
		int info;

		Entry() {
		}

		/**
		 * Creates a new entry with the given key and value.
		 *
		 * @param k
		 *            a key.
		 * @param v
		 *            a value.
		 */
		Entry(final long k, final V v) {
			this.key = k;
			this.value = v;
			info = SUCC_MASK | PRED_MASK;
		}

		/**
		 * Returns the left subtree.
		 *
		 * @return the left subtree (<code>null</code> if the left subtree is
		 *         empty).
		 */
		Entry<V> left() {
			return (info & PRED_MASK) != 0 ? null : left;
		}

		/**
		 * Returns the right subtree.
		 *
		 * @return the right subtree (<code>null</code> if the right subtree is
		 *         empty).
		 */
		Entry<V> right() {
			return (info & SUCC_MASK) != 0 ? null : right;
		}

		/**
		 * Checks whether the left pointer is really a predecessor.
		 * 
		 * @return true if the left pointer is a predecessor.
		 */
		boolean pred() {
			return (info & PRED_MASK) != 0;
		}

		/**
		 * Checks whether the right pointer is really a successor.
		 * 
		 * @return true if the right pointer is a successor.
		 */
		boolean succ() {
			return (info & SUCC_MASK) != 0;
		}

		/**
		 * Sets whether the left pointer is really a predecessor.
		 * 
		 * @param pred
		 *            if true then the left pointer will be considered a
		 *            predecessor.
		 */
		void pred(final boolean pred) {
			if (pred)
				info |= PRED_MASK;
			else
				info &= ~PRED_MASK;
		}

		/**
		 * Sets whether the right pointer is really a successor.
		 * 
		 * @param succ
		 *            if true then the right pointer will be considered a
		 *            successor.
		 */
		void succ(final boolean succ) {
			if (succ)
				info |= SUCC_MASK;
			else
				info &= ~SUCC_MASK;
		}

		/**
		 * Sets the left pointer to a predecessor.
		 * 
		 * @param pred
		 *            the predecessr.
		 */
		void pred(final Entry<V> pred) {
			info |= PRED_MASK;
			left = pred;
		}

		/**
		 * Sets the right pointer to a successor.
		 * 
		 * @param succ
		 *            the successor.
		 */
		void succ(final Entry<V> succ) {
			info |= SUCC_MASK;
			right = succ;
		}

		/**
		 * Sets the left pointer to the given subtree.
		 * 
		 * @param left
		 *            the new left subtree.
		 */
		void left(final Entry<V> left) {
			info &= ~PRED_MASK;
			this.left = left;
		}

		/**
		 * Sets the right pointer to the given subtree.
		 * 
		 * @param right
		 *            the new right subtree.
		 */
		void right(final Entry<V> right) {
			info &= ~SUCC_MASK;
			this.right = right;
		}

		/**
		 * Returns whether this node is black.
		 * 
		 * @return true iff this node is black.
		 */
		boolean black() {
			return (info & BLACK_MASK) != 0;
		}

		/**
		 * Sets whether this node is black.
		 * 
		 * @param black
		 *            if true, then this node becomes black; otherwise, it
		 *            becomes red..
		 */
		void black(final boolean black) {
			if (black)
				info |= BLACK_MASK;
			else
				info &= ~BLACK_MASK;
		}

		/**
		 * Computes the next entry in the set order.
		 *
		 * @return the next entry (<code>null</code>) if this is the last
		 *         entry).
		 */

		Entry<V> next() {
			Entry<V> next = this.right;
			if ((info & SUCC_MASK) == 0)
				while ((next.info & PRED_MASK) == 0)
					next = next.left;
			return next;
		}

		/**
		 * Computes the previous entry in the set order.
		 *
		 * @return the previous entry (<code>null</code>) if this is the first
		 *         entry).
		 */

		Entry<V> prev() {
			Entry<V> prev = this.left;
			if ((info & PRED_MASK) == 0)
				while ((prev.info & SUCC_MASK) == 0)
					prev = prev.right;
			return prev;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method
		 *             instead.
		 */
		@Deprecated
		public Long getKey() {
			return (Long.valueOf(key));
		}

		public long getLongKey() {
			return key;
		}

		public V getValue() {
			return (value);
		}

		public V setValue(final V value) {
			final V oldValue = this.value;
			this.value = value;
			return oldValue;
		}
		@SuppressWarnings("unchecked")
		public Entry<V> clone() {
			Entry<V> c;
			try {
				c = (Entry<V>) super.clone();
			} catch (CloneNotSupportedException cantHappen) {
				throw new InternalError();
			}

			c.key = key;
			c.value = value;
			c.info = info;

			return c;
		}

		@SuppressWarnings("unchecked")
		public boolean equals(final Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<Long, V> e = (Map.Entry<Long, V>) o;

			return ((key) == (((e.getKey()).longValue())))
					&& ((value) == null ? ((e.getValue())) == null : (value)
							.equals((e.getValue())));
		}

		public int hashCode() {
			return it.unimi.dsi.fastutil.HashCommon.long2int(key)
					^ ((value) == null ? 0 : (value).hashCode());
		}

		public String toString() {
			return key + "=>" + value;
		}

		/*
		 * public void prettyPrint() { prettyPrint(0); }
		 * 
		 * public void prettyPrint(int level) { if ( pred() ) { for (int i = 0;
		 * i < level; i++) System.err.print("  "); System.err.println("pred: " +
		 * left ); } else if (left != null) left.prettyPrint(level +1 ); for
		 * (int i = 0; i < level; i++) System.err.print("  ");
		 * System.err.println(key + "=" + value + " (" + balance() + ")"); if (
		 * succ() ) { for (int i = 0; i < level; i++) System.err.print("  ");
		 * System.err.println("succ: " + right ); } else if (right != null)
		 * right.prettyPrint(level + 1); }
		 */
	}

	/*
	 * public void prettyPrint() { System.err.println("size: " + count); if
	 * (tree != null) tree.prettyPrint(); }
	 */

	public boolean containsKey(final long k) {
		return findKey(k) != null;
	}

	public int size() {
		return count;
	}

	public boolean isEmpty() {
		return count == 0;
	}

	public V get(final long k) {
		final Entry<V> e = findKey(k);
		return e == null ? defRetValue : e.value;
	}
	public long firstLongKey() {
		if (tree == null)
			throw new NoSuchElementException();
		return firstEntry.key;
	}

	public long lastLongKey() {
		if (tree == null)
			throw new NoSuchElementException();
		return lastEntry.key;
	}

	/**
	 * An abstract iterator on the whole range.
	 *
	 * <P>
	 * This class can iterate in both directions on a threaded tree.
	 */

	private class TreeIterator {
		/**
		 * The entry that will be returned by the next call to
		 * {@link java.util.ListIterator#previous()} (or <code>null</code> if no
		 * previous entry exists).
		 */
		Entry<V> prev;
		/**
		 * The entry that will be returned by the next call to
		 * {@link java.util.ListIterator#next()} (or <code>null</code> if no
		 * next entry exists).
		 */
		Entry<V> next;
		/**
		 * The last entry that was returned (or <code>null</code> if we did not
		 * iterate or used {@link #remove()}).
		 */
		Entry<V> curr;
		/**
		 * The current index (in the sense of a {@link java.util.ListIterator}).
		 * Note that this value is not meaningful when this {@link TreeIterator}
		 * has been created using the nonempty constructor.
		 */
		int index = 0;

		TreeIterator() {
			next = firstEntry;
		}

		TreeIterator(final long k) {
			if ((next = locateKey(k)) != null) {
				if (compare(next.key, k) <= 0) {
					prev = next;
					next = next.next();
				} else
					prev = next.prev();
			}
		}

		public boolean hasNext() {
			return next != null;
		}
		public boolean hasPrevious() {
			return prev != null;
		}

		void updateNext() {
			next = next.next();
		}

		Entry<V> nextEntry() {
			if (!hasNext())
				throw new NoSuchElementException();
			curr = prev = next;
			index++;
			updateNext();
			return curr;
		}

		void updatePrevious() {
			prev = prev.prev();
		}

		Entry<V> previousEntry() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			curr = next = prev;
			index--;
			updatePrevious();
			return curr;
		}

		public int nextIndex() {
			return index;
		}

		public int previousIndex() {
			return index - 1;
		}

		public void remove() {
			if (curr == null)
				throw new IllegalStateException();
			/*
			 * If the last operation was a next(), we are removing an entry that
			 * preceeds the current index, and thus we must decrement it.
			 */
			if (curr == prev)
				index--;
			next = prev = curr;
			updatePrevious();
			updateNext();
			Long2ObjectRBNavigableMap.this.remove(curr.key);
			curr = null;
		}

		public int skip(final int n) {
			int i = n;
			while (i-- != 0 && hasNext())
				nextEntry();
			return n - i - 1;
		}

		public int back(final int n) {
			int i = n;
			while (i-- != 0 && hasPrevious())
				previousEntry();
			return n - i - 1;
		}
	}

	/**
	 * An iterator on the whole range.
	 *
	 * <P>
	 * This class can iterate in both directions on a threaded tree.
	 */

	private class EntryIterator extends TreeIterator
			implements
				ObjectListIterator<Long2ObjectMap.Entry<V>> {
		EntryIterator() {
		}

		EntryIterator(final long k) {
			super(k);
		}

		public Long2ObjectMap.Entry<V> next() {
			return nextEntry();
		}
		public Long2ObjectMap.Entry<V> previous() {
			return previousEntry();
		}

		public void set(Long2ObjectMap.Entry<V> ok) {
			throw new UnsupportedOperationException();
		}
		public void add(Long2ObjectMap.Entry<V> ok) {
			throw new UnsupportedOperationException();
		}
	}

	public ObjectSortedSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
		if (entries == null)
			entries = new AbstractObjectSortedSet<Long2ObjectMap.Entry<V>>() {
				final Comparator<? super Long2ObjectMap.Entry<V>> comparator = new Comparator<Long2ObjectMap.Entry<V>>() {
					public int compare(final Long2ObjectMap.Entry<V> x,
							Long2ObjectMap.Entry<V> y) {
						return Long2ObjectRBNavigableMap.this.actualComparator
								.compare(x.getLongKey(), y.getLongKey());
					}
				};

				public Comparator<? super Long2ObjectMap.Entry<V>> comparator() {
					return comparator;
				}

				public ObjectBidirectionalIterator<Long2ObjectMap.Entry<V>> iterator() {
					return new EntryIterator();
				}

				public ObjectBidirectionalIterator<Long2ObjectMap.Entry<V>> iterator(
						final Long2ObjectMap.Entry<V> from) {
					return new EntryIterator(from.getLongKey());
				}

				public boolean contains(final Object o) {
					if (!(o instanceof Map.Entry))
						return false;
					final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

					if (e.getKey() == null || !(e.getKey() instanceof Long))
						return false;

					final Entry<V> f = findKey(((((Long) (e.getKey()))
							.longValue())));
					return e.equals(f);
				}

				public boolean remove(final Object o) {
					if (!(o instanceof Map.Entry))
						return false;
					final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

					if (e.getKey() == null || !(e.getKey() instanceof Long))
						return false;

					final Entry<V> f = findKey(((((Long) (e.getKey()))
							.longValue())));
					if (f != null)
						Long2ObjectRBNavigableMap.this.remove(f.key);
					return f != null;
				}

				public int size() {
					return count;
				}
				public void clear() {
					Long2ObjectRBNavigableMap.this.clear();
				}

				public Long2ObjectMap.Entry<V> first() {
					return firstEntry;
				}
				public Long2ObjectMap.Entry<V> last() {
					return lastEntry;
				}
				public ObjectSortedSet<Long2ObjectMap.Entry<V>> subSet(
						Long2ObjectMap.Entry<V> from, Long2ObjectMap.Entry<V> to) {
					return subMap(from.getLongKey(), to.getLongKey())
							.long2ObjectEntrySet();
				}
				public ObjectSortedSet<Long2ObjectMap.Entry<V>> headSet(
						Long2ObjectMap.Entry<V> to) {
					return headMap(to.getLongKey()).long2ObjectEntrySet();
				}
				public ObjectSortedSet<Long2ObjectMap.Entry<V>> tailSet(
						Long2ObjectMap.Entry<V> from) {
					return tailMap(from.getLongKey()).long2ObjectEntrySet();
				}
			};

		return entries;
	}

	/**
	 * An iterator on the whole range of keys.
	 *
	 * <P>
	 * This class can iterate in both directions on the keys of a threaded tree.
	 * We simply override the {@link java.util.ListIterator#next()}/
	 * {@link java.util.ListIterator#previous()} methods (and possibly their
	 * type-specific counterparts) so that they return keys instead of entries.
	 */
	private final class KeyIterator extends TreeIterator
			implements
				LongListIterator {
		public KeyIterator() {
		}
		public KeyIterator(final long k) {
			super(k);
		}
		public long nextLong() {
			return nextEntry().key;
		}
		public long previousLong() {
			return previousEntry().key;
		}

		public void set(long k) {
			throw new UnsupportedOperationException();
		}
		public void add(long k) {
			throw new UnsupportedOperationException();
		}

		public Long next() {
			return (Long.valueOf(nextEntry().key));
		}
		public Long previous() {
			return (Long.valueOf(previousEntry().key));
		}
		public void set(Long ok) {
			throw new UnsupportedOperationException();
		}
		public void add(Long ok) {
			throw new UnsupportedOperationException();
		}

	};

	/**
	 * A keyset implementation using a more direct implementation for iterators.
	 */
	private class KeySet extends AbstractLong2ObjectSortedMap<V>.KeySet {
		public LongBidirectionalIterator iterator() {
			return new KeyIterator();
		}
		public LongBidirectionalIterator iterator(final long from) {
			return new KeyIterator(from);
		}
	}

	/**
	 * Returns a type-specific sorted set view of the keys contained in this
	 * map.
	 *
	 * <P>
	 * In addition to the semantics of {@link java.util.Map#keySet()}, you can
	 * safely cast the set returned by this call to a type-specific sorted set
	 * interface.
	 *
	 * @return a type-specific sorted set view of the keys contained in this
	 *         map.
	 */
	public LongSortedSet keySet() {
		if (keys == null)
			keys = new KeySet();
		return keys;
	}

	/**
	 * An iterator on the whole range of values.
	 *
	 * <P>
	 * This class can iterate in both directions on the values of a threaded
	 * tree. We simply override the {@link java.util.ListIterator#next()}/
	 * {@link java.util.ListIterator#previous()} methods (and possibly their
	 * type-specific counterparts) so that they return values instead of
	 * entries.
	 */
	private final class ValueIterator extends TreeIterator
			implements
				ObjectListIterator<V>,
				Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4385217557807222485L;
		public V next() {
			return nextEntry().value;
		}
		public V previous() {
			return previousEntry().value;
		}
		public void set(V v) {
			throw new UnsupportedOperationException();
		}
		public void add(V v) {
			throw new UnsupportedOperationException();
		}

	};

	/**
	 * Returns a type-specific collection view of the values contained in this
	 * map.
	 *
	 * <P>
	 * In addition to the semantics of {@link java.util.Map#values()}, you can
	 * safely cast the collection returned by this call to a type-specific
	 * collection interface.
	 *
	 * @return a type-specific collection view of the values contained in this
	 *         map.
	 */

	public ObjectCollection<V> values() {
		if (values == null)
			values = new AbstractObjectCollection<V>() {
				public ObjectIterator<V> iterator() {
					return new ValueIterator();
				}

				public boolean contains(final Object k) {
					return containsValue(k);
				}

				public int size() {
					return count;
				}

				public void clear() {
					Long2ObjectRBNavigableMap.this.clear();
				}

			};

		return values;
	}

	public LongComparator comparator() {
		return actualComparator;
	}

	public Long2ObjectSortedMap<V> headMap(long to) {
		return new Submap((0), true, to, false);
	}

	public Long2ObjectSortedMap<V> tailMap(long from) {
		return new Submap(from, false, (0), true);
	}

	public Long2ObjectSortedMap<V> subMap(long from, long to) {
		return new Submap(from, false, to, false);
	}

	/**
	 * A submap with given range.
	 *
	 * <P>
	 * This class represents a submap. One has to specify the left/right limits
	 * (which can be set to -&infin; or &infin;). Since the submap is a view on
	 * the map, at a given moment it could happen that the limits of the range
	 * are not any longer in the main map. Thus, things such as
	 * {@link java.util.SortedMap#firstKey()} or
	 * {@link java.util.Collection#size()} must be always computed on-the-fly.
	 */
	private final class Submap extends AbstractLong2ObjectSortedMap<V>
			implements
				java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;

		/** The start of the submap range, unless {@link #bottom} is true. */
		long from;
		/** The end of the submap range, unless {@link #top} is true. */
		long to;
		/** If true, the submap range starts from -&infin;. */
		boolean bottom;
		/** If true, the submap range goes to &infin;. */
		boolean top;
		/** Cached set of entries. */
		protected transient ObjectSortedSet<Long2ObjectMap.Entry<V>> entries;
		/** Cached set of keys. */
		protected transient LongSortedSet keys;
		/** Cached collection of values. */
		protected transient ObjectCollection<V> values;

		/**
		 * Creates a new submap with given key range.
		 *
		 * @param from
		 *            the start of the submap range.
		 * @param bottom
		 *            if true, the first parameter is ignored and the range
		 *            starts from -&infin;.
		 * @param to
		 *            the end of the submap range.
		 * @param top
		 *            if true, the third parameter is ignored and the range goes
		 *            to &infin;.
		 */
		public Submap(final long from, final boolean bottom, final long to,
				final boolean top) {
			if (!bottom && !top
					&& Long2ObjectRBNavigableMap.this.compare(from, to) > 0)
				throw new IllegalArgumentException("Start key (" + from
						+ ") is larger than end key (" + to + ")");

			this.from = from;
			this.bottom = bottom;
			this.to = to;
			this.top = top;
			this.defRetValue = Long2ObjectRBNavigableMap.this.defRetValue;
		}

		public void clear() {
			final SubmapIterator i = new SubmapIterator();
			while (i.hasNext()) {
				i.nextEntry();
				i.remove();
			}
		}

		/**
		 * Checks whether a key is in the submap range.
		 * 
		 * @param k
		 *            a key.
		 * @return true if is the key is in the submap range.
		 */
		final boolean in(final long k) {
			return (bottom || Long2ObjectRBNavigableMap.this.compare(k, from) >= 0)
					&& (top || Long2ObjectRBNavigableMap.this.compare(k, to) < 0);
		}

		public ObjectSortedSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
			if (entries == null)
				entries = new AbstractObjectSortedSet<Long2ObjectMap.Entry<V>>() {
					public ObjectBidirectionalIterator<Long2ObjectMap.Entry<V>> iterator() {
						return new SubmapEntryIterator();
					}

					public ObjectBidirectionalIterator<Long2ObjectMap.Entry<V>> iterator(
							final Long2ObjectMap.Entry<V> from) {
						return new SubmapEntryIterator(from.getLongKey());
					}

					public Comparator<? super Long2ObjectMap.Entry<V>> comparator() {
						return Long2ObjectRBNavigableMap.this.long2ObjectEntrySet()
								.comparator();
					}

					public boolean contains(final Object o) {
						if (!(o instanceof Map.Entry))
							return false;
						final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

						if (e.getKey() == null || !(e.getKey() instanceof Long))
							return false;

						final Long2ObjectRBNavigableMap.Entry<V> f = findKey(((((Long) (e
								.getKey())).longValue())));
						return f != null && in(f.key) && e.equals(f);
					}

					public boolean remove(final Object o) {
						if (!(o instanceof Map.Entry))
							return false;
						final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

						if (e.getKey() == null || !(e.getKey() instanceof Long))
							return false;

						final Long2ObjectRBNavigableMap.Entry<V> f = findKey(((((Long) (e
								.getKey())).longValue())));
						if (f != null && in(f.key))
							Submap.this.remove(f.key);
						return f != null;
					}

					public int size() {
						int c = 0;
						for (Iterator<?> i = iterator(); i.hasNext(); i.next())
							c++;
						return c;
					}

					public boolean isEmpty() {
						return !new SubmapIterator().hasNext();
					}
					public void clear() {
						Submap.this.clear();
					}

					public Long2ObjectMap.Entry<V> first() {
						return firstEntry();
					}
					public Long2ObjectMap.Entry<V> last() {
						return lastEntry();
					}
					public ObjectSortedSet<Long2ObjectMap.Entry<V>> subSet(
							Long2ObjectMap.Entry<V> from,
							Long2ObjectMap.Entry<V> to) {
						return subMap(from.getLongKey(), to.getLongKey())
								.long2ObjectEntrySet();
					}
					public ObjectSortedSet<Long2ObjectMap.Entry<V>> headSet(
							Long2ObjectMap.Entry<V> to) {
						return headMap(to.getLongKey()).long2ObjectEntrySet();
					}
					public ObjectSortedSet<Long2ObjectMap.Entry<V>> tailSet(
							Long2ObjectMap.Entry<V> from) {
						return tailMap(from.getLongKey()).long2ObjectEntrySet();
					}
				};

			return entries;
		}

		private class KeySet extends AbstractLong2ObjectSortedMap<V>.KeySet {
			public LongBidirectionalIterator iterator() {
				return new SubmapKeyIterator();
			}
			public LongBidirectionalIterator iterator(final long from) {
				return new SubmapKeyIterator(from);
			}
		}

		public LongSortedSet keySet() {
			if (keys == null)
				keys = new KeySet();
			return keys;
		}

		public ObjectCollection<V> values() {
			if (values == null)
				values = new AbstractObjectCollection<V>() {
					public ObjectIterator<V> iterator() {
						return new SubmapValueIterator();
					}

					public boolean contains(final Object k) {
						return containsValue(k);
					}

					public int size() {
						return Submap.this.size();
					}

					public void clear() {
						Submap.this.clear();
					}

				};

			return values;
		}

		public boolean containsKey(final long k) {
			return in(k) && Long2ObjectRBNavigableMap.this.containsKey(k);
		}

		public boolean containsValue(final Object v) {
			final SubmapIterator i = new SubmapIterator();
			Object ev;

			while (i.hasNext()) {
				ev = i.nextEntry().value;
				if (((ev) == null ? (v) == null : (ev).equals(v)))
					return true;
			}

			return false;
		}

		public V get(final long k) {
			final Long2ObjectRBNavigableMap.Entry<V> e;
			final long kk = k;
			return in(kk) && (e = findKey(kk)) != null
					? e.value
					: this.defRetValue;
		}
		public V put(final long k, final V v) {
			modified = false;
			if (!in(k))
				throw new IllegalArgumentException("Key (" + k
						+ ") out of range ["
						+ (bottom ? "-" : String.valueOf(from)) + ", "
						+ (top ? "-" : String.valueOf(to)) + ")");
			final V oldValue = Long2ObjectRBNavigableMap.this.put(k, v);
			return modified ? this.defRetValue : oldValue;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method
		 *             instead.
		 */
		@Deprecated
		@Override
		public V put(final Long ok, final V ov) {
			final V oldValue = put(((ok).longValue()), (ov));
			return modified ? (this.defRetValue) : (oldValue);
		}

		public V remove(final long k) {
			modified = false;
			if (!in(k))
				return this.defRetValue;
			final V oldValue = Long2ObjectRBNavigableMap.this.remove(k);
			return modified ? oldValue : this.defRetValue;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method
		 *             instead.
		 */
		@Deprecated
		@Override
		public V remove(final Object ok) {
			final V oldValue = remove(((((Long) (ok)).longValue())));
			return modified ? (oldValue) : (this.defRetValue);
		}

		public int size() {
			final SubmapIterator i = new SubmapIterator();
			int n = 0;

			while (i.hasNext()) {
				n++;
				i.nextEntry();
			}

			return n;
		}

		public boolean isEmpty() {
			return !new SubmapIterator().hasNext();
		}

		public LongComparator comparator() {
			return actualComparator;
		}

		public Long2ObjectSortedMap<V> headMap(final long to) {
			if (top)
				return new Submap(from, bottom, to, false);
			return compare(to, this.to) < 0 ? new Submap(from, bottom, to,
					false) : this;
		}

		public Long2ObjectSortedMap<V> tailMap(final long from) {
			if (bottom)
				return new Submap(from, false, to, top);
			return compare(from, this.from) > 0 ? new Submap(from, false, to,
					top) : this;
		}

		public Long2ObjectSortedMap<V> subMap(long from, long to) {
			if (top && bottom)
				return new Submap(from, false, to, false);
			if (!top)
				to = compare(to, this.to) < 0 ? to : this.to;
			if (!bottom)
				from = compare(from, this.from) > 0 ? from : this.from;
			if (!top && !bottom && from == this.from && to == this.to)
				return this;
			return new Submap(from, false, to, false);
		}

		/**
		 * Locates the first entry.
		 *
		 * @return the first entry of this submap, or <code>null</code> if the
		 *         submap is empty.
		 */
		public Long2ObjectRBNavigableMap.Entry<V> firstEntry() {
			if (tree == null)
				return null;
			// If this submap goes to -infinity, we return the main map first
			// entry; otherwise, we locate the start of the map.
			Long2ObjectRBNavigableMap.Entry<V> e;
			if (bottom)
				e = firstEntry;
			else {
				e = locateKey(from);
				// If we find either the start or something greater we're OK.
				if (compare(e.key, from) < 0)
					e = e.next();
			}
			// Finally, if this submap doesn't go to infinity, we check that the
			// resulting key isn't greater than the end.
			if (e == null || !top && compare(e.key, to) >= 0)
				return null;
			return e;
		}

		/**
		 * Locates the last entry.
		 *
		 * @return the last entry of this submap, or <code>null</code> if the
		 *         submap is empty.
		 */
		public Long2ObjectRBNavigableMap.Entry<V> lastEntry() {
			if (tree == null)
				return null;
			// If this submap goes to infinity, we return the main map last
			// entry; otherwise, we locate the end of the map.
			Long2ObjectRBNavigableMap.Entry<V> e;
			if (top)
				e = lastEntry;
			else {
				e = locateKey(to);
				// If we find something smaller than the end we're OK.
				if (compare(e.key, to) >= 0)
					e = e.prev();
			}
			// Finally, if this submap doesn't go to -infinity, we check that
			// the resulting key isn't smaller than the start.
			if (e == null || !bottom && compare(e.key, from) < 0)
				return null;
			return e;
		}

		public long firstLongKey() {
			Long2ObjectRBNavigableMap.Entry<V> e = firstEntry();
			if (e == null)
				throw new NoSuchElementException();
			return e.key;
		}
		public long lastLongKey() {
			Long2ObjectRBNavigableMap.Entry<V> e = lastEntry();
			if (e == null)
				throw new NoSuchElementException();
			return e.key;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method
		 *             instead.
		 */
		@Deprecated
		@Override
		public Long firstKey() {
			Long2ObjectRBNavigableMap.Entry<V> e = firstEntry();
			if (e == null)
				throw new NoSuchElementException();
			return e.getKey();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @deprecated Please use the corresponding type-specific method
		 *             instead.
		 */
		@Deprecated
		@Override
		public Long lastKey() {
			Long2ObjectRBNavigableMap.Entry<V> e = lastEntry();
			if (e == null)
				throw new NoSuchElementException();
			return e.getKey();
		}

		/**
		 * An iterator for subranges.
		 * 
		 * <P>
		 * This class inherits from {@link TreeIterator}, but overrides the
		 * methods that update the pointer after a
		 * {@link java.util.ListIterator#next()} or
		 * {@link java.util.ListIterator#previous()}. If we would move out of
		 * the range of the submap we just overwrite the next or previous entry
		 * with <code>null</code>.
		 */
		private class SubmapIterator extends TreeIterator {
			SubmapIterator() {
				next = firstEntry();
			}

			SubmapIterator(final long k) {
				this();

				if (next != null) {
					if (!bottom && compare(k, next.key) < 0)
						prev = null;
					else if (!top && compare(k, (prev = lastEntry()).key) >= 0)
						next = null;
					else {
						next = locateKey(k);

						if (compare(next.key, k) <= 0) {
							prev = next;
							next = next.next();
						} else
							prev = next.prev();
					}
				}
			}

			void updatePrevious() {
				prev = prev.prev();
				if (!bottom
						&& prev != null
						&& Long2ObjectRBNavigableMap.this.compare(prev.key, from) < 0)
					prev = null;
			}

			void updateNext() {
				next = next.next();
				if (!top && next != null
						&& Long2ObjectRBNavigableMap.this.compare(next.key, to) >= 0)
					next = null;
			}
		}

		private class SubmapEntryIterator extends SubmapIterator
				implements
					ObjectListIterator<Long2ObjectMap.Entry<V>> {
			SubmapEntryIterator() {
			}

			SubmapEntryIterator(final long k) {
				super(k);
			}

			public Long2ObjectMap.Entry<V> next() {
				return nextEntry();
			}
			public Long2ObjectMap.Entry<V> previous() {
				return previousEntry();
			}

			public void set(Long2ObjectMap.Entry<V> ok) {
				throw new UnsupportedOperationException();
			}
			public void add(Long2ObjectMap.Entry<V> ok) {
				throw new UnsupportedOperationException();
			}
		}

		/**
		 * An iterator on a subrange of keys.
		 *
		 * <P>
		 * This class can iterate in both directions on a subrange of the keys
		 * of a threaded tree. We simply override the
		 * {@link java.util.ListIterator#next()}/
		 * {@link java.util.ListIterator#previous()} methods (and possibly their
		 * type-specific counterparts) so that they return keys instead of
		 * entries.
		 */
		private final class SubmapKeyIterator extends SubmapIterator
				implements
					LongListIterator {
			public SubmapKeyIterator() {
				super();
			}
			public SubmapKeyIterator(long from) {
				super(from);
			}
			public long nextLong() {
				return nextEntry().key;
			}
			public long previousLong() {
				return previousEntry().key;
			}
			public void set(long k) {
				throw new UnsupportedOperationException();
			}
			public void add(long k) {
				throw new UnsupportedOperationException();
			}

			public Long next() {
				return (Long.valueOf(nextEntry().key));
			}
			public Long previous() {
				return (Long.valueOf(previousEntry().key));
			}
			public void set(Long ok) {
				throw new UnsupportedOperationException();
			}
			public void add(Long ok) {
				throw new UnsupportedOperationException();
			}

		};

		/**
		 * An iterator on a subrange of values.
		 *
		 * <P>
		 * This class can iterate in both directions on the values of a subrange
		 * of the keys of a threaded tree. We simply override the
		 * {@link java.util.ListIterator#next()}/
		 * {@link java.util.ListIterator#previous()} methods (and possibly their
		 * type-specific counterparts) so that they return values instead of
		 * entries.
		 */
		private final class SubmapValueIterator extends SubmapIterator
				implements
					ObjectListIterator<V> {
			public V next() {
				return nextEntry().value;
			}
			public V previous() {
				return previousEntry().value;
			}
			public void set(V v) {
				throw new UnsupportedOperationException();
			}
			public void add(V v) {
				throw new UnsupportedOperationException();
			}

		};

	}

	/**
	 * Returns a deep copy of this tree map.
	 *
	 * <P>
	 * This method performs a deep copy of this tree map; the data stored in the
	 * set, however, is not cloned. Note that this makes a difference only for
	 * object keys.
	 *
	 * @return a deep copy of this tree map.
	 */

	@SuppressWarnings("unchecked")
	public Long2ObjectRBNavigableMap<V> clone() {
		Long2ObjectRBNavigableMap<V> c;
		try {
			c = (Long2ObjectRBNavigableMap<V>) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}

		c.keys = null;
		c.values = null;
		c.entries = null;
		c.allocatePaths();

		if (count != 0) {
			// Also this apparently unfathomable code is derived from GNU
			// libavl.
			Entry<V> e, p, q, rp = new Entry<V>(), rq = new Entry<V>();

			p = rp;
			rp.left(tree);

			q = rq;
			rq.pred(null);

			while (true) {
				if (!p.pred()) {
					e = p.left.clone();
					e.pred(q.left);
					e.succ(q);
					q.left(e);

					p = p.left;
					q = q.left;
				} else {
					while (p.succ()) {
						p = p.right;

						if (p == null) {
							q.right = null;
							c.tree = rq.left;

							c.firstEntry = c.tree;
							while (c.firstEntry.left != null)
								c.firstEntry = c.firstEntry.left;
							c.lastEntry = c.tree;
							while (c.lastEntry.right != null)
								c.lastEntry = c.lastEntry.right;

							return c;
						}
						q = q.right;
					}

					p = p.right;
					q = q.right;
				}

				if (!p.succ()) {
					e = p.right.clone();
					e.succ(q.right);
					e.pred(q);
					q.right(e);
				}
			}
		}

		return c;
	}

	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		int n = count;
		EntryIterator i = new EntryIterator();
		Entry<V> e;

		s.defaultWriteObject();

		while (n-- != 0) {
			e = i.nextEntry();
			s.writeLong(e.key);
			s.writeObject(e.value);
		}
	}

	/**
	 * Reads the given number of entries from the input stream, returning the
	 * corresponding tree.
	 *
	 * @param s
	 *            the input stream.
	 * @param n
	 *            the (positive) number of entries to read.
	 * @param pred
	 *            the entry containing the key that preceeds the first key in
	 *            the tree.
	 * @param succ
	 *            the entry containing the key that follows the last key in the
	 *            tree.
	 */
	@SuppressWarnings("unchecked")
	private Entry<V> readTree(final java.io.ObjectInputStream s, final int n,
			final Entry<V> pred, final Entry<V> succ)
			throws java.io.IOException, ClassNotFoundException {
		if (n == 1) {
			final Entry<V> top = new Entry<V>(s.readLong(), (V) s.readObject());
			top.pred(pred);
			top.succ(succ);
			top.black(true);

			return top;
		}

		if (n == 2) {
			/*
			 * We handle separately this case so that recursion willalways* be
			 * on nonempty subtrees.
			 */
			final Entry<V> top = new Entry<V>(s.readLong(), (V) s.readObject());
			top.black(true);
			top.right(new Entry<V>(s.readLong(), (V) s.readObject()));
			top.right.pred(top);
			top.pred(pred);
			top.right.succ(succ);

			return top;
		}

		// The right subtree is the largest one.
		final int rightN = n / 2, leftN = n - rightN - 1;

		final Entry<V> top = new Entry<V>();

		top.left(readTree(s, leftN, pred, top));

		top.key = s.readLong();
		top.value = (V) s.readObject();
		top.black(true);

		top.right(readTree(s, rightN, top, succ));

		if (n + 2 == ((n + 2) & -(n + 2)))
			top.right.black(false); // Quick test for determining whether n + 2
									// is a power of 2.

		return top;
	}

	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		/*
		 * The storedComparator is now correctly set, but we must restore
		 * on-the-fly the actualComparator.
		 */
		setActualComparator();
		allocatePaths();

		if (count != 0) {
			tree = readTree(s, count, null, null);
			Entry<V> e;

			e = tree;
			while (e.left() != null)
				e = e.left();
			firstEntry = e;

			e = tree;
			while (e.right() != null)
				e = e.right();
			lastEntry = e;
		}

		if (ASSERTS)
			checkTree(tree, 0, -1);

	}
	private void checkNodePath() {
	}
	private static <V> int checkTree(Entry<V> e, int d, int D) {
		return 0;
	}

	@Override
	public java.util.Map.Entry<Long, V> lowerEntry(Long key) {
		return getLowerEntry(key);
	}

	@Override
	public Long lowerKey(Long key) {
		return getLowerEntry(key).getLongKey();
	}

	@Override
	public java.util.Map.Entry<Long, V> floorEntry(Long key) {
		return getFloorEntry(key);
	}

	@Override
	public Long floorKey(Long key) {
		return getFloorEntry(key).getLongKey();
	}

	@Override
	public java.util.Map.Entry<Long, V> ceilingEntry(Long key) {
		return getCeilingEntry(key);
	}

	@Override
	public Long ceilingKey(Long key) {
		return getCeilingEntry(key).getLongKey();
	}

	@Override
	public java.util.Map.Entry<Long, V> higherEntry(Long key) {
		return getHigherEntry(key);
	}

	@Override
	public Long higherKey(Long key) {
		return getHigherEntry(key).getLongKey();
	}

	@Override
	public java.util.Map.Entry<Long, V> firstEntry() {
		return firstEntry;
	}

	@Override
	public java.util.Map.Entry<Long, V> lastEntry() {
		return lastEntry;
	}

	@Override
	public java.util.Map.Entry<Long, V> pollFirstEntry() {
		java.util.Map.Entry<Long, V> entry = firstEntry;
		remove(firstEntry.key);
		return entry;
	}

	@Override
	public java.util.Map.Entry<Long, V> pollLastEntry() {
		java.util.Map.Entry<Long, V> entry = lastEntry;
		remove(lastEntry.key);
		return entry;
	}

	@Override
	public NavigableMap<Long, V> descendingMap() {
		return null;
	}

	@Override
	public NavigableSet<Long> navigableKeySet() {
		return null;
	}

	@Override
	public NavigableSet<Long> descendingKeySet() {
		return null;
	}

	@Override
	public NavigableMap<Long, V> subMap(Long fromKey, boolean fromInclusive, Long toKey, boolean toInclusive) {
		return null;
	}

	@Override
	public NavigableMap<Long, V> headMap(Long toKey, boolean inclusive) {
		return null;
	}

	@Override
	public NavigableMap<Long, V> tailMap(Long fromKey, boolean inclusive) {
		return null;
	}
	
	//Helper methods copied from java TreeMap
	/**
     * Gets the entry corresponding to the specified key; if no such entry
     * exists, returns the entry for the least key greater than the specified
     * key; if no such entry exists (i.e., the greatest key in the Tree is less
     * than the specified key), returns {@code null}.
     */
    final Entry<V> getCeilingEntry(Long key) {
        Entry<V> p = tree;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0) {
                if (p.left != null)
                    p = p.left;
                else
                    return p;
            } else if (cmp > 0) {
                if (p.right != null) {
                    p = p.right;
                } else {
                    Entry<V> parent = p.prev();
                    Entry<V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.prev();
                    }
                    return parent;
                }
            } else
                return p;
        }
        return null;
    }

    /**
     * Gets the entry corresponding to the specified key; if no such entry
     * exists, returns the entry for the greatest key less than the specified
     * key; if no such entry exists, returns {@code null}.
     */
    final Entry<V> getFloorEntry(Long key) {
        Entry<V> p = tree;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp > 0) {
                if (p.right != null)
                    p = p.right;
                else
                    return p;
            } else if (cmp < 0) {
                if (p.left != null) {
                    p = p.left;
                } else {
                    Entry<V> parent = p.prev();
                    Entry<V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.prev();
                    }
                    return parent;
                }
            } else
                return p;

        }
        return null;
    }

    /**
     * Gets the entry for the least key greater than the specified
     * key; if no such entry exists, returns the entry for the least
     * key greater than the specified key; if no such entry exists
     * returns {@code null}.
     */
    final Entry<V> getHigherEntry(Long key) {
        Entry<V> p = tree;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0) {
                if (p.left != null)
                    p = p.left;
                else
                    return p;
            } else {
                if (p.right != null) {
                    p = p.right;
                } else {
                    Entry<V> parent = p.prev();
                    Entry<V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.prev();
                    }
                    return parent;
                }
            }
        }
        return null;
    }

    /**
     * Returns the entry for the greatest key less than the specified key; if
     * no such entry exists (i.e., the least key in the Tree is greater than
     * the specified key), returns {@code null}.
     */
    final Entry<V> getLowerEntry(Long key) {
        Entry<V> p = tree;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp > 0) {
                if (p.right != null)
                    p = p.right;
                else
                    return p;
            } else {
                if (p.left != null) {
                    p = p.left;
                } else {
                    Entry<V> parent = p.prev();
                    Entry<V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.prev();
                    }
                    return parent;
                }
            }
        }
        return null;
    }
}
