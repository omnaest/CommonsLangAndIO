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
package org.omnaest.utils;

import org.junit.Test;

public class ExceptionUtilsTest
{
    @Test(expected = Exception.class)
    public void testExecuteUncatched() throws Exception
    {
        ExceptionUtils.execute(() ->
        {
            throw new Exception("test");
        }, e ->
        {
            throw e;
        });
    }

    @Test
    public void testExcecuteCatched() throws Exception
    {
        ExceptionUtils.execute(() ->
        {
            throw new Exception("test");
        }, e ->
        {
        });
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteThrowingSilentVoidUncatched() throws Exception
    {
        ExceptionUtils.executeSilentVoid(() ->
        {
            throw new RuntimeException("test");
        }, e ->
        {
            throw new RuntimeException(e);
        });
    }

    @Test
    public void testExecuteThrowingSilentVoidCatched() throws Exception
    {
        ExceptionUtils.executeSilentVoid(() ->
        {
            throw new RuntimeException("test");
        }, e ->
        {
        });
    }
}
