package com.enioka.jqm.integration.tests;

import com.enioka.jqm.runner.java.PayloadClassLoader;
import org.junit.Assert;
import org.junit.Test;

public class PayloadClassLoaderExclusionTest
{
    /**
     * A parent CL that DOES resolve classes (by delegating to the system CL), but is a DISTINCT object from the system CL.
     * This lets us tell apart "loaded through normal parent delegation" from "loaded through the excludedClassPrefixes shortcut",
     * because in the latter case PayloadClassLoader explicitly calls getSystemClassLoader().
     */
    private static class DelegatingParent extends ClassLoader
    {
        DelegatingParent()
        {
            super(ClassLoader.getSystemClassLoader());
        }
    }

    private static final String TEST_CLASS = PayloadClassLoaderExclusionTest.class.getName();

    /**
     * When the prefix matches, the class must be loaded by the SYSTEM class loader, NOT by the (distinct) parent.
     */
    @Test
    public void testExcludedPrefixGoesThroughSystemClassLoader() throws Exception
    {
        DelegatingParent parent = new DelegatingParent();
        try (PayloadClassLoader cl = new PayloadClassLoader(parent))
        {
            cl.setExcludedClassPrefixes("com.enioka.jqm.integration.tests");
            Class<?> loaded = cl.loadClass(TEST_CLASS);

            Assert.assertSame(ClassLoader.getSystemClassLoader(), loaded.getClassLoader());
            // And crucially NOT the parent: proves the exclusion shortcut was taken, not normal delegation.
            Assert.assertNotSame(parent, loaded.getClassLoader());
        }
    }

    /**
     * Control case: without any exclusion, the SAME class is resolved through normal parent delegation.
     * It must therefore be loaded by the parent (the system CL here), reaching the code path the exclusion would bypass.
     */
    @Test
    public void testWithoutExclusionGoesThroughParent() throws Exception
    {
        DelegatingParent parent = new DelegatingParent();
        try (PayloadClassLoader cl = new PayloadClassLoader(parent))
        {
            // No exclusion set.
            Class<?> loaded = cl.loadClass(TEST_CLASS);

            // Loaded by normal delegation -> resolved by the system CL through the parent, NOT via the shortcut.
            Assert.assertSame(ClassLoader.getSystemClassLoader(), loaded.getClassLoader());
        }
    }

    /**
     * A class NOT matching any prefix must NOT be sent to the system CL by the shortcut: it must follow normal delegation.
     * We prove this with a denying parent: the non-excluded class then ends up unresolvable.
     */
    @Test(expected = ClassNotFoundException.class)
    public void testNonMatchingPrefixDoesNotUseShortcut() throws Exception
    {
        // Parent that refuses everything: the only way to load anything is the exclusion shortcut.
        ClassLoader denyingParent = new ClassLoader(null)
        {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException
            {
                throw new ClassNotFoundException("parent refuses " + name);
            }
        };

        try (PayloadClassLoader cl = new PayloadClassLoader(denyingParent))
        {
            // Configure a prefix that does NOT match our test class.
            cl.setExcludedClassPrefixes("org.totally.unrelated");

            // Not matching -> no shortcut -> denying parent -> empty CL -> ClassNotFoundException.
            cl.loadClass(TEST_CLASS);
        }
    }

    /**
     * Parsing: spaces are trimmed and empty entries ignored, so a padded matching prefix still triggers the shortcut.
     */
    @Test
    public void testPrefixStringIsTrimmedAndFiltered() throws Exception
    {
        ClassLoader denyingParent = new ClassLoader(null)
        {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException
            {
                throw new ClassNotFoundException("parent refuses " + name);
            }
        };

        try (PayloadClassLoader cl = new PayloadClassLoader(denyingParent))
        {
            cl.setExcludedClassPrefixes("  ,  com.enioka.jqm.integration.tests  , ");

            // Despite the denying parent, the trimmed matching prefix routes the class to the system CL.
            Class<?> loaded = cl.loadClass(TEST_CLASS);
            Assert.assertSame(ClassLoader.getSystemClassLoader(), loaded.getClassLoader());
        }
    }

    /**
     * Resetting with null/blank must clear previously configured prefixes (so the shortcut no longer applies).
     */
    @Test(expected = ClassNotFoundException.class)
    public void testNullClearsPreviousPrefixes() throws Exception
    {
        ClassLoader denyingParent = new ClassLoader(null)
        {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException
            {
                throw new ClassNotFoundException("parent refuses " + name);
            }
        };

        try (PayloadClassLoader cl = new PayloadClassLoader(denyingParent))
        {
            cl.setExcludedClassPrefixes("com.enioka.jqm.integration.tests");
            cl.setExcludedClassPrefixes(null); // must clear

            // No active prefix -> denying parent -> ClassNotFoundException.
            cl.loadClass(TEST_CLASS);
        }
    }
}
