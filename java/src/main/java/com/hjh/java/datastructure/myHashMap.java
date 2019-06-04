package com.hjh.java.datastructure;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-01-25
 * @Description:自己实现HashMap
 */
public class myHashMap<K,V>{
  //默认初始容量大小 16
  static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
  //设置最大容量 2^30
  static final int MAXIMUM_CAPACITY = 1 << 30;
  //负载因子，当容量占用达到0.75f的时候，进行扩容
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

  class Node<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;
    Node(int hash, K key, V value, Node<K,V> next) {
      this.hash = hash;
      this.key = key;
      this.value = value;
      this.next = next;
    }
    public final K getKey()        { return key; }
    public final V getValue()      { return value; }
    public final String toString() { return key + "=" + value; }
    public final V setValue(V newValue) {
      V oldValue = value;
      value = newValue;
      return oldValue;
    }
  }

  Node<K,V>[] table;
  int size;
  final float loadFactor;
  int initialCapacity;

  public int size() {
    return size;
  }

  public float getLoadFactor() {
    return loadFactor;
  }

  public int getInitialCapacity() {
    return initialCapacity;
  }

  public myHashMap(){
    this.loadFactor = DEFAULT_LOAD_FACTOR;
  }

  public myHashMap(int initialCapacity, float loadFactor){
    if (initialCapacity < 0)
      throw new IllegalArgumentException("初始化容量不合法: " +
              initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
      initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || loadFactor > 1 || Float.isNaN(loadFactor))
      throw new IllegalArgumentException("初始化负载因子不合法，应该介于0-1之间: " +
              initialCapacity);
    this.loadFactor = loadFactor;
    this.initialCapacity = tableSizeFor(initialCapacity);
  }

  static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }

  /**
   * 将容量转化为最接近的2的n次方
   * 因为扩容的时候，直接扩容一倍容易实现.
   * 例如:cap = 1000 size = 1024
   * cap = 1999 size = 2048
   * @param cap
   * @return
   */
  static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
  }

  /**
   * final 方法，可以被子类调用，不能被子类覆盖
   * @return 返回一个新的大小的Node[] 数组
   */
  final Node<K,V>[] resize(){
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    Node<K,V>[] newTab;
    int newCap = 0;
    if (oldCap > 0) {
      if (oldCap >= MAXIMUM_CAPACITY) {
        this.initialCapacity = Integer.MAX_VALUE;
        return oldTab;
      }else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
              oldCap >= DEFAULT_INITIAL_CAPACITY)
        this.initialCapacity = this.initialCapacity << 1; // 扩大一倍
    }else if (this.initialCapacity > 0) // initial capacity was placed in threshold
      newCap = this.initialCapacity;
    else {               // zero initial threshold signifies using defaults
      newCap = DEFAULT_INITIAL_CAPACITY;
    }
    return null;
  }
  public void put(K key, V value) {
    Node<K,V>[] tab;
    Node<K,V> p;
    int n;
    int i;
    if(table == null || table.length == 0){
      table = resize();
      n = table.length;
    }

  }
}
