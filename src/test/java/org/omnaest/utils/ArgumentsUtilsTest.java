package org.omnaest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.omnaest.utils.ArgumentsUtils.Argument;
import org.omnaest.utils.ArgumentsUtils.Arguments;
import org.omnaest.utils.ArgumentsUtils.Parameter;

public class ArgumentsUtilsTest
{

    @Test
    public void testParse() throws Exception
    {
        Arguments arguments = ArgumentsUtils.parse("-buildCache", "-sourceFolder", "/root/folder");
        assertTrue(arguments.getParameter("buildCache")
                            .isPresent());
        assertEquals("/root/folder", arguments.getParameter("sourceFolder")
                                              .flatMap(Parameter::getFirstArgument)
                                              .map(Argument::get)
                                              .orElse(""));
    }

    @Test
    public void testConditional()
    {
        AtomicBoolean operationHasRun = new AtomicBoolean(false);
        ArgumentsUtils.parse("-buildCache", "-sourceFolder", "/root/folder")
                      .ifAllParametersArePresent("buildCache")
                      .then(arguments -> operationHasRun.set(true))
                      .orElse(() -> fail("Else block should not be called"));
        assertTrue(operationHasRun.get());
    }

    @Test
    public void testConditionalElseBlock()
    {
        AtomicBoolean elseBlockOperationHasRun = new AtomicBoolean(false);
        ArgumentsUtils.parse("-buildCache", "-sourceFolder", "/root/folder")
                      .ifAllParametersArePresent("extractFiles")
                      .then(arguments -> fail("Then block should not be called"))
                      .orElse(() -> elseBlockOperationHasRun.set(true));
        assertTrue(elseBlockOperationHasRun.get());
    }

    @Test
    public void testConditionalElseBlockAfterSuccessAndUnsuccessCondition()
    {
        AtomicBoolean operationHasRun = new AtomicBoolean(false);
        ArgumentsUtils.parse("-buildCache", "-sourceFolder", "/root/folder")
                      .ifAllParametersArePresent("buildCache")
                      .then(arguments -> operationHasRun.set(true))
                      .orElse()
                      .ifAllParametersArePresent("notAvailableParameter")
                      .then(arguments -> fail("This if condition should not be called"))
                      .orElse(() -> fail("Else block should not be called"));
        assertTrue(operationHasRun.get());
    }

}
