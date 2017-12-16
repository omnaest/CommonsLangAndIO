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
package org.omnaest.utils.iterator;

import java.util.Iterator;

public class StringIterator implements Iterator<String>
{
	private String	text;
	private int		position	= -1;

	public StringIterator(String text)
	{
		this.text = text;
	}

	@Override
	public boolean hasNext()
	{
		return this.text != null && this.position + 1 < this.text.length();
	}

	@Override
	public String next()
	{
		int index = ++this.position;
		return this.text != null ? this.text.substring(index, index + 1) : null;
	}

}
