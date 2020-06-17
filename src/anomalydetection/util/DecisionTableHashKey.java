/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    DecisionTableHashKey.java
 *    Copyright (C) 2007-2012 University of Waikato, Hamilton, New Zealand
 *
 */
package anomalydetection.util;


import java.io.Serializable;

import anomalydetection.clustering.Instance;
/**
 * Class providing hash table keys for DecisionTable
 */
public class DecisionTableHashKey 
  implements Serializable {

  /** for serialization */
  static final long serialVersionUID = 5674163500154964602L;

  /** Array of attribute values for an instance */
  private double [] attributes;

  /** True for an index if the corresponding attribute value is missing. */
  private boolean [] missing;

  /** The key */
  private int key;

  /**
   * Constructor for a hashKey
   *
   * @param t an instance from which to generate a key
   * @param numAtts the number of attributes
   * @param ignoreClass if true treat the class as a normal attribute
   * @throws Exception if something goes wrong
   */
  public DecisionTableHashKey(Instance t, int numAtts, boolean ignoreClass) throws Exception {
    int i;

    key = -999;
    attributes = new double [numAtts];
   
    for (i=0;i<numAtts;i++) {
    	attributes[i] = t.get(i);
    }
  }

  /**
   * Calculates a hash code
   *
   * @return the hash code as an integer
   */
  public int hashCode() {

    int hv = 0;

    if (key != -999)
      return key;
    for (int i=0;i<attributes.length;i++) {
         hv += (i * 5 * (attributes[i]+1));
    }
  
    if (key == -999) {
      key = hv;
    }
    return hv;
  }

  /**
   * Tests if two instances are equal
   *
   * @param b a key to compare with
   * @return true if both objects are equal
   */
  public boolean equals(Object b) {

    if ((b == null) || !(b.getClass().equals(this.getClass()))) {
      return false;
    }
    boolean ok = true;
    boolean l;
    if (b instanceof DecisionTableHashKey) {
      DecisionTableHashKey n = (DecisionTableHashKey)b;
      for (int i=0;i<attributes.length;i++) {
          if (attributes[i] != n.attributes[i]) {
            ok = false;
            break;
          }
        }
      
     } else {
      return false;
    }
    return ok;
  }

  /**
   * Prints the hash code
   */
  public void print_hash_code() {
    System.out.println("Hash val: "+hashCode());
  }
  
}