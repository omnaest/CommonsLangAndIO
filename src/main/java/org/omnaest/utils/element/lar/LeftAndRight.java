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
package org.omnaest.utils.element.lar;

/**
 * @see UnaryLeftAndRight
 * @see IntegerLeftAndRight
 * @see ModifiableUnaryLeftAndRight
 * @author omnaest
 * @param <E>
 */
public class LeftAndRight<L, R>
{
	protected L	left;
	protected R	right;

	public LeftAndRight(L left, R right)
	{
		super();
		this.left = left;
		this.right = right;
	}

	public L getLeft()
	{
		return this.left;
	}

	public R getRight()
	{
		return this.right;
	}

	@Override
	public String toString()
	{
		return "LeftAndRight [left=" + this.left + ", right=" + this.right + "]";
	}

	protected LeftAndRight<L, R> setLeft(L left)
	{
		this.left = left;
		return this;
	}

	protected LeftAndRight<L, R> setRight(R right)
	{
		this.right = right;
		return this;
	}

}