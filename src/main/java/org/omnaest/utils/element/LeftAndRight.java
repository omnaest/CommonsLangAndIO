/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils.element;

/**
 * @see LeftAndRightInteger
 * @see ModifiableLeftAndRight
 * @author omnaest
 * @param <E>
 */
public class LeftAndRight<E>
{
	protected E	left;
	protected E	right;

	public LeftAndRight(E left, E right)
	{
		super();
		this.left = left;
		this.right = right;
	}

	public E getLeft()
	{
		return this.left;
	}

	public E getRight()
	{
		return this.right;
	}

	@Override
	public String toString()
	{
		return "LeftAndRight [left=" + this.left + ", right=" + this.right + "]";
	}

	protected LeftAndRight<E> setLeft(E left)
	{
		this.left = left;
		return this;
	}

	protected LeftAndRight<E> setRight(E right)
	{
		this.right = right;
		return this;
	}

}