import java.util.Iterator;

public class HashTable {
   private Tree[] table = new Tree[Screen.MAX_HASH_CODE];
   
   public HashTable () {
      for(int i = 0; i < table.length; i++) {
         table[i] = new Tree();
      }
   }
   
   //add go to table
   public void add(GameObject go) {
      table[go.hashCode() % table.length].add(go);
   }
   
   //remove go from table
   public void remove(GameObject go) {
      table[go.hashCode() % table.length].remove(go);
   }
   
   //return iterator of one tree in table
   public Iterator getIterator(int i) {
      return table[i].iterator();
   }
}