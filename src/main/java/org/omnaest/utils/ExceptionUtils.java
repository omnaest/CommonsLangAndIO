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
package org.omnaest.utils;

/**
 * Helper for {@link Exception} handling
 * 
 * @author omnaest
 */
public class ExceptionUtils
{
	public static interface Operation<R>
	{
		public R execute();
	}

	public static interface OperationWithException<R>
	{
		public R execute() throws Exception;
	}

	public static interface ExceptionHandler
	{
		public void handle(Exception e) throws Exception;
	}

	public static interface RuntimeExceptionHandler
	{
		public void handle(RuntimeException e);
	}

	public static <R, EX> R execute(OperationWithException<R> operation, ExceptionHandler exceptionHandler) throws Exception
	{
		R retval = null;

		try
		{
			retval = operation.execute();
		} catch (Exception e)
		{
			exceptionHandler.handle(e);
		}

		return retval;
	}

	public static <R> R executeSilent(Operation<R> operation, RuntimeExceptionHandler exceptionHandler)
	{
		R retval = null;

		try
		{
			retval = operation.execute();
		} catch (RuntimeException e)
		{
			exceptionHandler.handle(e);
		}

		return retval;
	}
}
