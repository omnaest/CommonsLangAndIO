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

public class ModifiableLeftAndRight<E> extends LeftAndRight<E>
{
	public ModifiableLeftAndRight()
	{
		this(null, null);
	}

	public ModifiableLeftAndRight(E left, E right)
	{
		super(left, right);
	}

	@Override
	public ModifiableLeftAndRight<E> setLeft(E left)
	{
		super.setLeft(left);
		return this;
	}

	@Override
	public ModifiableLeftAndRight<E> setRight(E right)
	{
		super.setRight(right);
		return this;
	}

}
