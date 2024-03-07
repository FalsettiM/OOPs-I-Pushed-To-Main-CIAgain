package com.oopsipushedtomain;

import static org.junit.Assert.assertEquals;

import com.oopsipushedtomain.Profile;

import org.junit.Before;
import org.junit.Test;

public class ProfileUnitTest {
    private Profile profile;

    @Before
    public void setUp() {
        profile = new Profile("1", "John Doe", "Johnny", "1990-01-01", "http://john.doe", "123 Main St", "555-1234", "john@example.com");
    }

    @Test
    public void testGetUserId() {
        assertEquals("1", profile.getUserId());
    }

    @Test
    public void testGetName() {
        assertEquals("John Doe", profile.getName());
    }
}