//  Mutation.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.operators.mutation;

import jmetal.core.Operator;

import java.util.HashMap;
import jmetal.util.PseudoRandom;

/**
 * This class represents the super class of all the mutations operators
 */
public abstract class Mutation extends Operator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected PseudoRandom pseudoRandom;
	
	public Mutation(HashMap<String, Object> parameters, PseudoRandom pseudoRandom) {
	  super(parameters);
	  this.pseudoRandom = pseudoRandom;
  }
} // Mutation
