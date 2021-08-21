package org.omnaest.utils.element.transactional.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.omnaest.utils.ListUtils;
import org.omnaest.utils.element.transactional.TransactionalElement;

/**
 * @see DefaultTransactionalElement
 * @author omnaest
 */
public class DefaultTransactionalElementTest
{

    @Test
    public void testCommit() throws Exception
    {
        //
        TransactionalElement<List<String>> element = TransactionalElement.of(() -> new ArrayList<>());
        assertEquals(Collections.emptyList(), element.getStaging());
        assertEquals(null, element.getActive());

        //
        element.getStaging()
               .add("a");
        assertEquals(Arrays.asList("a"), element.getStaging());
        assertEquals(null, element.getActive());

        //
        element.updateStaging(list -> ListUtils.addToNew(list, "b"));
        assertEquals(Arrays.asList("a", "b"), element.getStaging());
        assertEquals(null, element.getActive());

        //
        element.commit();
        assertEquals(Collections.emptyList(), element.getStaging());
        assertEquals(Arrays.asList("a", "b"), element.getActive());
    }

    @Test
    public void testTransaction() throws Exception
    {
        TransactionalElement<List<String>> element = TransactionalElement.of(() -> new ArrayList<>());
        assertEquals(Integer.valueOf(123), element.transaction()
                                                  .execute(() ->
                                                  {
                                                      return 123;
                                                  })
                                                  .withStagingAndActiveMergeFunction((staging, active) ->
                                                  {
                                                      return ListUtils.addAllToNew(staging, "a", "b");
                                                  })
                                                  .commit());
        assertEquals(Arrays.asList("a", "b"), element.getActive());
    }

    @Test
    public void testWithFinalMergeFunction() throws Exception
    {
        TransactionalElement<List<String>> element = TransactionalElement.of(() -> new ArrayList<>());
        element.withFinalMergeFunction((staging, active) -> ListUtils.addAllToNew(staging, "a", "b"))
               .commit();
        assertEquals(Arrays.asList("a", "b"), element.getActive());
    }

}
