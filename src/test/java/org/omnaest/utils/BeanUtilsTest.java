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

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import org.junit.Test;
import org.omnaest.utils.BeanUtils.Property;

public class BeanUtilsTest
{

    @Test
    public void testAnalyze() throws Exception
    {
        Property<Bean> property = BeanUtils.analyze(Bean.class)
                                           .getProperties()
                                           .findFirst()
                                           .get();
        BeanImpl instance = new BeanImpl("value1");
        assertEquals("value1", property.access(String.class, instance)
                                       .get());

        property.access(String.class, instance)
                .set("value2");

        assertEquals("value2", property.access(String.class, instance)
                                       .get());
    }

    @Test
    public void testAsFlattenedMap()
    {
        Map<String, Object> map = BeanUtils.analyze(ParentBean.class)
                                           .access(new BeanImpl(new BeanImpl("parent value"), "child value"))
                                           .asFlattenedMap()
                                           .asPropertyMap();

        assertEquals(2, map.size());
        assertEquals("parent value", map.get("parent.field"));
        assertEquals("child value", map.get("field"));
    }

    @Test
    public void testProxy()
    {
        Bean bean = BeanUtils.analyze(Bean.class)
                             .newProxy(MapUtils.builder()
                                               .put("field", new BeanUtils.BeanPropertyAccessor<String>()
                                               {
                                                   private String value;

                                                   @Override
                                                   public void accept(String value)
                                                   {
                                                       this.value = value;
                                                   }

                                                   @Override
                                                   public String get()
                                                   {
                                                       return this.value;
                                                   }
                                               })
                                               .build());
        bean.setField("value1");
        assertEquals("value1", bean.getField());
    }

    @Test
    public void testProxyOfMap()
    {
        Bean bean = BeanUtils.analyze(Bean.class)
                             .toMapToProxyMapper()
                             .apply(MapUtils.builder()
                                            .put("field", "value")
                                            .build());

        assertEquals("value", bean.getField());

        bean.setField("value1");
        assertEquals("value1", bean.getField());
    }

    @Test
    public void testInstanceFromMap() throws Exception
    {
        Bean bean = BeanUtils.analyze(BeanImpl.class)
                             .toMapToBeanMapper()
                             .apply(MapUtils.builder()
                                            .put("field", "value")
                                            .build());

        assertEquals("value", bean.getField());

        bean.setField("value1");
        assertEquals("value1", bean.getField());
    }

    @Test
    public void testWithFieldAnnotation() throws Exception
    {
        Bean bean = BeanUtils.analyze(BeanImpl.class)
                             .toMapToBeanMapper()
                             .withFieldNameDeterminigAnnotation(FieldDefinition.class)
                             .apply(MapUtils.builder()
                                            .put("alternativeField", "value")
                                            .build());

        assertEquals("value", bean.getField());

        bean.setField("value1");
        assertEquals("value1", bean.getField());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public static @interface FieldDefinition
    {
        public String value() default "";
    }

    protected static interface Bean
    {
        String getField();

        void setField(String field);
    }

    protected static interface ParentBean extends Bean
    {
        Bean getParent();
    }

    protected static class BeanImpl implements ParentBean
    {
        private Bean parent = null;

        @FieldDefinition("alternativeField")
        private String field;

        public BeanImpl(String field)
        {
            super();
            this.field = field;
        }

        public BeanImpl(Bean parent, String field)
        {
            super();
            this.parent = parent;
            this.field = field;
        }

        protected BeanImpl()
        {
            super();
        }

        @Override
        public String getField()
        {
            return this.field;
        }

        @Override
        public void setField(String field)
        {
            this.field = field;
        }

        @Override
        public Bean getParent()
        {
            return this.parent;
        }

    }

}
