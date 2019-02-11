
/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2019, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.carrot2.core;

import org.carrot2.core.ControllerTestsBase.ComponentWithInitParameter;
import org.carrot2.util.attribute.Bindable;
import org.carrot2.util.tests.CarrotTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;

import java.util.Collections;

/**
 * Runs matrix tests on {@link Controller} in all realistic configurations.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    ControllerTest.ComponentManagerIndependentTests.class,
    ControllerTest.SimpleControllerCommonTests.class,
    ControllerTest.PoolingControllerCommonTests.class,
    ControllerTest.PoolingControllerPoolingTests.class
})
@SuppressWarnings("unchecked")
public class ControllerTest
{
    @ThreadLeakLingering(linger = 2000)
    public static class ComponentManagerIndependentTests extends CarrotTestCase
    {
        @Test
        public void testAutomaticInitialization()
        {
            Controller controller = null;
            try
            {
                controller = ControllerFactory.createSimple();
                controller.process(Collections.emptyMap(), ComponentWithInitParameter.class);
            }
            finally
            {
                controller.close();
            }
        }

        @Test(expected = IllegalStateException.class)
        public void testUsingSimpleManagerWithMoreThanOneController()
        {
            checkManagerWithMultipleControllers(new SimpleProcessingComponentManager());
        }

        @Test(expected = IllegalStateException.class)
        public void testUsingPoolingManagerWithMoreThanOneController()
        {
            checkManagerWithMultipleControllers(new PoolingProcessingComponentManager());
        }

        @Test(expected = IllegalArgumentException.class)
        public void testUnknownComponentId()
        {
            processAndDispose("nonexistent-component");
        }

        @Test(expected = IllegalArgumentException.class)
        public void testUnexpectedComponentClass()
        {
            processAndDispose(Integer.class);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testUnexpectedComponentDesignatorType()
        {
            processAndDispose(42);
        }

        @Test(expected = IllegalStateException.class)
        public void testMultipleInitialization()
        {
            Controller controller = null;
            try
            {
                controller = ControllerFactory.createSimple();
                controller.init();
                controller.init();
            }
            finally
            {
                controller.close();
            }
        }

        @Test
        public void testMultipleDisposal()
        {
            final Controller controller = ControllerFactory.createSimple();
            try {
              controller.init();
              controller.dispose();
              controller.dispose();
            } finally {
              controller.close();
            }
        }

        private void checkManagerWithMultipleControllers(
            final IProcessingComponentManager manager)
        {
            Controller controller1 = null;
            Controller controller2 = null;
            try
            {
                controller1 = new Controller(manager);
                controller2 = new Controller(manager);
                controller1.init();
                controller2.init();
            }
            finally
            {
                controller1.close();
                controller2.close();
            }
        }

        private void processAndDispose(final Object designator)
        {
            Controller controller = null;
            try
            {
                controller = ControllerFactory.createSimple();
                controller.init();
                controller.process(Collections.emptyMap(), designator);
            }
            finally
            {
                controller.close();
            }
        }
        
    }
    
    public static class SimpleControllerCommonTests extends ControllerTestsCommon
    {
        @Override
        public Controller getSimpleController()
        {
            return ControllerFactory.createSimple();
        }
    }

    public static class PoolingControllerWithFixedPoolCommonTests extends ControllerTestsCommon
    {
        private static final int EAGERLY_INITIALIZED_INSTANCES = 6;

        @Override
        public Controller getSimpleController()
        {
            return ControllerFactory.createPooling(EAGERLY_INITIALIZED_INSTANCES);
        }

        @Override
        public int eagerlyInitializedInstances()
        {
            return EAGERLY_INITIALIZED_INSTANCES;
        }
    }
    
    public static class PoolingControllerWithFixedPoolPoolingTests extends ControllerTestsPooling
    {
        private static final int EAGERLY_INITIALIZED_INSTANCES = 4;

        @Override
        public Controller getPoolingController()
        {
            return ControllerFactory.createPooling(EAGERLY_INITIALIZED_INSTANCES);
        }

        @Override
        public int eagerlyInitializedInstances()
        {
            return EAGERLY_INITIALIZED_INSTANCES;
        }
    }
    
    public static class PoolingControllerCommonTests extends ControllerTestsCommon
    {
        @Override
        public Controller getSimpleController()
        {
            return ControllerFactory.createPooling();
        }
    }

    public static class PoolingControllerPoolingTests extends ControllerTestsPooling
    {
        @Override
        public Controller getPoolingController()
        {
            return ControllerFactory.createPooling();
        }
    }
}