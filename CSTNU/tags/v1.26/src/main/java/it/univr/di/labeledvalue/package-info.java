/**
 * Provides the classes necessary to manage labeled value maps.
 * <p>
 * A labeled value is a value that is associated to a label formed by the conjunction of 0 or more literals.<br>
 * A labeled value is valid if no one of literals in the label is false. Therefore, even if the value of one or more literals are not known, the label is valid
 * and, so, the value.
 *
 * @author Roberto Posenato
 */
package it.univr.di.labeledvalue;