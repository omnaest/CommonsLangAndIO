/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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

public class Triplet<E1, E2, E3>
{
	private E1	element1;
	private E2	element2;
	private E3	element3;

	public Triplet(E1 element1, E2 element2, E3 element3)
	{
		super();
		this.element1 = element1;
		this.element2 = element2;
		this.element3 = element3;
	}

	public E1 getElement1()
	{
		return this.element1;
	}

	public E2 getElement2()
	{
		return this.element2;
	}

	public E3 getElement3()
	{
		return this.element3;
	}

	@Override
	public String toString()
	{
		return "Triplet [element1=" + this.element1 + ", element2=" + this.element2 + ", element3=" + this.element3 + "]";
	}

}
